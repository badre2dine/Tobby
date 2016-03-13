package controller;

import main.Instance;
import messages.context.GameContextReadyMessage;
import messages.fights.GameFightJoinRequestMessage;

public class SoldierController extends FighterController {
	protected CaptainController captain;

	public SoldierController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId);
	}
	
	public void setCaptain(CaptainController captain) {
		this.captain = captain;
	}
	
	private void followCaptain() {
		waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		while(this.infos.currentMap.id != this.captain.infos.currentMap.id) {
			this.mvt.dynamicGoTo(this.captain.infos.currentMap.id);
			waitState(CharacterState.IS_LOADED); // attendre le refresh des infos
		}
	}
	
	private void joinFight() {
		waitState(CharacterState.IS_LOADED); // peut-�tre encore dans le pr�c�dent combat
		
		GameFightJoinRequestMessage GFJRM = new GameFightJoinRequestMessage();
		GFJRM.fighterId = this.captain.infos.characterId;
		GFJRM.fightId = this.roleplayContext.currentCaptainFightId;
		GFJRM.serialize();
		this.instance.outPush(GFJRM);
		this.instance.log.p("Request for join fight sent.");
	}
	
	public void run() {
		waitState(CharacterState.IS_LOADED);
		
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas � quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			fight(true);
		}
		if(inState(CharacterState.IN_PARTY))
			leaveGroup();
		captain.newRecruit(this);
		waitState(CharacterState.IN_PARTY);
		changePlayerStatus();
		
		while(!isInterrupted()) {
			followCaptain();
			waitState(CharacterState.CAPTAIN_ACT);
			if(this.captain.inState(CharacterState.IN_FIGHT)) {
				waitState(CharacterState.FIGHT_LAUNCHED);
				joinFight();
				if(waitState(CharacterState.IN_FIGHT)) {
					fight(false);
					upgradeStatsAndSpell();
				}
			}
			else if(this.captain.inState(CharacterState.NEED_TO_EMPTY_INVENTORY))
				goToExchangeWithMule(true);
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}