package messages;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import utilities.ByteArray;
import utilities.Log;

public class RawDataMessage extends Message {
	private byte[] data;
	
	public RawDataMessage(Message msg) {
		super(6253, msg.getLenOfSize(), msg.getSize(), msg.getContent());
		
		deserialize();
		createSWF();
		//sendRDMToClient();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.data = buffer.readBytes(buffer.readVarInt());
	}
	
	private void createSWF() {
		try {
			FileOutputStream fs = new FileOutputStream("./RDM.swf");
			fs.write(data);
			fs.close();
			Log.p("SWF file created.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendRDMToClient() {
		ByteArray buffer = new ByteArray(this.data);
		while(buffer.readUTFBytes(3) != "CWF")
			buffer.incPos(-2);
		buffer.incPos(-3);

		try {
			System.out.println("run server");
			ServerSocket server = new ServerSocket(23);
			Socket client = server.accept();
			OutputStream os = client.getOutputStream();
			os.write(buffer.bytesFromPos());
			
			//server.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private class Test extends Thread {
		public Test() {}

		public void run() {
			try {
				System.out.println("runtime");
				Runtime.getRuntime().exec("C:/PROGRA~2/AdobeAIRSDK/bin/adl C:/Users/Nicolas/Documents/Programmation/Java/tobby/Antibot/application.xml");
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
}
