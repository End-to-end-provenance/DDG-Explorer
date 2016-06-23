package laser.ddg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DDGServer extends ServerSocket{
	
	String s = "";
	public DDGServer(int portNumber) throws IOException{
		super(portNumber);
		Socket clientSocket = this.accept();
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		if(clientSocket.isConnected()){
			System.out.println("YAY CONNECTED");
		}
		while((s =in.readLine())!=null){
			System.out.println(s);
		}
		this.close();
	}
	

}