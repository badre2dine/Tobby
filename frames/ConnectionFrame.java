package frames;

import java.util.Hashtable;

import main.CharacterController;
import main.Emulation;
import main.Instance;
import messages.Message;
import messages.connection.AuthenticationTicketMessage;
import messages.connection.CharacterSelectionMessage;
import messages.connection.CharactersListMessage;
import messages.connection.CharactersListRequestMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationFailedMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.RawDataMessage;
import messages.connection.SelectedServerDataMessage;
import messages.connection.ServerSelectionMessage;
import messages.connection.ServerStatusUpdateMessage;
import messages.connection.ServersListMessage;

public class ConnectionFrame implements IFrame {
	private Instance instance;
	private CharacterController CC;
	private Hashtable<String, Object> usefulInfos = new Hashtable<String, Object>();
	
	public ConnectionFrame(Instance instance, CharacterController CC) {
		this.instance = instance;
		this.CC = CC;
	}
	
	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 3 :
				HelloConnectMessage HCM = new HelloConnectMessage(msg);
				this.usefulInfos.put("HCM", HCM);
				IdentificationMessage IM = new IdentificationMessage();
				IM.serialize(HCM, CC.infos.login, CC.infos.password);
				instance.outPush(IM);
				return true;
			case 22 :
				IdentificationSuccessMessage ISM = new IdentificationSuccessMessage(msg);
				this.usefulInfos.put("ISM", ISM);
				return true;
			case 20 :
				IdentificationFailedMessage IFM = new IdentificationFailedMessage(msg); 
				this.instance.log.p("Authentification failed for reason " + IFM.reason);
				return true;
			case 30 :
				ServersListMessage SLM = new ServersListMessage(msg);
				int serverId = CC.infos.serverId;
				if(SLM.isSelectable(serverId)) {
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize(serverId);
					instance.outPush(SSM);
				}
				else
					this.instance.log.p("Backup in progress on the requested server.");
				return true;
			case 50 :
				ServerStatusUpdateMessage SSUM = new ServerStatusUpdateMessage(msg);
				serverId = CC.infos.serverId;
				if(SSUM.server.id == serverId && SSUM.server.isSelectable) {	
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize(serverId);
					instance.outPush(SSM);
				}
				return true;
			case 42 : 
				SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
				this.usefulInfos.put("ticket", SSDM.ticket);
				instance.setGameServerIP(SSDM.address);
				return true;
			case 101 :
				AuthenticationTicketMessage ATM = new AuthenticationTicketMessage();
				ATM.serialize("fr", (int[]) this.usefulInfos.get("ticket"));
				instance.outPush(ATM);
				return true;
			case 6253 :
				HCM = (HelloConnectMessage) this.usefulInfos.get("HCM");
				ISM = (IdentificationSuccessMessage) this.usefulInfos.get("ISM");
				RawDataMessage RDM = new RawDataMessage(msg);
				Emulation.sendCredentials(CC.infos.login, CC.infos.password);
				Message CIM = Emulation.createServer(HCM, ISM, RDM, instance.id);
				instance.outPush(CIM);
				return true;
			case 6267 :
				CharactersListRequestMessage CLRM = new CharactersListRequestMessage();
				instance.outPush(CLRM);
				return true;
			case 151 : // CharactersListMessage
			case 6475 : // BasicCharactersListMessage
				CharactersListMessage CLM = new CharactersListMessage(msg);
				CC.infos.characterId = CLM.id.toNumber();
				CharacterSelectionMessage CSM = new CharacterSelectionMessage();
				CSM.serialize(CLM);
				instance.outPush(CSM);
				
				this.instance.endOfConnection();
				return true;
		}
		return false;
	}
}