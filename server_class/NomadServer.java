import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Object.*;
import nomads.v210.*;

public class NomadServer implements Runnable {  
	private NomadServerThread clients[] = new NomadServerThread[5000];
	private NomadServerThread currentClient;
	private short clientThreadNum[] = new short[100000];

	private String IPsLoggedIn[] = new String[1000];
	private String users[] = new String[1000];

	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;
	private int IPCount = 0;
	private int userCount = 0;
	private int eventNum = 0;
	private static int debugLine = 0;
	private static String[] children;
	private static Boolean requireLogin = false;
	private Calendar cal;
	long nowT,appT,diffT,lagT;

	//Instance variables to store values that the server will give to new clients
	private static byte _DISCUSS_STATUS = 0;
	private static byte _CLOUD_STATUS = 0;
	private static byte _POLL_STATUS = 0;
	private static byte _MOSAIC_STATUS = 0;
	private static byte _SWARM_STATUS = 0;
	private static byte _UGROOVE_STATUS = 0;

	int iDay;
	FileOutputStream out; // declare a file output object
	PrintStream p; // declare a print stream object

	// xxx
	NGrain myGrain;

	//Set up app id's 
	private String printID (byte id) {
		String[] idList = new String[255];
		int i;
		for(i=0;i<255;i++) {
			idList[i] = null;
		}

		// Populate the list

		idList[NAppID.SERVER] = new String("SERVER");
		idList[NAppID.INSTRUCTOR_PANEL] = new String("INSTRUCTOR_PANEL");
		idList[NAppID.BINDLE] = new String("BINDLE");
		idList[NAppID.DISCUSS] = new String("DISCUSS");
		idList[NAppID.DISCUSS_PROMPT] = new String("DISCUSS_PROMPT");
		idList[NAppID.INSTRUCTOR_DISCUSS] = new String("INSTRUCTOR_DISCUSS");
		idList[NAppID.CLOUD_DISPLAY] = new String("CLOUD_DISPLAY");
		idList[NAppID.CLOUD_CHAT] = new String("CLOUD_CHAT");
		idList[NAppID.CLOUD_PROMPT] = new String("CLOUD_PROMPT");
		idList[NAppID.DEBUG] = new String("DEBUG");
		//***STK Need to add rest of list...

		// Print out the id as a string
		if (idList[id] != null) {
			String rString = new String(idList[id] + "[" + id + "]");
			return rString;	
		}
		else {
			String rString = new String("UNKNOWN[" + id + "]");
			return rString;	
		}
	}

	public NomadServer(int port) {  	    
		for (int i=0;i<100000;i++) {
			clientThreadNum[i] = -1;
		}
		for (int i=0;i<1000;i++) {
			IPsLoggedIn[i] = null;
			users[i] = null;
		}
		try {  
			NGlobals.sPrint("  Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);  
			NGlobals.sPrint("  Server started: " + server);
			start(); 
		}
		catch(IOException ioe)  {  	   
			NGlobals.sPrint("  Can not bind to port " + port + ": " + ioe.getMessage());
			ioe.printStackTrace();
			System.exit(1); 
		}
	}

	public void run()  {  
		while (thread != null) {  
			try {  
				NGlobals.sPrint("  Waiting for a client ..."); 
				addThread(server.accept()); 
			}
			catch(IOException ioe)  {  
				NGlobals.sPrint("  Server accept error: " + ioe); stop(); }
		}
	}

	public void start()  {  
		if (thread == null)  {  
			thread = new Thread(this); 
			thread.start();
		}
	}

	public void stop() {  
		if (thread != null)  {  
			thread.stop(); 
			thread = null;
		}
	}

	public synchronized void getFiles(String iDir) {
		File dir = new File(iDir); 
		children = dir.list(new DirFilter(".")); 
		if (children == null) { // Either dir does not exist or is not a directory 
		} else { 
			for (int i=0; i<children.length; i++) { // Get filename of file or directory 
				String filename = children[i]; 
				NGlobals.sPrint("Getting file:  " + filename); 
			} 
		} // It is also possible to filter the list of returned files. 
		// This example does not return any files that start with `.'. 

		// FilenameFilter filter = new FilenameFilter() { 
		// 	public boolean accept(File dir, String name) {
		// 	    return !name.startsWith("."); 
		// 	} 
		//     };

		// children = dir.list(filter); 
	}

	private synchronized short checkIP (String IP) {
		NGlobals.sPrint("          checkIP(" + IP + ")");

		for (int i = 0; i < IPCount; i++) {
			if (IPsLoggedIn[i] == null)
				return (short)-1;
			if (IPsLoggedIn[i].equals(IP))
				return (short)i;
		}
		return (short)-1;
	}

	class DirFilter implements FilenameFilter {
		String afn;
		DirFilter(String afn) { this.afn = afn; }
		public boolean accept(File dir, String name) {
			// Strip path information:
			String f = new File(name).getName();
			return f.indexOf(afn) != -1;
		}
	}

	// ================================================================
	//    handle ( THREAD_ID , grain )
	// ================================================================

	public synchronized void handle(int THREAD_ID, NGrain myGrain)  {  
		String tUser, IP, tempString;
		Boolean tLoginStatus = false;
		int cNum = -1;
		int cIPNum = -1;
		int nBlocks; //number of "blocks" of data
		int tCNum;
		int incIntData[] = new int[1000];
		byte incByteData[] = new byte[1000];

		byte incAppCmd, incAppDataType;
		int incAppDataLen;
		byte incAppID;

		NGrain inGrain;

		NGlobals.sPrint("-----------------------------------------------------[" + debugLine++ + "]");



		// Do the following for EACH client

		// 1 ---- READ ------------------------------------------------------------------
		// ------------------------------------------------------------------------------

		NGlobals.sPrint("===== READING =====");

		// Read in relevant SAND header info
		incAppID = myGrain.appID;
		incAppCmd = myGrain.command;
		incAppDataType = myGrain.dataType;
		incAppDataLen = myGrain.dataLen;

		// Print out at the SERVER level
		NGlobals.sPrint("appID: " + incAppID);
		NGlobals.sPrint("appID(): " + printID((byte)incAppID));
		NGlobals.sPrint("command: " + incAppCmd);
		NGlobals.sPrint("dataType: " + incAppDataType);
		NGlobals.sPrint("dataLen: " + incAppDataLen);
		myGrain.print();

		// Thread admin stuff ---------------------------------------------------------------------

		// Get client number of inc client
		tCNum = clientThreadNum[THREAD_ID];

		if (tCNum < 0) {
			NGlobals.sPrint("   ERROR:  client thread not found.");
			// TODO:  send the bye command!!!
			remove(THREAD_ID);
			return;
		}

		currentClient = clients[tCNum];

		// Login and THREAD registration ----------------------------------------------------------

		// 1: check if client thread is registered
		//    if not reg, REGISTER the client's appID with the SERVER client thread
		if (currentClient.getAppID() == -1) {
			if (incAppCmd != NCommand.REGISTER) {
				NGlobals.sPrint("ERROR:  you must REGISTER your app first before sending data\n");
				remove(THREAD_ID);
				return;
			}
			else {
				NGlobals.sPrint("===== REGISTERING (ONE TIME) =====");
				NGlobals.sPrint("  Setting client[" + tCNum + "] incAppID to: " + incAppID);
				currentClient.setAppID(incAppID);
			}
		}


		// 2: check login
		//      only the LOGIN app can log you in
		//      if you're not logged in, you get booted

		tLoginStatus = currentClient.getLoginStatus();
		if ((incAppID == NAppID.BINDLE) && (incAppCmd == NCommand.LOGIN)) {
			if (tLoginStatus == true) {
				// send back "you're already logged in" message / LOGIN_STATUS w/ value = 2
				NGlobals.sPrint("  LOGIN client [" + tCNum + "] already logged in.\n" + incAppID);

				byte[] dx = new byte[1];
				dx[0] = 2;
				currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.LOGIN_STATUS, NDataType.UINT8, 1, dx);
			}
			else {
				// Log the client in
				String tString = new String(myGrain.bArray);
				NGlobals.sPrint("Got username: " + tString);

				// add USER NAME to clients[] lookup array
				clients[tCNum].setUser(tString);

				// Set new login status 
				clients[tCNum].setLoginStatus(true);
				tLoginStatus = currentClient.getLoginStatus();

				NGlobals.sPrint("  LOGIN client [" + tCNum + "] logging in, sending back confirmation info.\n" + incAppID);

				// send back "successful login" message / LOGIN_STATUS w/ value = 1
				byte[] dx = new byte[1];
				dx[0] = 1;
				currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.LOGIN_STATUS, NDataType.UINT8, 1, dx);
			}
		}

		// Comment this out to turn back on login : LOGIN TOGGLE
		// TODO:  needs to be fixed first for INSTRUCTOR PANEL (may require login for that too)

		tLoginStatus = true;

		// Kick -------------------------------------------------------------------------------------

		// TODO this message should go back to the Bindle login app, or some kind of status screen

		// X:  if you're not the LOGIN app providing the correct info, you get booted here
		if (tLoginStatus == false) {
			NGlobals.sPrint("   WARNING:  client THREAD NOT logged in.");
			remove(THREAD_ID);
			// TODO: send back some sand data re: login info
			return;
		}

		// ====================================================================================
		//  At this point we're logged in, and we have your SAND data GRAIN
		// ====================================================================================

		NGlobals.sPrint("   client THREAD logged in.");

		// Grab IP (more for data logging)

		IP = currentClient.getIP();

		// 1.5: INIT =================================================================================================================
		currentClient = clients[tCNum];

		// If you are THE BINDLE or the INSTRUCTOR_PANEL -----------------------------------------------

		if (((currentClient.getAppID() == NAppID.BINDLE) || (currentClient.getAppID() == NAppID.INSTRUCTOR_PANEL)) && (currentClient.getButtonInitStatus() == 0)) {

			NGlobals.lPrint("  Sending button states to Bindle / Instructor Panel from SERVER.");
			byte d[] = new byte[1];
			int ix[] = new int[1];

			d[0] = _DISCUSS_STATUS;
			currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_DISCUSS_STATUS, NDataType.UINT8, 1, d);
			NGlobals.lPrint("_DISCUSS_STATUS: " + d[0]);

			d[0] = _CLOUD_STATUS;
			currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_CLOUD_STATUS, NDataType.UINT8, 1, d);
			NGlobals.lPrint("_CLOUD_STATUS: " + d[0]);

			d[0] = _POLL_STATUS;
			currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_POLL_STATUS, NDataType.UINT8, 1, d);
			NGlobals.lPrint("_POLL_STATUS: " + d[0]);

			d[0] = _MOSAIC_STATUS;
			currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_MOSAIC_STATUS, NDataType.UINT8, 1, d);
			NGlobals.lPrint("_MOSAIC_STATUS: " + d[0]);

			d[0] = _SWARM_STATUS;
			currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_SWARM_STATUS, NDataType.UINT8, 1, d);
			NGlobals.lPrint("_SWARM_STATUS: " + d[0]);

			d[0] = _UGROOVE_STATUS;
			currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_UGROOVE_STATUS, NDataType.UINT8, 1, d);
			NGlobals.lPrint("_UGROOVE_STATUS: " + d[0]);

			currentClient.setButtonInitStatus((byte)1);
			// tempString = new String("CNT:" + clientCount);
			// clients[cNum].send((byte)NAppID.MONITOR, tempString);
			// NGlobals.lPrint("  Sending " + tempString + " to MONITOR client [" + cNum + "] from SERVER")
		}

		// 2 ---- WRITE -----------------------------------------------------------------
		// ------------------------------------------------------------------------------

		// ====================================================================================================R
		// BEGIN Main data routing code
		//
		//    each    if (incAppID ==   )    block below corresponds to a single app's input data GRAIN
		//    depending on who is sending us data
		//    we cycle through all (or a subset of) clients and send data out
		//
		// ====================================================================================================R

		NGlobals.sPrint("===== WRITING =====");

		if (incAppCmd != NCommand.REGISTER) {

			// TODO:  put this into a mysql database

			if (incAppID == NAppID.INSTRUCTOR_PANEL) {

				// INSTRUCTOR PANEL sending button status 

				// update button status variables
				if (incAppCmd == NCommand.SET_DISCUSS_STATUS) {
					_DISCUSS_STATUS = myGrain.bArray[0];
				}
				else if (incAppCmd == NCommand.SET_CLOUD_STATUS) {
					_CLOUD_STATUS = myGrain.bArray[0];
				}
				else if (incAppCmd == NCommand.SET_POLL_STATUS) {
					_POLL_STATUS = myGrain.bArray[0];
				}
				else if (incAppCmd == NCommand.SET_MOSAIC_STATUS) {
					_MOSAIC_STATUS = myGrain.bArray[0];
				}
				else if (incAppCmd == NCommand.SET_SWARM_STATUS) {
					_SWARM_STATUS = myGrain.bArray[0];
				}
				else if (incAppCmd == NCommand.SET_UGROOVE_STATUS) {
					_UGROOVE_STATUS = myGrain.bArray[0];
				}

				// send button status to clients
				for (int c = 0; c < clientCount; c++) {
					// Get the client off the master list
					currentClient = clients[c];

					// send data to ===> INSTRUCTOR PANEL - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

					if ((currentClient.getAppID() == NAppID.INSTRUCTOR_PANEL) && (currentClient.getThreadID() != THREAD_ID)) {

						//Set button states
						if (incAppCmd == NCommand.SET_DISCUSS_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_CLOUD_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_POLL_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_MOSAIC_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_SWARM_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_UGROOVE_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
					}

					// send data to ===> STUDENT PANEL - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
					if (currentClient.getAppID() == NAppID.BINDLE) {

						//Set button states
						if (incAppCmd == NCommand.SET_DISCUSS_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_CLOUD_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_POLL_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_MOSAIC_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_SWARM_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
						else if (incAppCmd == NCommand.SET_UGROOVE_STATUS) {
							NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
							myGrain.print();
							// Write the data out
							currentClient.threadSand.sendGrain(myGrain);
						}
					}
				}	
			}
		}

		// =========== MAIN ROUTING LOGIC ============================================================
		// ===========================================================================================
		// ===========================================================================================

		// =============== DISCUSS CLIENTS ====================

		if ((incAppID == NAppID.DISCUSS) || (incAppID == NAppID.INSTRUCTOR_DISCUSS) || (incAppID == NAppID.DISCUSS_PROMPT)) {

			for (int c = 0; c < clientCount; c++) {
				// Get the client off the master list
				currentClient = clients[c];
				if (currentClient.getAppID() == NAppID.BINDLE || 
						currentClient.getAppID() == NAppID.INSTRUCTOR_PANEL)
					NGlobals.sPrint("Sending BINDLE:DISCUSS/INSTRUCTOR_DISCUSS/DISCUSS_PROMPT (" + THREAD_ID + " to ---> DISCUSS/INSTRUCTOR_DISCUSS");
				currentClient.threadSand.sendGrain(myGrain);
			}
		}


		if (incAppID == NAppID.CLOUD_CHAT) {
			for (int c = 0; c < clientCount; c++) {
				// Get the client off the master list
				currentClient = clients[c];
				if (currentClient.getAppID() == NAppID.INSTRUCTOR_PANEL || 
						currentClient.getAppID() == NAppID.CLOUD_DISPLAY) 
					NGlobals.sPrint("Sending BINDLE:CLOUD_DISPLAY (" + THREAD_ID + " to ---> CLOUD_DISPLAY");
					currentClient.threadSand.sendGrain(myGrain);
				
			}
		}
		if (incAppID == NAppID.CLOUD_PROMPT) {

			for (int c = 0; c < clientCount; c++) {
				// Get the client off the master list
				currentClient = clients[c];
				if ((currentClient.getAppID() == NAppID.BINDLE) || (currentClient.getAppID() == NAppID.CLOUD_PROMPT)) {
					NGlobals.sPrint("Sending CLOUD_PROMPT (" + THREAD_ID + " to ---> CLOUD_CHAT/CLOUD_PROMPT");
					currentClient.threadSand.sendGrain(myGrain);
				}
			}
		}

		if (incAppID == NAppID.STUDENT_POLL ) {

			for (int c = 0; c < clientCount; c++) {
				// Get the client off the master list
				currentClient = clients[c];
				if ((currentClient.getAppID() == NAppID.TEACHER_POLL) || (currentClient.getAppID() == NAppID.DISPLAY_POLL)) {
					NGlobals.sPrint("Sending STUDENT_POLL (" + THREAD_ID + " to ---> TEACHER_POLL/DISPLAY_POLL");
					currentClient.threadSand.sendGrain(myGrain);
				}
			}
		}

		if (incAppID == NAppID.TEACHER_POLL) {

			for (int c = 0; c < clientCount; c++) {
				// Get the client off the master list
				currentClient = clients[c];
				if ((currentClient.getAppID() == NAppID.BINDLE) || (currentClient.getAppID() == NAppID.DISPLAY_POLL)) {
					NGlobals.sPrint("Sending TEACHER_POLL (" + THREAD_ID + " to ---> STUDENT_POLL/DISPLAY_POLL");
					currentClient.threadSand.sendGrain(myGrain);
				}
			}
		}

		//****STK TODO: Sound Mosaic routing code here


		// incoming appID ===> SOUND SWARM - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		if (incAppID == NAppID.SOUND_SWARM) {
			for (int c = 0; c < clientCount; c++) {
				// Get the client off the master list
				currentClient = clients[c];

				if (currentClient.getAppID() == NAppID.SOUND_SWARM_DISPLAY) {
					NGlobals.sPrint("Sending SOUND_SWARM:THREAD_ID to ---> SOUND_SWARM_DISPLAY: " + THREAD_ID);
					int[] x = new int[1];
					x[0] = THREAD_ID;
					currentClient.threadSand.sendGrain(myGrain.appID, NCommand.SEND_THREAD_ID, NDataType.INT, 1, x);
					NGlobals.sPrint("Sending SOUND_SWARM: x/y coordinates\n");
					currentClient.threadSand.sendGrain(myGrain);
					myGrain.print();
				}

			}
		}

		if (incAppID == NAppID.INSTRUCT_EMRG_SYNTH_PROMPT) {

			for (int c = 0; c < clientCount; c++) {
				// Get the client off the master list
				currentClient = clients[c];
				if (currentClient.getAppID() == NAppID.BINDLE) {
					NGlobals.sPrint("Sending INSTRUCT_EMRG_SYNTH_PROMPT (" + THREAD_ID + " to ---> STUD_EMRG_SYNTH");
					currentClient.threadSand.sendGrain(myGrain);
				}
			}
		}

		// BROADCAST MODE =============================================================
		// 
		// - disabled, swap comments below to re-enable
		// - NOTE: better to add specific logic as per above
		//

		// else {  // this enables BROADCAST MODE

		else if (false) {   // this disables BROADCAST MODE 

			NGlobals.sPrint("===> sending PASSTHROUGH network data");
			for (int c = 0; c < clientCount; c++) {

				// Get the client off the master list
				currentClient = clients[c];
				NGlobals.sPrint("===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));

				// Extra step for SOUND_SWARM_DISPLAY: need to send THREAD_ID


				myGrain.print();
				// Write the data out
				currentClient.threadSand.sendGrain(myGrain);

			}   
			// END --------------------------------------------------------------------
			NGlobals.sPrint("handle(DONE) " + THREAD_ID + ":" + printID((byte)myGrain.appID));

			// Free up memory
			if (myGrain != null) {
				myGrain = null;
			}
		}


		// =====================================================================================================
		// END main data routing code --- handle() fn
		// =====================================================================================================
	}

	public synchronized void remove(int THREAD_ID) {  
		int pos = clientThreadNum[THREAD_ID];
		int tID;
		if (pos >= 0) {  
			clientThreadNum[THREAD_ID] = -1;
			NomadServerThread toTerminate = clients[pos];

			if (pos < clientCount-1) {
				NGlobals.sPrint("Removing client thread " + THREAD_ID + " at " + pos);
				for (int i = pos+1; i < clientCount; i++) {
					clients[i-1] = clients[i];
					tID = clients[i-1].getThreadID();
					clientThreadNum[tID] = (short)(i-1);
				}
			}
			clientCount--;
			try {  
				toTerminate.close(); 
				toTerminate.stop(); 
			}
			catch(IOException ioe) {  
				NGlobals.sPrint("  Error closing thread: " + ioe); 
			}
		}
	}

	private  synchronized void addThread(Socket socket) {  
		int tID;
		NGlobals.sPrint("addThread(" + socket + ")");

		String IP = new String((socket.getInetAddress()).getHostAddress());

		NGlobals.sPrint("     clientCount = " + clientCount);
		NGlobals.sPrint("     clients.length = " + clients.length);

		if (clientCount < clients.length) {  
			NGlobals.sPrint("  Client accepted: " + socket);
			NGlobals.sPrint("  IP = " + IP);

			clients[clientCount] = new NomadServerThread(this, socket);
			try {  
				clients[clientCount].open(); 
				clients[clientCount].setIP(IP);
				tID = clients[clientCount].getThreadID();
				clientThreadNum[tID] = (short)clientCount;
				clients[clientCount].start();  

				NGlobals.sPrint("  Client added to lookup array at slot # " + clientCount);
				clientCount++; 
			}
			catch(IOException ioe) {  
				NGlobals.sPrint("    Error opening thread: " + ioe); 
			} 
		}
		else
			NGlobals.sPrint("  Client refused: maximum " + clients.length + " reached.");
	}

	public static void main(String args[]) {  
		NomadServer server = null;
		if (args.length != 1)
			NGlobals.sPrint("Usage: java NomadServer port");
		else
			server = new NomadServer(Integer.parseInt(args[0]));
	}

}
