package laser.ddg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DDGServer extends ServerSocket{
	
	String s = "";
	String fileName;
	String timeStamp;
	Socket clientSocket;
	private BufferedReader in;
	
	
	public DDGServer(int portNumber) throws IOException{
		super(portNumber);
		clientSocket = this.accept();
		if(clientSocket.isConnected()){
			System.out.println("YAY CONNECTED");
		}
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		System.out.println("Server got input stream");
//		try {
			System.out.println("Calling readLine");
			fileName = in.readLine();
//			System.out.println("readLine returned");
//			while (fileName == null) {
//				System.out.println("Server waiting for filename");
//				Thread.sleep(1000);
//				fileName = in.readLine();
//			}
			System.out.println("Got filename");
			timeStamp = in.readLine();
			System.out.println("Got timestamp");
//		while((s =in.readLine())!=null){
//			System.out.println(s);
//		}
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	
	public BufferedReader getClientReader() {
		return in;
	}
	

}