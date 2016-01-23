package main;

import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.RawDataMessage;
import utilities.ByteArray;
import utilities.Log;
import utilities.Processes;

public class Emulation {
	private static final String APP_PATH = System.getProperty("user.dir");
	private static final int launcherPort = 5554;
	private static final int serverPort = 5555;
	private static Connection.Client launcherCo;
	private static Connection.Server clientDofusCo;

	public static void runASLauncher() {
		if(!Processes.inProcess("adl.exe"))
			try {
				Log.p("Running AS launcher.");
				String adlPath = "C:/PROGRA~2/AdobeAIRSDK/bin/adl.exe";
				if(!Processes.fileExists(adlPath))
					throw new Error("AIR debug launcher not found.");
				else
					Runtime.getRuntime().exec(adlPath + " " + APP_PATH + "/Ressources/Antibot/application.xml");
			} catch (Exception e) {
				e.printStackTrace();
			}
		else
			Log.p("AS launcher already in process.");
	}
	
	public static void sendCredentials() {
		launcherCo = new Connection.Client("127.0.0.1", launcherPort);
		byte[] buffer = new byte[1]; // bool d'injection
		launcherCo.receive(buffer);
		if(buffer[0] == 0)
			Processes.injectDLL(Main.dllLocation, "adl.exe");
		ByteArray array = new ByteArray();
		array.writeInt(2 + 11 + 2 + 10);
		array.writeUTF("maxlebgdu93");
		array.writeUTF("represente");
		Log.p("Sending credentials to AS launcher.");
		launcherCo.send(array.bytes());
	}
	
	public static void createServer(HelloConnectMessage HCM, IdentificationSuccessMessage ISM, RawDataMessage RDM) {
		try {
			clientDofusCo = new Connection.Server(serverPort);
			Log.p("Running emulation server. Waiting Dofus client connection...");
			
			clientDofusCo.waitClient();
			Log.p("Dofus client connected.");
			
			clientDofusCo.send(HCM.makeRaw());
			Log.p("HCM sent to Dofus client");
			
			byte[] buffer = new byte[Main.BUFFER_SIZE];
			int bytesReceived = 0;
			bytesReceived = clientDofusCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			Main.processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			clientDofusCo.send(ISM.makeRaw());
			Log.p("ISM sent to Dofus client");
			clientDofusCo.send(RDM.makeRaw());
			Log.p("RDM sent to Dofus client");
			
			bytesReceived = clientDofusCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			Main.processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			Log.p("Deconnection from AS launcher and Dofus client.");
			launcherCo.close();
			clientDofusCo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
