package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import utilities.Processes;
import messages.NetworkMessage;

public class Log {
	private static final String EOL = System.getProperty("line.separator");
	private static final StringBuilder globalLog = new StringBuilder();
	
	private String logFilepath;
	private PrintWriter writer;
	private StringBuilder logString;
	private StringBuilder logFileBuffer;
	private long lastFlushDate;
	
	public Log(String login) {
		if(!Processes.dirExists(Main.LOG_PATH))
			new File(Main.LOG_PATH).mkdir();
		this.logFilepath = Main.LOG_PATH + login + ".txt";
		try {
			this.writer = new PrintWriter(this.logFilepath, "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.logString = new StringBuilder();
		this.logFileBuffer = new StringBuilder();
		this.lastFlushDate = new Date().getTime();
	}
	
	public synchronized void p(String msgDirection, NetworkMessage msg) { // pour la r�ception et l'envoi de messages
		if(Thread.currentThread().isInterrupted())
			return;
		
		this.logString.append("[").append(Main.DATE_FORMAT.format(new Date())).append("] ");
		if(msgDirection == "r" || msgDirection == "reception")
			this.logString.append("Receiving message ").append(msg.getId()).append(" (").append(msg.getName()).append(")");
		else if(msgDirection == "s" || msgDirection == "sending")
			this.logString.append("Sending message " + msg.getId() + " (" + msg.getName() + ")");
		this.logString.append(EOL);
		writeIntoLogFile(this.logString);
	}
	
	public synchronized void p(String str) {
		if(Thread.currentThread().isInterrupted())
			return;
		
		this.logString.append("[").append(Main.DATE_FORMAT.format(new Date())).append("] ").append(str).append(EOL);
		writeIntoLogFile(this.logString);
	}
	
	public synchronized static void info(String msg) {
		msg = "[" + Main.DATE_FORMAT.format(new Date()) + "] INFO : " + msg;
		globalLog.append(msg);
		globalLog.append(EOL);
		if(Main.TEST_MODE)
			System.out.println(msg);
	}
	
	public synchronized static void warn(String msg) {
		msg = "[" + Main.DATE_FORMAT.format(new Date()) + "] WARNING : " + msg;
		globalLog.append(msg);
		globalLog.append(EOL);
		if(Main.TEST_MODE)
			System.out.println(msg);
	}
	
	public synchronized static void err(String msg) {
		msg = "[" + Main.DATE_FORMAT.format(new Date()) + "] ERROR : " + msg;
		globalLog.append(msg);
		globalLog.append(EOL);
		if(Main.TEST_MODE)
			System.out.println(msg);
	}
	
	public synchronized static String getGlobalLog() {
		return globalLog.toString();
	}
	
	public String getCharacterLog(int linesNumber) {
		return tail(new File(this.logFilepath), linesNumber);
	}
	
	public void flushBuffer() {
		synchronized(this.logFileBuffer) {
			this.writer.print(this.logFileBuffer);
			this.writer.flush();
			this.logFileBuffer.setLength(0);
		}
		this.lastFlushDate = new Date().getTime();
	}
	
	private void writeIntoLogFile(StringBuilder msg) {
		synchronized(this.logFileBuffer) {
			this.logFileBuffer.append(msg);
		}
		msg.setLength(0);
		if(new Date().getTime() - this.lastFlushDate > 10000) // 10 secondes
			flushBuffer();
	}
	
	// fonction r�cup�r�e sur le net
	private String tail(File file, int lines) {
	    java.io.RandomAccessFile fileHandler = null;
	    try {
	        fileHandler = 
	            new java.io.RandomAccessFile( file, "r" );
	        long fileLength = fileHandler.length() - 1;
	        StringBuilder sb = new StringBuilder();
	        int line = 0;

	        for(long filePointer = fileLength; filePointer != -1; filePointer--){
	            fileHandler.seek( filePointer );
	            int readByte = fileHandler.readByte();

	             if( readByte == 0xA ) {
	                if (filePointer < fileLength) {
	                    line = line + 1;
	                }
	            } else if( readByte == 0xD ) {
	                if (filePointer < fileLength-1) {
	                    line = line + 1;
	                }
	            }
	            if (line >= lines) {
	                break;
	            }
	            sb.append( ( char ) readByte );
	        }

	        String lastLine = sb.reverse().toString();
	        return lastLine;
	    } catch( java.io.FileNotFoundException e ) {
	        e.printStackTrace();
	        return null;
	    } catch( java.io.IOException e ) {
	        e.printStackTrace();
	        return null;
	    }
	    finally {
	        if (fileHandler != null )
	            try {
	                fileHandler.close();
	            } catch (IOException e) {
	            }
	    }
	}
}