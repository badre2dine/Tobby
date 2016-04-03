package controller.api;

import gamedata.character.PlayerLifeStatusEnum;
import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.d2o.modules.SubArea;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.CharacterState;
import controller.FightOptions;
import controller.characters.Captain;
import controller.characters.Fighter;
import controller.characters.Soldier;
import messages.EmptyMessage;
import messages.character.SpellUpgradeRequestMessage;
import messages.character.StatsUpgradeRequestMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import messages.fights.GameActionFightCastOnTargetRequestMessage;
import messages.fights.GameFightReadyMessage;
import messages.fights.GameFightTurnFinishMessage;

public class FightAPI {
	private Fighter fighter;
	private FightOptions fightOptions;

	public FightAPI(Fighter fighter, int fightAreaId) {
		this.fighter = fighter;
		this.fightOptions = new FightOptions(fightAreaId);
	}
	
	public FightAPI(Fighter fighter) {
		this(fighter, 0);
	}

	public int getFightAreaId() {
		return this.fightOptions.getFightAreaId();
	}

	public void updateFightArea(int level) {
		this.fightOptions.updateFightArea(level);
		this.fighter.instance.log.graphicalFrame.setAreaLabel(SubArea.getSubAreaById(this.fightOptions.getFightAreaId()).getName());
	}

	public void levelUpManager() {
		if(!this.fighter.waitState(CharacterState.LEVEL_UP))
			return;
		this.fighter.waitState(CharacterState.IS_LOADED);
		upgradeSpell();
		increaseStats();
		if(this.fighter instanceof Soldier)
			((Soldier) this.fighter).getCaptain().updateTeamLevel(); // met � jour le niveau de l'escouade et donc possiblement l'aire de combat 
		else if(this.fighter instanceof Captain)
			((Captain) this.fighter).updateTeamLevel();
		else
			updateFightArea(this.fighter.infos.level);
	}

	public void lifeManager() {
		this.fighter.waitState(CharacterState.IS_LOADED);

		int missingLife = this.fighter.infos.missingLife();
		this.fighter.instance.log.p("Missing life : " + missingLife + " life points.");
		if(missingLife > 0) {
			this.fighter.instance.log.p("Break for life regeneration.");
			try {
				Thread.sleep(this.fighter.infos.regenRate * 100 * missingLife); // on attend de r�cup�rer toute sa vie
			} catch(Exception e) {
				Thread.currentThread().interrupt();
				return;
			}
			this.fighter.infos.stats.lifePoints = this.fighter.infos.stats.maxLifePoints;
			this.fighter.instance.log.graphicalFrame.setLifeLabel(this.fighter.infos.stats.lifePoints, this.fighter.infos.stats.maxLifePoints);
		}
	}
	
	public void rebirthManager() {
		if(this.fighter.infos.healthState == PlayerLifeStatusEnum.STATUS_TOMBSTONE) {
			EmptyMessage msg = new EmptyMessage("GameRolePlayFreeSoulRequestMessage");
			this.fighter.instance.outPush(msg);
			this.fighter.updateState(CharacterState.IS_LOADED, false);
			this.fighter.interaction.useInteractive(287, 479466, 152192, false);
			try {
				Thread.sleep(2000); // temporaire
			} catch(Exception e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void inventoryManager() {
		if(this.fighter.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
			this.fighter.instance.log.p("Need to empty inventory.");
			this.fighter.social.goToExchange(this.fighter.mule);
		}
	}

	public boolean fightSearchManager() {
		this.fighter.waitState(CharacterState.IS_LOADED);

		this.fighter.instance.log.p("Searching for monster group to fight.");
		int monsterGroupSize;
		int monsterGroupMaxSize = this.fightOptions.getMonsterGroupMaxSize();
		for(GameRolePlayGroupMonsterInformations monsterGroup : this.fighter.roleplayContext.getMonsterGroups()) {
			monsterGroupSize = monsterGroup.staticInfos.underlings.size() + 1;
			if(monsterGroupSize <= monsterGroupMaxSize) {
				this.fighter.instance.log.p("Going to take a monster group of size " + monsterGroupSize + " on cell id " + monsterGroup.disposition.cellId + ".");
				if(!this.fighter.mvt.moveTo(monsterGroup.disposition.cellId, false)) // groupe de monstres inatteignable
					continue;
				this.fighter.instance.log.p("Sending attack request.");
				GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
				GRPAMRM.serialize(monsterGroup.contextualId);
				this.fighter.instance.outPush(GRPAMRM);
				return true;
			}
		}
		this.fighter.instance.log.p("None monster group available or attackable on the map.");
		return false;
	}

	public void fightManager(boolean fightRecovery) {
		if(!fightRecovery) { // si c'est un combat tout frais
			GameFightReadyMessage GFRM = new GameFightReadyMessage();
			GFRM.serialize();
			this.fighter.instance.outPush(GFRM);
		}
		while(!Thread.currentThread().isInterrupted() && this.fighter.inState(CharacterState.IN_FIGHT)) {
			this.fighter.waitState(CharacterState.IN_GAME_TURN); // attente du d�but du prochain tour ou de la fin du combat
			if(!this.fighter.inState(CharacterState.IN_FIGHT))
				break;
			Vector<GameFightMonsterInformations> aliveMonsters = this.fighter.fightContext.getAliveMonsters();
			this.fighter.instance.log.p(aliveMonsters.size() + " alive monster(s) remaining.");
			for(GameFightMonsterInformations aliveMonster : aliveMonsters) {
				if(this.fighter.inState(CharacterState.IN_GAME_TURN) && this.fighter.fightContext.self.stats.actionPoints >= this.fighter.infos.attackSpellActionPoints)
					castSpellOverMonster(aliveMonster);
				else
					break;
			}
			if(this.fighter.inState(CharacterState.IN_FIGHT)) {
				GameFightTurnFinishMessage GFTFM = new GameFightTurnFinishMessage();
				GFTFM.serialize();
				this.fighter.instance.outPush(GFTFM);
				this.fighter.updateState(CharacterState.IN_GAME_TURN, false);
			}
		}
		if(this.fighter.roleplayContext.lastFightOutcome) { // si on a gagn� le combat
			this.fighter.infos.fightsWonCounter++;
			this.fighter.instance.log.graphicalFrame.setFightsWonLabel(this.fighter.infos.fightsWonCounter);
		}
		else {
			this.fighter.infos.fightsLostCounter++;
			this.fighter.instance.log.graphicalFrame.setFightsLostLabel(this.fighter.infos.fightsLostCounter);
			this.fighter.instance.log.p("Fight lost.");
		}
	}

	private void upgradeSpell() {
		int spellId = this.fighter.infos.attackSpell;
		if(this.fighter.infos.spellList.get(spellId) != null && canUpgradeSpell(spellId)) {
			this.fighter.infos.spellList.get(spellId).spellLevel++;
			SpellUpgradeRequestMessage SURM = new SpellUpgradeRequestMessage();
			SURM.serialize(spellId, this.fighter.infos.spellList.get(spellId).spellLevel);
			this.fighter.instance.outPush(SURM);
			this.fighter.instance.log.p("Increasing attack spell to level " + this.fighter.infos.spellList.get(spellId).spellLevel + ".");
		}
	}

	private boolean canUpgradeSpell(int spellId) {
		int level = this.fighter.infos.spellList.get(spellId).spellLevel;
		if(level < 5)
			return this.fighter.infos.stats.spellsPoints >= level;
			return false;
	}

	private void increaseStats() {
		StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
		SURM.serialize(this.fighter.infos.element, calculateMaxStatsPoints());
		this.fighter.instance.outPush(SURM);
		this.fighter.instance.log.p("Increase stat : " + this.fighter.infos.element + ".");
	}

	private int calculateMaxStatsPoints() {
		int stage = (getElementInfoById() / 100) + 1;
		return this.fighter.infos.stats.statsPoints - (this.fighter.infos.stats.statsPoints % stage);
	}

	private int getElementInfoById() {
		switch(this.fighter.infos.element) {
			case 10 : return this.fighter.infos.stats.strength.base;
			case 13 : return this.fighter.infos.stats.chance.base;
			case 14 : return this.fighter.infos.stats.agility.base;
			case 15 : return this.fighter.infos.stats.intelligence.base;
		}
		return 0;
	}

	private void castSpellOverMonster(GameFightMonsterInformations monster) {
		this.fighter.instance.log.p("Trying to cast a spell over a monster.");
		GameActionFightCastOnTargetRequestMessage GAFCOTRM = new GameActionFightCastOnTargetRequestMessage();
		GAFCOTRM.spellId = this.fighter.infos.attackSpell;
		GAFCOTRM.targetId = monster.contextualId;
		GAFCOTRM.serialize();
		this.fighter.instance.outPush(GAFCOTRM);
		/*
		GameActionFightCastRequestMessage GAFCRM = new GameActionFightCastRequestMessage();
		GAFCRM.serialize(this.infos.spellToUpgrade, monster.disposition.cellId, this.instance.id);
		instance.outPush(GAFCRM);
		 */
		this.fighter.waitState(CharacterState.SPELL_CASTED); // on attend le r�sultat du sort lanc�
		try {
			Thread.sleep(1000); // n�cessaire sinon kick par le serveur
		} catch(Exception e) {
			Thread.currentThread().interrupt();
			return;
		}
	}
}