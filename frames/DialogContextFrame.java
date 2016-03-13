package frames;

import controller.CharacterController;
import controller.CharacterState;
import main.Instance;
import messages.Message;
import messages.exchanges.ExchangeIsReadyMessage;

public class DialogContextFrame extends Frame {
	private Instance instance;
	private CharacterController character;
	
	public DialogContextFrame(Instance instance, CharacterController character) {
		this.instance = instance;
		this.character = character;
	}

	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 5509 : // ExchangeIsReadyMessage 
				ExchangeIsReadyMessage EIRM = new ExchangeIsReadyMessage(msg);
				if(EIRM.id != this.character.infos.characterId) {
					this.instance.log.p("Exchange validated by peer.");
					this.character.updateState(CharacterState.EXCHANGE_VALIDATED, true);
				}
				return true;
		}
		return false;
	}
}