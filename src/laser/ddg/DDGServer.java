package laser.ddg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class contains the code that starts the server and accepts multiple clients
 * @author Moe Pwint Phyu 
 *
 */
public class DDGServer extends ServerSocket{
	
	private Socket clientSocket;
	private ClientConnection clientConnection;

	public DDGServer(int portNumber) throws IOException{
		super(portNumber);
		//accept multiple clients 
		while(true){
			clientSocket = this.accept();
			clientConnection = new ClientConnection(clientSocket);
			new Thread(clientConnection).start();
		}
	}
	
	public ClientConnection getClientConnection(){
		return this.clientConnection;
	}
}

