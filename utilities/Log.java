package utilities;

import messages.Message;

public class Log {
	public synchronized static void p(String msgDirection, Message msg) {
		int id = msg.getId();
		int lenofsize = msg.getLenOfSize();
		int size = msg.getSize();
		if(msgDirection == "r" || msgDirection == "reception")
			System.out.println("Receiving message " + id + " (" + Message.get(id) + ")");
		else if(msgDirection == "s" || msgDirection == "sending")
			System.out.println("Sending message " + id + " (" + Message.get(id) + ")");
		if(lenofsize > 1)
			System.out.println("Length of size : " + lenofsize + " bytes");
		else
			System.out.println("Length of size : " + lenofsize + " byte");
		if(size > 1)
			System.out.println("Size : " + size + " bytes\n");
		else
			System.out.println("Size : " + size + " byte\n");
	}
	
	public synchronized static void p(String str) {
		System.out.println(str + '\n');
	}
}
