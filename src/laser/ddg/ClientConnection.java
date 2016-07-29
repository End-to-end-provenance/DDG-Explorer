package laser.ddg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import laser.ddg.commands.LoadFileCommand;

/**
 * A class that reads in information from the client side 
 * @author Moe Pwint Phyu
 *
 */
public class ClientConnection implements Runnable{

	private String fileName;
	private String timeStamp;
	private Socket clientSocket;
	private String language;
	private static BufferedReader in;
	
	
	public ClientConnection(Socket clientSocket) throws IOException{
		this.clientSocket = clientSocket;
	}
	
	@Override
	public void run(){
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			fileName = in.readLine();
			timeStamp = in.readLine();
			language = in.readLine();
			LoadFileCommand.executeIncrementalDrawing(this);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Socket getClientSocket(){
		return clientSocket;
	}
	public String getFileName(){
		return fileName;
	}

	public String getTimeStamp(){
		return timeStamp;
	}
	
	public static BufferedReader getClientReader() {
		return in;
	}

	public String getLanguage() {
		return language;
	}
}
