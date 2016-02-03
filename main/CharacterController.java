package main;

import java.util.Vector;

import messages.EmptyMessage;
import messages.currentmap.ChangeMapMessage;
import messages.currentmap.GameMapMovementRequestMessage;
import messages.roleplay.GameRolePlayAttackMonsterRequestMessage;
import roleplay.movement.D2pReader;
import roleplay.movement.Pathfinder;
import roleplay.movement.ankama.Map;
import roleplay.movement.ankama.MapMovementAdapter;
import roleplay.movement.ankama.MapPoint;
import roleplay.movement.ankama.MovementPath;
import roleplay.paths.Path;
import roleplay.paths.PathsManager;

public class CharacterController extends Thread {
	private Instance instance;
	private String login;
	private String password;
	private int serverId;
	private String characterName;
	private double characterId;
	private int currentCellId;
	private int currentDirection;
	private Map currentMap;
	private String currentPathName;
	private boolean isAccessible;
	
	public CharacterController(Instance instance, String login, String password, int serverId) {
		this.instance = instance;
		this.login = login;
		this.password = password;
		this.serverId = serverId;
		this.isAccessible = false;
	}
	
	public String getLogin() {
		return this.login;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public int getServerId() {
		return this.serverId;
	}
	
	public synchronized void makeCharacterAccessible() {
		this.isAccessible = true;
		notify();
	}
	
	public synchronized void makeCharacterInaccessible() {
		this.isAccessible = false;
	}
	
	public synchronized void waitCharacterAccessibility() {
		if(!this.isAccessible)
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public String getCharacterName() {
		return this.characterName;
	}
	
	public void setCharacterName(String characterName) {
		this.characterName = characterName;
	}
	
	public double getCharacterId() {
		return this.characterId;
	}
	
	public void setCharacterId(double characterId) {
		this.characterId = characterId;
	}
	
	public int getCurrentCellId() {
		return this.currentCellId;
	}
	
	public void setCurrentCellId(int cellId) {
		this.currentCellId = cellId;
	}
	
	public int getCurrentDirection() {
		return this.currentDirection;
	}
	
	public void setCurrentDirection(int direction) {
		this.currentDirection = direction;
	}
	
	public int getCurrentMapId() {
		return this.currentMap.id;
	}
	
	public void setCurrentMap(int mapId) {
		this.currentMap = new Map(D2pReader.getBinaryMap(mapId));
		Pathfinder.initMap(this.currentMap);
	}
	
	public String getCurrentPathName() {
		return this.currentPathName;
	}
	
	public void moveTo(int cellId) {
		waitCharacterAccessibility();
		
		MapPoint src = MapPoint.fromCellId(this.currentCellId);
		MapPoint dest = MapPoint.fromCellId(cellId);
		
		MovementPath path = Pathfinder.compute(this.currentCellId, cellId);
		path.setStart(src);
		path.setEnd(dest);
		
		Vector<Integer> vector = MapMovementAdapter.getServerMovement(path);
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(vector, this.currentMap.id);
		instance.outPush(GMMRM);
		
		try {
			Thread.sleep(Pathfinder.getPathTime());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		instance.outPush(EM);
		
		this.currentCellId = cellId;
	}
	
	public void changeMap(int direction) {
		waitCharacterAccessibility();
		
		moveTo(Pathfinder.getChangementMapCell(direction));
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(this.currentMap.getNeighbourMapFromDirection(direction));
		instance.outPush(CMM);
		
		this.isAccessible = false; // on attend la fin du changement de map
	}
	
	public void runPath(String pathName) {
		Path path = PathsManager.getPathByName(pathName);
		path.checkCurrentPos(this.currentMap.id); // v�rifie si le perso est sur le trajet
		this.currentPathName = pathName;
		int nextMapId;
		while((nextMapId = path.nextMap()) != -1)
			changeMap(nextMapId);
	}
	
	public void run() {
		while(true) {
			waitCharacterAccessibility();
			runPath("test");
		}
	}
	
	public void launchFight(int position, double id) {
		moveTo(position);
		GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
		GRPAMRM.serialize(id);
		instance.outPush(GRPAMRM);
	}
}