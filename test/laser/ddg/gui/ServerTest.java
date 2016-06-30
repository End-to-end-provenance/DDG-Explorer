package laser.ddg.gui;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerTest {
	private static Socket toServer;
	private static PrintWriter out;
	private static Thread serverThread;

	public static void main(String[] args) {
		System.out.println("In ServerTest");
		startServer();
		try {
			Thread.yield();
			Thread.sleep(2000);
			connectToServer();
			Thread.yield();
			Thread.sleep(2000);
			sendHeader();
			sendDDG();
//			Thread.yield();
//			while (serverThread.isAlive()) {
//				Thread.sleep(1000);
//			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (toServer != null) {
				try {
					toServer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Server test ending");
	}

	private static void startServer() {
		Runnable server = new Runnable() {

			@Override
			public void run() {
				String[] args = { "True" };
				DDGExplorer.main(args);
			}

		};
		serverThread = new Thread(server);
		serverThread.setDaemon(false);
		serverThread.start();
	}

	private static void connectToServer() throws UnknownHostException, IOException {
		toServer = new Socket("localhost", 6096);
		out = new PrintWriter(toServer.getOutputStream());
	}

	private static void sendHeader() {
		// Filename
		System.out.println("Sending filename");
		out.println("foo.R");
		
		// Timestamp
		System.out.println("Sending timestamp");
		out.println("NOW");
		
		System.out.println("Sending language");
		out.println("Language=\"R\"");
		
		System.out.println("Sending flag for # pins");
		out.println(-1);
		
		out.flush();
	}
	
	private static void sendDDG() throws IOException {
		sendLine("Start p1 \"1-SuperSimple.R\" Value=\"SuperSimple.R\" Time=\"0.919999999999987\" Script=\"0\" Line=\"NA\";");
		sendLine("Operation p2 \"2-a <- 1\" Value=\"a <- 1\" Time=\"0.926000000000016\" Script=\"0\" Line=\"1\";");
		sendLine("CF p1 p2");
		sendLine("Data d1 \"1-a\" Value=\"1\";");
		sendLine("DF p2 d1");
		sendLine("Operation p3 \"3-b <- 2\" Value=\"b <- 2\" Time=\"0.99199999999999\" Script=\"0\" Line=\"2\";");
		sendLine("CF p2 p3");
		sendLine("Data d2 \"2-b\" Value=\"2\";");
		sendLine("DF p3 d2");
		sendLine("Finish p4 \"4-SuperSimple.R\" Value=\"SuperSimple.R\" Time=\"1.00199999999998\" Script=\"0\" Line=\"NA\";");
		sendLine("CF p3 p4");
	}

	private static void sendLine(String line) throws IOException {
		System.out.println("Sending " + line);
		out.println(line);
		out.flush();
		System.out.println("Hit enter to continue");
		System.in.read();
	}

	
	
	
	



}
