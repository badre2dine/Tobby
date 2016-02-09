package main;

import java.util.Vector;

import messages.EmptyMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import roleplay.movement.MapsAnalyser;
import roleplay.movement.MapsCache;
import roleplay.movement.ankama.Map;
import roleplay.movement.ankama.MapPoint;
import roleplay.movement.ankama.MovementPath;
import roleplay.movement.pathfinding.CellsPathfinder;
import roleplay.movement.pathfinding.Pathfinder;
import roleplay.movement.pathfinding.Pathfinder.PathNode;
import roleplay.paths.Path;
import roleplay.paths.PathsManager;
import utilities.Log;

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
	private CellsPathfinder pathfinder;
	private RoleplayContext context;
	
	public CharacterController(Instance instance, String login, String password, int serverId) {
		this.instance = instance;
		this.login = login;
		this.password = password;
		this.serverId = serverId;
		this.isAccessible = false;
		this.context = new RoleplayContext(this);
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
		this.currentMap = MapsCache.loadMap(mapId);
		this.pathfinder = new CellsPathfinder(this.currentMap);
	}
	
	public String getCurrentPathName() {
		return this.currentPathName;
	}
	
	public void setCurrentPathName(String pathName) {
		this.currentPathName = pathName;
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
	
	public RoleplayContext getContext() {
		return this.context;
	}
	
	public void moveTo(int cellId, boolean changeMap) {
		waitCharacterAccessibility();
		
		if(this.currentCellId == cellId) // d�j� sur la cellule cible
			return;
		
		if(changeMap && !pathfinder.getCellFromId(cellId).allowsChangementMap())
			throw new Error("Target cell does not allow changement of map.");
		
		pathfinder = new CellsPathfinder(this.currentMap);
		Vector<PathNode> nodesVector = pathfinder.compute(this.currentCellId, cellId);
		MovementPath path = CellsPathfinder.movementPathFromArray(nodesVector);
		path.setStart(MapPoint.fromCellId(this.currentCellId));
		path.setEnd(MapPoint.fromCellId(cellId));
		
		Vector<Integer> vector = path.getServerMovement();
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(vector, this.currentMap.id);
		instance.outPush(GMMRM);
		
		try {
			Thread.sleep(pathfinder.getPathTime());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		instance.outPush(EM);
		
		this.currentCellId = cellId;
	}
	
	public void changeMap(int direction) {
		waitCharacterAccessibility();
		
		Log.p("Move to " + Pathfinder.directionToString(direction) + " map.");
		
		moveTo(pathfinder.getChangementMapCell(direction), true);
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(this.currentMap.getNeighbourMapFromDirection(direction));
		instance.outPush(CMM);
		
		this.isAccessible = false; // on attend la fin du changement de map
	}
	
	public void launchFight(int position, double id) {
		moveTo(position, false);
		GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
		GRPAMRM.serialize(id);
		instance.outPush(GRPAMRM);
	}
	
	public void run() {
		waitCharacterAccessibility();
		MapsAnalyser.getZones(this.currentMap);
		Path path = PathsManager.getPathByName("test2");
		path.run(this);
	}
}
