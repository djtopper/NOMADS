import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Object.*;
import nomads.v210_auk.*;

public class NomadServer implements Runnable {  

    int MAX_THREADS = 5000;
    int MAX_DISP_THR = 500;
    int MAX_THREAD_IDS = 100000;
    int MAX_IPS = 2000;

    private NomadServerThread clientList[] = new NomadServerThread[MAX_THREADS];
    private NomadServerThread mainDisplayClientList[] = new NomadServerThread[MAX_DISP_THR];
    private NomadServerThread currentClient;
    private short clientNumFromThreadID[] = new short[MAX_THREAD_IDS];

    private String IPsLoggedIn[] = new String[MAX_IPS];
    private String users[] = new String[MAX_IPS];

    private ServerSocket server = null;
    private Thread       thread = null;
    private int clientCount = 0;
    private int mainDisplayClientCount = 0;
    private int IPCount = 0;

    private int userCount = 0;
    private int eventNum = 0;
    private static int debugLine = 0;
    private static String[] children;
    private Calendar cal;
    long nowT,appT,diffT,lagT;

    private static byte _DISCUSS_STATUS = 0;
    private static byte _CLOUD_STATUS = 0;
    private static byte _POINTER_STATUS = 0;
    private static byte _DROPLET_STATUS = 0;
    private static int _DROPLET_VOLUME = 100;
    private static byte _CLOUD_SOUND_STATUS = 0;
    private static int _CLOUD_SOUND_VOLUME = 100;
    private static byte _POINTER_TONE_STATUS = 0;
    private static int _POINTER_TONE_VOLUME = 100;
    private static int _SYNTH_VOLUME = 100;
    private String _SEND_PROMPT_ON = new String ("Auksalaq NOMADS");
    private ArrayList<String> discussStringCached = new ArrayList<String>(Arrays.asList(" "));
    private static int MAX_CACHED_DISCUSS_STRINGS = 15;
	



    int iDay;
    FileOutputStream out; // declare a file output object
    PrintStream p; // declare a print stream object

    NGrain myGrain;

    private String printID (byte id) {
	String[] idList = new String[255];
	int i;
	for(i=0;i<255;i++) {
	    idList[i] = null;
	}

	// Populate the list

	idList[NAppID.SERVER] = new String("SERVER");
	idList[NAppID.CONDUCTOR_PANEL] = new String("CONDUCTOR_PANEL");
	idList[NAppID.OPERA_MAIN] = new String("OPERA_MAIN");
	idList[NAppID.OPERA_CLIENT] = new String("OPERA_CLIENT");
	idList[NAppID.OC_DISCUSS] = new String("OC_DISCUSS");
	idList[NAppID.OC_CLOUD] = new String("OC_CLOUD");
	idList[NAppID.OC_LOGIN] = new String("OC_LOGIN");
	idList[NAppID.OC_POINTER] = new String("OC_POINTER");
	idList[NAppID.JOC_POINTER] = new String("OC_POINTER");

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
	for (int i=0;i<MAX_THREAD_IDS;i++) {
	    clientNumFromThreadID[i] = -1;
	}
	for (int i=0;i<MAX_DISP_THR;i++) {
	    mainDisplayClientList[i] = null;
	}
	for (int i=0;i<MAX_THREADS;i++) {
	    clientList[i] = null;
	}

	for (int i=0;i<MAX_IPS;i++) {
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
	int tMDNum;

	int incIntData[] = new int[1000];
	byte incByteData[] = new byte[1000];

	byte incAppCmd, incAppDataType;
	int incAppDataLen;
	byte incAppID;

	NGrain inGrain;

	NGlobals.sPrint("-----------------------------------------------------[" + debugLine++ + "]");

	// Do the following for EACH client

	// 1 ---- READ ============================================================================================

	NGlobals.sPrint("===== READING =====");

	// Read in relevant SAND header info
	incAppID = myGrain.appID;
	incAppCmd = myGrain.command;
	incAppDataType = myGrain.dataType;
	incAppDataLen = myGrain.dataLen;

	NGlobals.sPrint("appID: " + incAppID);

	// Print out at the SERVER level
	if (false) {
	    NGlobals.sPrint("appID: " + incAppID);
	    NGlobals.sPrint("command: " + incAppCmd);
	    NGlobals.sPrint("dataType: " + incAppDataType);
	    NGlobals.sPrint("dataLen: " + incAppDataLen);
	}
	//myGrain.print();

	// Thread admin stuff ---------------------------------------------------------------------

	// Get client number of inc client
	tCNum = clientNumFromThreadID[THREAD_ID];

	if (tCNum < 0) {
	    NGlobals.sPrint("   ERROR:  client thread not found.");
	    // TODO:  send the bye command!!!
	    remove(THREAD_ID);
	    return;
	}

	currentClient = clientList[tCNum];

	// Login and THREAD registration ----------------------------------------------------------

	// 1: check if client thread is registered
	//    if not reg, REGISTER the client's appID with the SERVER client thread
	//
	//    otherwise KICK if not registering as the very first thing
	//

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
		if (incAppID == NAppID.OPERA_MAIN) {

		    if (mainDisplayClientCount < mainDisplayClientList.length) {  
			NGlobals.sPrint("  ################ Adding client to OPERA_MAIN cache @ " + mainDisplayClientCount);
			mainDisplayClientList[mainDisplayClientCount] = currentClient;
			mainDisplayClientCount++; 
		    }
		    else {
			NGlobals.sPrint("  ERROR:  current client > MAX_DISPLAY_THREADS, client NOT added to DISPLAY cache");
		    }
		}
	    }
	}
	
	// 2: INIT =================================================================================================================
	
	// TODO:  what other apps need init?  EG., OperaMain display too? - to set last state in case of a crash
	IP = currentClient.getIP();   	// Grab IP (more for data logging)

	// INIT for CONDUCTOR_PANEL - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - WI

	currentClient = clientList[tCNum];
	if ((currentClient.getAppID() == NAppID.CONDUCTOR_PANEL) && (currentClient.getButtonInitStatus() == 0)) {

	    NGlobals.lPrint("  Sending button states to CONDUCTOR PANEL from SERVER.");
	    byte d[] = new byte[1];

	    d[0] = _DISCUSS_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_DISCUSS_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_DISCUSS_STATUS: " + d[0]);

	    d[0] = _CLOUD_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_CLOUD_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_CLOUD_STATUS:  " + d[0]);

	    d[0] = _POINTER_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_POINTER_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_POINTER_STATUS:  " + d[0]);

	    int ix[] = new int[1];
	    ix[0] = _DROPLET_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_DROPLET_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_DROPLET_VOLUME:  " + ix[0]);

	    d[0] = _DROPLET_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_DROPLET_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_DROPLET_STATUS:  " + d[0]);

	    d[0] = _CLOUD_SOUND_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_CLOUD_SOUND_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_CLOUD_SOUND_STATUS:  " + d[0]);

	    ix[0] = _CLOUD_SOUND_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_CLOUD_SOUND_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_CLOUD_SOUND_VOLUME:  " + ix[0]);
			
	    d[0] = _POINTER_TONE_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_POINTER_TONE_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_POINTER_TONE_STATUS:  " + d[0]);

	    ix[0] = _POINTER_TONE_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_POINTER_TONE_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_POINTER_TONE_VOLUME:  " + ix[0]);

	    ix[0] = _SYNTH_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.SET_SYNTH_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_SYNTH_VOLUME:  " + ix[0]);
			
	    String tString = _SEND_PROMPT_ON;
	    byte[] tStringAsBytes = tString.getBytes();
	    int tLen = _SEND_PROMPT_ON.length();
	    currentClient.threadSand.sendGrain(NAppID.SERVER, (byte)NCommand.SEND_PROMPT_ON, (byte)NDataType.CHAR, tLen, tStringAsBytes);
	    NGlobals.lPrint("ACP: Prompt " + tString + " sent"); 

			
	    currentClient.setButtonInitStatus((byte)1);
	    // tempString = new String("CNT:" + clientCount);
	    // clientList[cNum].send((byte)NAppID.MONITOR, tempString);
	    // NGlobals.lPrint("  Sending " + tempString + " to MONITOR client [" + cNum + "] from SERVER");

			
			
	}

	// INIT for OPERA_CLIENT - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - WI
	currentClient = clientList[tCNum];
	if ((incAppID == NAppID.OPERA_CLIENT) && (currentClient.getButtonInitStatus() == 0)) {
	    NGlobals.lPrint("  Sending button states to OPERA CLIENT from SERVER / CONDUCTOR_PANEL.");
	    byte d[] = new byte[1];

	    d[0] = _DISCUSS_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_DISCUSS_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_DISCUSS_STATUS: " + d[0]);

	    d[0] = _CLOUD_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_CLOUD_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_CLOUD_STATUS:  " + d[0]);

	    d[0] = _POINTER_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_POINTER_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_POINTER_STATUS:  " + d[0]);

	    int ix[] = new int[1];
	    ix[0] = _DROPLET_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_DROPLET_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_DROPLET_VOLUME:  " + ix[0]);

	    d[0] = _DROPLET_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_DROPLET_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_DROPLET_STATUS:  " + d[0]);

	    d[0] = _CLOUD_SOUND_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_CLOUD_SOUND_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_CLOUD_SOUND_STATUS:  " + d[0]);

	    ix[0] = _CLOUD_SOUND_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_CLOUD_SOUND_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_CLOUD_SOUND_VOLUME:  " + ix[0]);
			
	    d[0] = _POINTER_TONE_STATUS;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_POINTER_TONE_STATUS, NDataType.UINT8, 1, d);
	    NGlobals.lPrint("_POINTER_TONE_STATUS:  " + d[0]);

	    ix[0] = _POINTER_TONE_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_POINTER_TONE_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_POINTER_TONE_VOLUME:  " + ix[0]);
			
	    String tString = _SEND_PROMPT_ON;
	    byte[] tStringAsBytes = tString.getBytes();
	    int tLen = _SEND_PROMPT_ON.length();
	    currentClient.threadSand.sendGrain(NAppID.SERVER, (byte)NCommand.SEND_PROMPT_ON, (byte)NDataType.CHAR, tLen, tStringAsBytes);
	    NGlobals.lPrint("ACP: Prompt " + tString + " sent"); 

	    //Cached Discuss String
	    for (int i=0;i<discussStringCached.size();i++) {
		tString = discussStringCached.get(i);
		tStringAsBytes = tString.getBytes();
		tLen = tString.length();
		currentClient.threadSand.sendGrain(NAppID.SERVER, (byte)NCommand.SEND_CACHED_DISCUSS_STRING, (byte)NDataType.CHAR, tLen, tStringAsBytes);
		NGlobals.lPrint("ACP: Cached Discuss String " + tString + " arrayElt# " + i + " sent");
	    }
			
	    currentClient.setButtonInitStatus((byte)1);
	}

	// INIT for OPERA_MAIN - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - WI

	// HACKISH - sends message back to server, mimicking a send from the CONDUCTOR_PANEL
	// effectively getting the message back from the server as if the CP had sent it

	// TOP

	// FIX:  just have the cached synth volume sent when an OM connects (along with the other attributes like alpha, etc.. perhaps even cached text and cloud)

	currentClient = clientList[tCNum];
	
	if (incAppID == NAppID.OPERA_MAIN) {
	    NGlobals.lPrint("  Sending button states to OPERA MAIN from SERVER / CONDUCTOR_PANEL.");
	    int ix[] = new int[1];
	    ix[0] = _SYNTH_VOLUME;
	    currentClient.threadSand.sendGrain(NAppID.CONDUCTOR_PANEL, NCommand.SET_SYNTH_VOLUME, NDataType.INT32, 1, ix);
	    NGlobals.lPrint("_SYNTH_VOLUME:  " + ix[0]);
	}


	// ====================================================================================================W
	// BEGIN Main data routing code
	//
	//    each    if (incAppID ==   )    block below corresponds to a single app's input data GRAIN
	//    depending on who is sending us data
	//    we cycle through all (or a subset of) clientList and send data out
	//
	// ====================================================================================================W

	// 3 ---- WRITE ===========================================================================================================

	NGlobals.sPrint("===== WRITING =====");

	// FILTER out some commands that don't need to go past this point

	if (incAppCmd != NCommand.REGISTER) {

	    // TODO:  add code to cache the button statuses
	    //        IDEA:  use button_status[array]

	    // incoming appID = CONDUCTOR_PANEL = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

	    NGlobals.dtPrint("   incAppID == " + printID((byte)currentClient.getAppID()));


	    if (incAppID == NAppID.CONDUCTOR_PANEL) {
		// scroll through all clientList // TODO: FIX: SPEEDUP: change to separate APPID[client] arrays

		// Store various FEATURE STATES - - - - - - - - - - - - - - - - -

		if (incAppCmd == NCommand.SET_DISCUSS_STATUS) {
		    _DISCUSS_STATUS = myGrain.bArray[0];
		}

		else if (incAppCmd == NCommand.SET_CLOUD_STATUS) {
		    _CLOUD_STATUS = myGrain.bArray[0];
		}

		else if (incAppCmd == NCommand.SET_POINTER_STATUS) {
		    _POINTER_STATUS = myGrain.bArray[0];
		}

		else if (incAppCmd == NCommand.SET_DROPLET_STATUS) {
		    _DROPLET_STATUS = myGrain.bArray[0];
		}

		else if (incAppCmd == NCommand.SET_DROPLET_VOLUME) {
		    _DROPLET_VOLUME = myGrain.iArray[0];
		}

		else if (incAppCmd == NCommand.SET_CLOUD_SOUND_STATUS) {
		    _CLOUD_SOUND_STATUS = myGrain.bArray[0];
		}

		else if (incAppCmd == NCommand.SET_CLOUD_SOUND_VOLUME) {
		    _CLOUD_SOUND_VOLUME = myGrain.iArray[0];
		}
				
		else if (incAppCmd == NCommand.SET_POINTER_TONE_STATUS) {
		    _POINTER_TONE_STATUS = myGrain.bArray[0];
		}

		else if (incAppCmd == NCommand.SET_POINTER_TONE_VOLUME) {
		    _POINTER_TONE_VOLUME = myGrain.iArray[0];
		}

		else if (incAppCmd == NCommand.SET_SYNTH_VOLUME) {
		    _SYNTH_VOLUME = myGrain.iArray[0];
		}
		else if (incAppCmd == NCommand.SEND_PROMPT_ON) {
		    _SEND_PROMPT_ON = new String(myGrain.bArray);
		}

		for (int c = 0; c < clientCount; c++) {
		    // Get the client off the master list
		    currentClient = clientList[c];


		    if (currentClient.getAppID() == NAppID.CONDUCTOR_PANEL) {
			if (incAppCmd == NCommand.SEND_PROMPT_ON) {
			    NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
			    //myGrain.print();
			    // Write the data out
			    currentClient.threadSand.sendGrain(myGrain);
			}
			else if (incAppCmd == NCommand.SEND_PROMPT_OFF) {
			    NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
			    //myGrain.print();
			    // Write the data out
			    currentClient.threadSand.sendGrain(myGrain);
			}
		    }


		    // send data to ===> OPERA CLIENT - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		    if (currentClient.getAppID() == NAppID.OPERA_CLIENT) {
			if ((incAppCmd != NCommand.SET_CLOUD_ALPHA) && 
			    (incAppCmd != NCommand.SET_DISCUSS_ALPHA) &&
			    (incAppCmd != NCommand.SET_POINTER_ALPHA)) {

			    NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
			    //myGrain.print();
			    // Write the data out
			    currentClient.threadSand.sendGrain(myGrain);
			}
		    }

		    // send data to ===> OPERA CLIENT - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		    if (currentClient.getAppID() == NAppID.OPERA_MAIN) {
			if ((incAppCmd != NCommand.SET_DROPLET_VOLUME) && 
			    (incAppCmd != NCommand.SET_DROPLET_STATUS)) {
			    NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
			    //myGrain.print();
			    // Write the data out
			    currentClient.threadSand.sendGrain(myGrain);
			}
		    }

		}
	    }   

	    // incoming appID = OC_DISCUSS = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

	    else if (incAppID == NAppID.OC_DISCUSS) {

		if (incAppCmd == NCommand.SEND_MESSAGE && (_DISCUSS_STATUS == 1)) {
			    	
		    //Sets cached discuss strings from input
		    discussStringCached.add(new String(myGrain.bArray));
		    if (discussStringCached.size() > MAX_CACHED_DISCUSS_STRINGS) {
			discussStringCached.remove(0);
		    }
			    	
		    // scroll through all clientList // TODO: FIX: SPEEDUP: change to separate APPID[client] arrays
		    for (int c = 0; c < clientCount; c++) {
			// Get the client off the master list
			currentClient = clientList[c];

			// send data to ===> OPERA CLIENT - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if (currentClient.getAppID() == NAppID.OPERA_CLIENT) {
			    NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
			    //myGrain.print();
			    // Write the data out
			    currentClient.threadSand.sendGrain(myGrain);
			}

			// send data to ===> OPERA MAIN - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			else if (currentClient.getAppID() == NAppID.OPERA_MAIN) {
			    NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
			    //myGrain.print();
			    // Write the data out
			    currentClient.threadSand.sendGrain(myGrain);
			}

		    }
		}   
	    }

	    // incoming appID = OC_CLOUD = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

	    else if (incAppID == NAppID.OC_CLOUD && (_CLOUD_STATUS == 1)) {

		if (incAppCmd == NCommand.SEND_MESSAGE) {

		    // (see below)
		    // TODO: FIX: SPEEDUP: change to separate APPID[client] arrays
		    // scroll through all clientList 
		    // for (int c = 0; c < clientCount; c++) {
		    // 	// Get the client off the master list
		    // 	currentClient = clientList[c];

		    // 	// send data to ===> OPERA MAIN - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		    // 	if (currentClient.getAppID() == NAppID.OPERA_MAIN) {
		    // 	    NGlobals.sPrint("   sending to ===> client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
		    // 	    //myGrain.print();
		    // 	    // Write the data out
		    // 	    currentClient.threadSand.sendGrain(myGrain);
		    // 	}

		    // }

		    // FIXED: SPEDUP: changed to separate APPID[client] arrays
		    for (int c = 0; c < mainDisplayClientCount; c++) {
			// Get the client off the master list
			NGlobals.sPrint("   sending to ===> display client[" + c + "]");
			currentClient = mainDisplayClientList[c];
			NGlobals.sPrint("   w/ appID = " + printID((byte)currentClient.getAppID()));
			currentClient.threadSand.sendGrain(myGrain);
		    }

		}

	    }   
	    
	    // incoming appID = OC_POINTER = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
	    
	    else if ((incAppID == NAppID.OC_POINTER || incAppID == NAppID.JOC_POINTER) && (_POINTER_STATUS == 1)) {
		if (incAppCmd == NCommand.SEND_SPRITE_XY) {

		    // FIXED: SPEDUP: changed to separate APPID[client] arrays
		    for (int c = 0; c < mainDisplayClientCount; c++) {
			// Get the client off the master list
			currentClient = mainDisplayClientList[c];
			NGlobals.sPrint("   sending to ===> display client[" + c + "] w/ appID = " + printID((byte)currentClient.getAppID()));
			currentClient.threadSand.sendGrain(myGrain);
		    }
		

		    // OLD METHOD 

		    // for (int c = 0; c < clientCount; c++) {
		    // 	currentClient = clientList[c];
		    // 	// NGlobals.sPrint("===> client[" + c + "] w/ id = " + currentClient.getAppID());
		    // 	// send out to SOUND_SWARM_DISPLAY - - - - - - - - - - - - - - - - - - - - - - - - -
		    // 	if (currentClient.getAppID() == NAppID.OPERA_MAIN) {
		    // 	    NGlobals.sPrint("Sending SOUND_SWARM:THREAD_ID to ---> OPERA_MAIN: " + THREAD_ID);
		    // 	    // CUSTOM DATA PACKING into 3 ints: THREAD_ID, x, y
		    // 	    int[] x = new int[3];
		    // 	    x[0] = THREAD_ID;
		    // 	    x[1] = myGrain.iArray[0];
		    // 	    x[2] = myGrain.iArray[1];
		    // 	    currentClient.threadSand.sendGrain(incAppID, NCommand.SEND_SPRITE_THREAD_XY, NDataType.INT32, 3, x);
		    // 	}
		    // }

		}
	    }

	    else if (false) {
		NGlobals.sPrint("===> sending PASSTHROUGH network data");
		for (int c = 0; c < clientCount; c++) {

		    // Get the client off the master list
		    currentClient = clientList[c];
		    NGlobals.sPrint("===> client[" + c + "] w/ appID = " + currentClient.getAppID());

		    // Extra step for SOUND_SWARM_DISPLAY: need to send THREAD_ID


		    //myGrain.print();
		    // Write the data out
		    currentClient.threadSand.sendGrain(myGrain);

		}   
		// END --------------------------------------------------------------------
		NGlobals.sPrint("handle(DONE) " + THREAD_ID + ":" + myGrain.appID);

		// Free up memory
		if (myGrain != null) {
		    myGrain = null;
		}
	    }
	}
    }    
    // =====================================================================================================
    // END main data routing code --- handle() fn
    // =====================================================================================================

    public synchronized void remove(int THREAD_ID) {  
	int pos = clientNumFromThreadID[THREAD_ID];
	int tID;
	

	// SEND REMOVE/DELETE TO SWARM DISPLAY CLIENTS ---------------------------------------------------

	for (int c = 0; c < clientCount; c++) {
	    
	    currentClient = clientList[c];
	    NGlobals.sPrint("===> client[" + c + "] w/ id = " + currentClient.getAppID());
	    
	    
	    // send out to SOUND_SWARM_DISPLAY - - - - - - - - - - - - - - - - - - - - - - - - -
	    if ((currentClient.getAppID() == NAppID.OPERA_MAIN) && (currentClient.getThreadID() != THREAD_ID)) {
		NGlobals.sPrint("Sending DELETE_SPRITE ---> OPERA_MAIN: " + THREAD_ID);
		
		// CUSTOM DATA PACKING into 3 ints: THREAD_ID, x, y
		int[] x = new int[1];
		x[0] = THREAD_ID;
		
		currentClient.threadSand.sendGrain(NAppID.SERVER, NCommand.DELETE_SPRITE, NDataType.INT32, 1, x);
		
	    }
	}

	// remove from lists ---------------------------------------------------

	if (pos >= 0) {  
	    clientNumFromThreadID[THREAD_ID] = -1;
	    NomadServerThread toTerminate = clientList[pos];

	    // rem from main display list
	    if (pos < mainDisplayClientCount-1) {
		NGlobals.sPrint("Removing thread from MAIN_DISPLAY cache" + THREAD_ID + " at " + pos);
		for (int i = pos+1; i < mainDisplayClientCount; i++) {
		    mainDisplayClientList[i-1] = mainDisplayClientList[i];
		    tID = mainDisplayClientList[i-1].getThreadID();
		}
	    }
	    mainDisplayClientCount--;

	    // rem from client list
	    if (pos < clientCount-1) {
		NGlobals.sPrint("Removing client thread " + THREAD_ID + " at " + pos);
		for (int i = pos+1; i < clientCount; i++) {
		    clientList[i-1] = clientList[i];
		    tID = clientList[i-1].getThreadID();
		    clientNumFromThreadID[tID] = (short)(i-1);
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
	NomadServerThread tHolder;
	NGlobals.sPrint("addThread(" + socket + ")");

	String IP = new String((socket.getInetAddress()).getHostAddress());

	NGlobals.sPrint("     clientCount = " + clientCount);
	NGlobals.sPrint("     clientList.length = " + clientList.length);

	if (clientCount < clientList.length) {  
	    NGlobals.sPrint("  Client accepted, adding to Global Client List: " + socket + " w/IP " + IP);
	    tHolder = new NomadServerThread(this, socket);
	    clientList[clientCount] = tHolder;
	    try {  
		clientList[clientCount].open(); 
		clientList[clientCount].setIP(IP);
		tID = clientList[clientCount].getThreadID();
		clientNumFromThreadID[tID] = (short)clientCount;
		clientList[clientCount].start();  
		NGlobals.sPrint("  Client added to lookup array at slot # " + clientCount);
		clientCount++; 
	    }
	    catch(IOException ioe) {  
		NGlobals.sPrint("    Error opening thread: " + ioe); 
	    } 


	}
	else
	    NGlobals.sPrint("  Client refused: maximum " + clientList.length + " reached.");
    }

    public static void main(String args[]) {  
	NomadServer server = null;
	if (args.length != 1)
	    NGlobals.sPrint("Usage: java NomadServer port");
	else
	    server = new NomadServer(Integer.parseInt(args[0]));
    }

}

