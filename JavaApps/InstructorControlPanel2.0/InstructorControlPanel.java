//
// NOMADS Instructor Control Panel
//

import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import nomads.v210.*;
import javax.imageio.ImageIO;


public class InstructorControlPanel extends JApplet  implements  ActionListener, Runnable{

    NSand instructorControlPanelSand;
    private NomadsAppThread nThread;
    private NomadsErrCheckThread nECThread;
    
    int mSecLimit=1000;
    int errTrip=20;

    JCheckBoxMenuItem audioMenu_SoundOn;
    JCheckBoxMenuItem audioMenu_SoundOff;

    JCheckBoxMenuItem pointerMenu_MapBackground;
    JCheckBoxMenuItem pointerMenu_BlackBackground;

    JMenuItem pollMenu_VoteAgain ;
    JMenuItem pollMenu_ResetScreen;
    JMenuItem pollMenu_PollMode;

    JCheckBoxMenuItem  pollMenu_ShowAnswer;
    JCheckBoxMenuItem  pollMenu_ShowStats;
    JCheckBoxMenuItem  pollMenu_ShowQuestion;

    JButton joinButton, discussButton, cloudButton,  pollButton, uGrooveButton;
    JButton discussDisplayButton, cloudDisplayButton, pollDisplayButton, uGrooveDisplayButton;
    JButton discussPromptButton, cloudPromptButton, pollPromptButton, uGroovePromptButton;
    JButton mosaicButton, mosaicDisplayButton;
    JButton pointerButton, pointerDisplayButton, pointerPropmtButton;
    JLabel blankButton, blankButton2;

    Image discussPromptImg, discussDisplayImg, discussImgOn, discussImgOff;
    Image cloudPromptImg, cloudDisplayImg, cloudImgOn, cloudImgOff;
    Image pollPromptImg, pollDisplayImg, pollImgOn, pollImgOff;
    Image mosaicImgOff, mosaicImgOn, mosaicPromptImg, mosaicDisplayImg;
    Image pointerDisplayImg, pointerPromptImg, pointerImgOn, pointerImgOff;
    Image uGroovePromptImg, uGrooveDisplayImg, uGrooveImgOff, uGrooveImgOn;

    ImageIcon discussIcon, discussPromptIcon, discussDisplayIcon;
    ImageIcon cloudIcon, cloudPromptIcon, cloudDisplayIcon;
    ImageIcon pollIcon, pollPromptIcon, pollDisplayIcon;
    ImageIcon uGrooveIcon, uGroovePromptIcon, uGrooveDisplayIcon;
    ImageIcon mosaicIcon, mosaicDisplayIcon;
    ImageIcon pointerIcon, pointerDisplayIcon;
    ImageIcon icon;

    ImageIcon discussIconOn, pollIconOn, cloudIconOn, mosaicIconOn, pointerIconOn, uGrooveIconOn;
    ImageIcon discussIconOff, pollIconOff, cloudIconOff, mosaicIconOff, pointerIconOff, uGrooveIconOff;

    GridLayout buttonGridLayout = new GridLayout (4,3,0,0); //3rd value was set to 5
    

    int discussOnOff, cloudOnOff, pollOnOff, mosaicOnOff, pointerOnOff, uGrooveOnOff; //*****STK variables store current state of button	

    JPanel butPanel = new JPanel();
    JPanel logoPanel;
    JLabel imageLabel;

    URL discussPromptURL;
    URL discussDisplayURL;
    URL cloudPromptURL;
    URL cloudDisplayURL;
    URL pollPromptURL;
    URL pollDisplayURL;
    URL mosaicDisplayURL;
    URL pointerDisplayURL;
    URL uGroovePromptURL;
    URL uGrooveDisplayURL;
	
    //To set JPanel background images
    String imgPrefix;
    Image backgroundImg;
    URL imgWebBase;

    // begin v2.0 panels and frames --------------

    InstructorGroupDiscuss myInstructorGroupDiscussPanel;
    JFrame instructorGroupDiscussFrame;

    GroupDiscussPrompt myGroupDiscussPromptPanel;
    JFrame groupDiscussPromptFrame;

    CloudPrompt myCloudPromptPanel;
    JFrame cloudPromptFrame;

    CloudDisplay myCloudDisplayPanel;
    JFrame cloudDisplayFrame;
	
    PollPrompt myPollPromptPanel;
    JFrame pollPromptFrame;

    PollDisplay myPollDisplayPanel;
    JFrame pollDisplayFrame;

    SoundMosaicDisplay myMosaicDisplayPanel;
    JFrame mosaicDisplayFrame;

    SandPointerDisplay myPointerDisplayPanel;
    JFrame pointerDisplayFrame;

    UnityGroovePrompt myUnityGroovePromptPanel;
    JFrame unityGroovePromptFrame;
	
    UnityGroovePanel myUnityGrooveDisplayPanel;
    JFrame unityGrooveDisplayFrame;	

    // end v2.0 panels and frames ----------------
 
    int errFlag = 0;
    int lastThread = 0;
    Boolean handleActive = false;
    Boolean sandRead = false;
    Boolean connected = false;
    Boolean soundStatus = false;

    long mSecR=0;
    int resetCtr=0;
    int maxResets=1000;

    float mSecAvg=10;
    float mSecAvgL=10;

    int pToggle=0;
    private int maxSkip;

    private Object sandReadLock = new Object();

    public Boolean getSandRead() {
	synchronized (sandReadLock) {
	    return sandRead;
	}
    }

    public void setSandRead(Boolean sr) {
	synchronized (sandReadLock) {
	    sandRead = sr;
	}
    }

    private Object handleActiveLock = new Object();

    public Boolean getHandleActive() {
	synchronized(handleActiveLock) {
	    return handleActive;
	}
    }

    public void setHandleActive(Boolean ha) {
	synchronized(handleActiveLock) {
	    handleActive = ha;
	}
    }

    Thread runner;

    // DT 6/30/10:  not sure we need these anymore

    public void start() {
	runner = new Thread(this);
	runner.start();
    }

    public synchronized void run () {
	while (true) {
	    try {
		runner.sleep(1000);
	    }
	    catch (InterruptedException ie) {}
	}
    }

    private class NomadsAppThread extends Thread {
	InstructorControlPanel client; //Replace with current class name
	Calendar now;

	long handleStart=0;
	long handleEnd=1;
	long millis=0;
	Boolean runState=false;

	long mSecR=0;
	int resetCtr=0;
	int maxResets=10;
	
	float mSecAvg=10;
	float mSecAvgL=10;

	private Object handleStartLock = new Object();

	public long getHandleStart() {
	    synchronized(handleStartLock) {
		return handleStart;
	    }
	}

	public void setHandleStart(long hs) {
	    synchronized(handleStartLock) {
		handleStart = hs;
	    }
	}

	private Object handleEndLock = new Object();

	public long getHandleEnd() {
	    synchronized(handleEndLock) {
		return handleEnd;
	    }
	}

	public void setHandleEnd(long he) {
	    synchronized(handleEndLock) {
		handleEnd = he;
	    }
	}

	private Object runStateLock = new Object();

	public void setRunState(Boolean state) {
	    synchronized(runStateLock) {
		runState = state;
	    }
	}

	public Boolean getRunState() {
	    synchronized(runStateLock) {
		return runState;
	    }
	}


	public NomadsAppThread(InstructorControlPanel _client) {
	    client = _client;
	    // Connect
	}

	public synchronized void run()    {			
	    //NGlobals.dtPrint("NomadsAppThread -> run()");
	    while (getRunState() == true)  {
		now = Calendar.getInstance();
		setHandleStart(now.getTimeInMillis());
		client.handle();
		client.setHandleActive(false);
		handleEnd = now.getTimeInMillis();
		millis = getHandleEnd()-getHandleStart();
		NGlobals.dtPrint("handle() proc time:" + millis);
	    }
	}
    }

    private class NomadsErrCheckThread extends Thread {
	InstructorControlPanel client; //Replace with current class name

	public NomadsErrCheckThread(InstructorControlPanel _client) {
	    client = _client;
	}
	public synchronized void run()    {			
	    NGlobals.dtPrint("InstructorControlPanel ERRCHECKTHREAD -> run");
	    while (true)  {
		client.errCheck();
	    }
	}
    }

    // START errCheck() !!===================================!!

    public void errCheck() {
	Calendar now;
	long mSecN=0;
	long mSecH=0;
	long mSecDiff=0;

	// NGlobals.csvPrint(" . ");

	try {

	    if ((getHandleActive() == true) && (getSandRead() == true)) {

		now = Calendar.getInstance();
		mSecN = now.getTimeInMillis();
		mSecH = nThread.getHandleStart();

		mSecDiff = mSecN-mSecH;
		mSecAvg = ((mSecAvg*4)+mSecDiff)/5;
		mSecAvgL = ((mSecAvgL*19)+mSecDiff)/20;

		NGlobals.dtPrint("errCheck --> mSecDiff: " + mSecDiff + " avg: " + mSecAvg + " avgL: " + mSecAvgL);

		pToggle++;
		if (pToggle > 10) {
		    pToggle=0;
		}
		if (pToggle%2 == 0) {
		    NGlobals.dtPrint(">>> maxSkip:" + maxSkip);
		}

		if (mSecDiff > mSecLimit) {
		    errFlag += 1;
		    if (errFlag > 0) {
			NGlobals.dtPrint("   INCR ERROR COUNT: " + errFlag);
		    }
		    if ((errFlag > errTrip) && (connected == true)) {
			now = Calendar.getInstance();
			mSecR = now.getTimeInMillis(); // time of this reset
			NGlobals.dtPrint("-----> EREC #" + resetCtr);
			resetCtr++;
			if (resetCtr > maxResets) {
			    NGlobals.dtPrint("######### CRITICAL ERROR");
			    NGlobals.dtPrint(">>> #### MAX RESETS");
			    NGlobals.dtPrint(">>> sleeping 10 sec");
			    NomadsErrCheckThread.sleep(12000);
			    resetCtr=0;
			}
			nThread.setHandleStart(mSecN);
			NGlobals.dtPrint("######### NETWORK ERROR");
			// NGlobals.dtPrint(">>> handleErrCheck time diff: " + mSecDiff);
			// NGlobals.dtPrint(">>> halting thread.");
			nThread.setRunState(false);
			NomadsErrCheckThread.sleep(800);
			// deleteSynth(lastThread);
			nThread = null;
			System.out.println("   disconnecting.");
			instructorControlPanelSand.disconnect();
			NomadsErrCheckThread.sleep(800);
			instructorControlPanelSand = null;
			connected = false;
			System.out.println("   disconneced.");
			// System.out.println(">>> deleting sprites/synths.");
			// deleteAllSynths();
			// System.out.println(">>> sprites/synths deleted.");
			System.out.println("   Attempting reconnect.");
			NomadsErrCheckThread.sleep(800);
			instructorControlPanelSand = new NSand(); 
			instructorControlPanelSand.connect();

			int d[] = new int[1];
			d[0] = 0;
			instructorControlPanelSand.sendGrain((byte)NAppID.INSTRUCTOR_PANEL, (byte)NCommand.REGISTER, (byte)NDataType.UINT8, 1, d );

			connected = true;
			System.out.println("   reconnected!");			
			System.out.println("   attempting to restart thread.");			
			NomadsErrCheckThread.sleep(800);
			nThread = new NomadsAppThread(this);
			nThread.setRunState(true);
			nThread.start();

			myInstructorGroupDiscussPanel.resetSand(instructorControlPanelSand);
			myGroupDiscussPromptPanel.resetSand(instructorControlPanelSand);
			myCloudPromptPanel.resetSand(instructorControlPanelSand);
			myCloudDisplayPanel.resetSand(instructorControlPanelSand);
			myPollPromptPanel.resetSand(instructorControlPanelSand);
			myPollDisplayPanel.resetSand(instructorControlPanelSand);

			System.out.println("Thread restarted.");			
			errFlag = 0;

			now = Calendar.getInstance();
			mSecN = now.getTimeInMillis();
			nThread.setHandleStart(mSecN);

			groupDiscussPromptFrame.setVisible(false);
			instructorGroupDiscussFrame.setVisible(false);
			cloudPromptFrame.setVisible(false);
			cloudDisplayFrame.setVisible(false);
			pollPromptFrame.setVisible(false);
			pollDisplayFrame.setVisible(false);
			mosaicDisplayFrame.setVisible(false);
			pointerDisplayFrame.setVisible(false);
			unityGroovePromptFrame.setVisible(false);
			unityGrooveDisplayFrame.setVisible(false);


		    }
		}
		else if ((errFlag > 0) && (mSecDiff < mSecLimit)) {
		    errFlag--;
		    NGlobals.dtPrint(">>> DECR ERROR COUNT: " + errFlag);
		}
	    }
	    NomadsErrCheckThread.sleep(10);
	}
	catch (InterruptedException ie) {}

    }

    // END errcheck -------------------------------------------


    public void init() {
	int discussOnOff = 0;
	int cloudOnOff = 0;
	int pollOnOff = 0;
	int mosaicOnOff = 0;
	int pointerOnOff = 0;
	int uGrooveOnOff = 0;

	//============================= MENU BAR BEGIN ==============================
	JMenuBar menuBar = new JMenuBar ();
	JMenu pollMenu = new JMenu ("Poll");

	pollMenu_VoteAgain = new JMenuItem ("Vote Again");
	pollMenu.add (pollMenu_VoteAgain);
	pollMenu_VoteAgain.addActionListener (this);

	JMenu audioMenu = new JMenu ("Audio");

	audioMenu_SoundOn = new JCheckBoxMenuItem ("Pointer Sound ON");
	audioMenu.add (audioMenu_SoundOn);
	audioMenu_SoundOn.addActionListener (this);

	audioMenu_SoundOff = new JCheckBoxMenuItem ("Pointer Sound OFF");
	audioMenu.add (audioMenu_SoundOff);
	audioMenu_SoundOff.addActionListener (this);

	JMenu pointerMenu = new JMenu ("Pointer");

	pointerMenu_BlackBackground = new JCheckBoxMenuItem ("BLACK Background");
	pointerMenu_MapBackground = new JCheckBoxMenuItem ("GLOBE Background");

	pointerMenu_MapBackground.addActionListener (this);
	pointerMenu_BlackBackground.addActionListener (this);

	pointerMenu.add (pointerMenu_BlackBackground);
	pointerMenu.add (pointerMenu_MapBackground);

	menuBar.add (pollMenu);
	menuBar.add (audioMenu);
	menuBar.add (pointerMenu);


	setJMenuBar (menuBar);

	pointerMenu_BlackBackground.setEnabled(true);
	pointerMenu_BlackBackground.setSelected(true);

	pointerMenu_MapBackground.setEnabled(true);
	pointerMenu_MapBackground.setSelected(false);

	pollMenu_VoteAgain.setEnabled (true);

	audioMenu_SoundOn.setEnabled (true);
	audioMenu_SoundOn.setSelected(false);

	audioMenu_SoundOff.setEnabled (true);
	audioMenu_SoundOff.setSelected(true);

	//============================= MENU BAR END ==============================

	butPanel.setLayout(buttonGridLayout);
	Container content = getContentPane();
	content.setBackground(Color.black);
	setJMenuBar(menuBar);

	// try {
	// 	// discussPromptURL = new URL(user +  "GroupDiscussPrompt");                  	
	// 	// discussDisplayURL = new URL(user + "GroupDiscussInstructor");
	// 	// cloudPromptURL = new URL(user + "CloudPrompt");                  	
	// 	// cloudDisplayURL = new URL(user + "CloudDisplay");   
	// 	// pollPromptURL = new URL(user + "PollPrompt");       
	// 	// pollDisplayURL = new URL(user + "PollDisplay");                  	
	// 	// mosaicDisplayURL = new URL(user + "MosaicInstructor");
	// 	// pointerDisplayURL = new URL(user + "SandPointerDisplay");
	// 	// uGroovePromptURL = new URL(user + "UnityGroovePrompt");       
	// 	// uGrooveDisplayURL = new URL(user + "UnityGrooveStudent");
	// }
	// catch (MalformedURLException e) {
	// }
	imgPrefix = "http://nomads.music.virginia.edu/images/";

	try { 
	    imgWebBase = new URL(imgPrefix); 
	} 
	catch (Exception e) {}

	butPanel.setPreferredSize(new Dimension(550,225));



	instructorControlPanelSand = new NSand();
	instructorControlPanelSand.connect();
	connected = true;

	setupButtons();

	nThread = new NomadsAppThread(this);
	nThread.setRunState(true);
	nThread.start();

	nECThread = new NomadsErrCheckThread(this);
	nECThread.start();

	//Code below starts thread (connects), sends register byte
	byte d[] = new byte[1];
	d[0] = 0;

	NGlobals.dtPrint("registering...");
	instructorControlPanelSand.sendGrain((byte)NAppID.INSTRUCTOR_PANEL, (byte)NCommand.REGISTER, (byte)NDataType.UINT8, 1, d );

	
    }

    // button setup function -----------------------------------------------------------------------

    public void setupButtons() {
	int w,h;
	w = (int)(240*0.7);
	h = (int)(57*0.9);

	// Images --------------------------------

	discussImgOff=getImage(getCodeBase( ),"buttons/InstructDiscussOff.png");
	discussImgOn=getImage(getCodeBase( ),"buttons/InstructDiscussOn.png");
	discussPromptImg=getImage(getCodeBase(), "buttons/InstructDiscussPrompt.png");
	discussDisplayImg=getImage(getCodeBase(), "buttons/InstructDiscussDisplay.png");

	cloudImgOff=getImage(getCodeBase( ),"buttons/InstructCloudOff.png");
	cloudImgOn=getImage(getCodeBase( ),"buttons/InstructCloudOn.png");
	cloudPromptImg=getImage(getCodeBase(), "buttons/InstructCloudPrompt.png");
	cloudDisplayImg=getImage(getCodeBase(), "buttons/InstructCloudDisplay.png ");	

	pollImgOff=getImage(getCodeBase( ),"buttons/InstructPollOff.png");
	pollImgOn=getImage(getCodeBase( ),"buttons/InstructPollOn.png");
	pollPromptImg=getImage(getCodeBase(), "buttons/InstructPollPrompt.png");
	pollDisplayImg=getImage(getCodeBase(), "buttons/InstructPollDisplay.png ");

	mosaicImgOff=getImage(getCodeBase( ), "buttons/InstructSoundOff.png"); //InstructSoundOff.png
	mosaicImgOn=getImage(getCodeBase( ),"buttons/InstructSoundOn.png");
	mosaicPromptImg=getImage(getCodeBase( ),"buttons/InstructSoundPrompt.png");
	mosaicDisplayImg=getImage(getCodeBase( ),"buttons/InstructSoundDisplay.png"); 

	pointerImgOff=getImage(getCodeBase( ), "buttons/InstructPointerOff.png"); //
	pointerImgOn=getImage(getCodeBase( ),"buttons/InstructPointerOn.png");
	pointerPromptImg=getImage(getCodeBase( ),"buttons/InstructPointerPrompt.png");
	pointerDisplayImg=getImage(getCodeBase( ),"buttons/InstructPointerDisplay.png");

	uGrooveImgOff=getImage(getCodeBase( ), "buttons/InstructUnityOff.png"); // FIX THESE NAMES 
	uGrooveImgOn=getImage(getCodeBase( ),"buttons/InstructUnityOn.png");
	uGroovePromptImg=getImage(getCodeBase( ),"buttons/InstructUnityPrompt.png");
	uGrooveDisplayImg=getImage(getCodeBase( ),"buttons/InstructUnityDisplay.png");

	// Icons ---------------------------------

	discussIconOn = new ImageIcon(discussImgOn);
	cloudIconOn = new ImageIcon(cloudImgOn);
	pollIconOn = new ImageIcon(pollImgOn);
	mosaicIconOn = new ImageIcon(mosaicImgOn);
	pointerIconOn = new ImageIcon(pointerImgOn);
	uGrooveIconOn = new ImageIcon(uGrooveImgOn);

	discussIconOff = new ImageIcon(discussImgOff);
	cloudIconOff = new ImageIcon(cloudImgOff);
	pollIconOff = new ImageIcon(pollImgOff);
	mosaicIconOff = new ImageIcon(mosaicImgOff);
	pointerIconOff = new ImageIcon(pointerImgOff);
	uGrooveIconOff = new ImageIcon(uGrooveImgOff);

	discussPromptIcon = new ImageIcon(discussPromptImg); //STK 1_29_10
	discussDisplayIcon = new ImageIcon(discussDisplayImg);

	cloudPromptIcon = new ImageIcon(cloudPromptImg); //STK 1_29_10
	cloudDisplayIcon = new ImageIcon(cloudDisplayImg);

	pollPromptIcon = new ImageIcon(pollPromptImg); //STK 1_29_10
	pollDisplayIcon = new ImageIcon(pollDisplayImg);

	pointerDisplayIcon = new ImageIcon(pointerDisplayImg);
	mosaicDisplayIcon = new ImageIcon(mosaicDisplayImg);

	uGroovePromptIcon = new ImageIcon(uGroovePromptImg); 
	uGrooveDisplayIcon = new ImageIcon(uGrooveDisplayImg);


	// on/off button basics ------------------

	discussIcon = new ImageIcon(discussImgOff);
	discussButton = new JButton( discussIcon );
	discussButton.setMargin(new Insets(0, 0, 0, 0));

	cloudIcon = new ImageIcon(cloudImgOff);
	cloudButton = new JButton( cloudIcon );
	cloudButton.setMargin(new Insets(0,0,0,0));

	pollIcon = new ImageIcon(pollImgOff);
	pollButton = new JButton( pollIcon );
	pollButton.setMargin(new Insets(0,0,0,0));

	mosaicIcon = new ImageIcon(mosaicImgOff);
	mosaicButton = new JButton( mosaicIcon);
	mosaicButton.setMargin(new Insets(0,0,0,0));
	mosaicButton.setEnabled(false);

	pointerIcon = new ImageIcon(pointerImgOff);
	pointerButton = new JButton( pointerIcon );
	pointerButton.setMargin(new Insets(0,0,0,0));
	pointerButton.setEnabled(true);

	uGrooveIcon = new ImageIcon(uGrooveImgOff);
	uGrooveButton = new JButton( uGrooveIcon);
	uGrooveButton.setMargin(new Insets(0,0,0,0));
	uGrooveButton.setEnabled(false);

	// Set up button specifics + actions + set class pointers ----------------------------------
	//
	// v2.0 frame/panel instantiation code below

	// Discuss Prompt ------------------------

	myGroupDiscussPromptPanel = new GroupDiscussPrompt();
	myGroupDiscussPromptPanel.init(instructorControlPanelSand);
	groupDiscussPromptFrame = new JFrame("Discuss Prompt");
	groupDiscussPromptFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	groupDiscussPromptFrame.setLocationRelativeTo(null);
	groupDiscussPromptFrame.setPreferredSize(new Dimension(750,200));
	groupDiscussPromptFrame.getContentPane().add(myGroupDiscussPromptPanel);
	groupDiscussPromptFrame.pack();
	discussPromptButton = new JButton ( discussPromptIcon ); //STK 1_29_10
	discussPromptButton.setMargin(new Insets(0, 0, 0, 0));
	discussPromptButton.setBorderPainted(false);
	discussPromptButton.addActionListener( this );

	// Instructor Discuss (display) ----------

	myInstructorGroupDiscussPanel = new InstructorGroupDiscuss();
	myInstructorGroupDiscussPanel.init(instructorControlPanelSand);

	instructorGroupDiscussFrame = new JFrame("Discussion");

	instructorGroupDiscussFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	instructorGroupDiscussFrame.setLocationRelativeTo(null);
	instructorGroupDiscussFrame.setPreferredSize(new Dimension(800,600));
	instructorGroupDiscussFrame.getContentPane().add(myInstructorGroupDiscussPanel);
	instructorGroupDiscussFrame.pack();


	discussDisplayButton = new JButton ( discussDisplayIcon );
	discussDisplayButton.setMargin(new Insets(0,0,0,0));
	discussDisplayButton.setBorderPainted(false);
	discussDisplayButton.addActionListener( this );
	// Cloud Prompt ------------------------
		
	myCloudPromptPanel = new CloudPrompt();
	myCloudPromptPanel.init(instructorControlPanelSand);
	cloudPromptFrame = new JFrame("Discuss Prompt");
	cloudPromptFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	cloudPromptFrame.setLocationRelativeTo(null);
	cloudPromptFrame.setPreferredSize(new Dimension(750,200));
	cloudPromptFrame.getContentPane().add(myCloudPromptPanel);
	cloudPromptFrame.pack();
		
	cloudPromptButton = new JButton ( cloudPromptIcon );
	cloudPromptButton.setMargin(new Insets(0,0,0,0));
	cloudPromptButton.setBorderPainted(false);
	cloudPromptButton.addActionListener( this );

	// Thought Cloud (display)------------------------
		
	myCloudDisplayPanel = new CloudDisplay();
	myCloudDisplayPanel.init(instructorControlPanelSand);

	
	//Have to perform this method here, can't do it in JPanel
	myCloudDisplayPanel.backgroundImg = getImage(imgWebBase,"SandDunes1_1024x768_web.jpg");
	cloudDisplayFrame = new JFrame("Cloud");
	cloudDisplayFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	cloudDisplayFrame.setLocationRelativeTo(null);
	cloudDisplayFrame.setPreferredSize(new Dimension(1024,768));
	cloudDisplayFrame.getContentPane().add(myCloudDisplayPanel);
	cloudDisplayFrame.pack();
		
	cloudDisplayButton = new JButton ( cloudDisplayIcon );
	cloudDisplayButton.setMargin(new Insets(0,0,0,0));
	cloudDisplayButton.setBorderPainted(false);
	cloudDisplayButton.addActionListener( this );
		
	// Poll Prompt ---------------------------

	myPollPromptPanel = new PollPrompt();
	myPollPromptPanel.init(instructorControlPanelSand);

	pollPromptFrame = new JFrame("Poll Prompt");
	pollPromptFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	pollPromptFrame.setLocationRelativeTo(null);
	pollPromptFrame.setPreferredSize(new Dimension(850,350));
	pollPromptFrame.getContentPane().add(myPollPromptPanel);
	pollPromptFrame.pack();

	pollPromptButton = new JButton ( pollPromptIcon );
	pollPromptButton.setMargin(new Insets(0,0,0,0));
	pollPromptButton.setBorderPainted(false);
	pollPromptButton.addActionListener( this );

	// Poll Display --------------------------- xxx

	myPollDisplayPanel = new PollDisplay();
	myPollDisplayPanel.init(instructorControlPanelSand);
	imgPrefix = "http://nomads.music.virginia.edu/images/PollSandBackgroundImages/";

	try { 
	    imgWebBase = new URL(imgPrefix); 
	} 
	catch (Exception e) {}

	myPollDisplayPanel.bgImage = getImage(imgWebBase,"SandDunePoll_40_web.jpg");
	for (int i=0; i<30; i++) {
	    int fileNum = i+20;
	    String tString = new String("SandDunePoll_" + fileNum + "_web.jpg");
	    myPollDisplayPanel.bgImages[i] = getImage(imgWebBase,tString);
	    // NGlobals.dtPrint("tString = " + tString);
	}

	pollDisplayFrame = new JFrame("NOMADS Poll");
	pollDisplayFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	pollDisplayFrame.setLocationRelativeTo(null);
	pollDisplayFrame.setPreferredSize(new Dimension(800,800));
	pollDisplayFrame.getContentPane().add(myPollDisplayPanel);
	pollDisplayFrame.pack();

	pollDisplayButton = new JButton ( pollDisplayIcon );
	pollDisplayButton.setMargin(new Insets(0,0,0,0));
	pollDisplayButton.setBorderPainted(false);
	pollDisplayButton.addActionListener( this );

	// Unity Groove Panel ---------------------------		 

	myUnityGroovePromptPanel = new UnityGroovePrompt();
	myUnityGroovePromptPanel.init(instructorControlPanelSand);
	unityGroovePromptFrame = new JFrame("Unity Groove Control");
	unityGroovePromptFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	unityGroovePromptFrame.setLocationRelativeTo(null);
	unityGroovePromptFrame.setPreferredSize(new Dimension(400,200));
	unityGroovePromptFrame.getContentPane().add(myUnityGroovePromptPanel);
	unityGroovePromptFrame.pack();
		
	uGroovePromptButton = new JButton ( uGroovePromptIcon ); 
	uGroovePromptButton.setMargin(new Insets(0,0,0,0));
	uGroovePromptButton.setBorderPainted(false);
	uGroovePromptButton.addActionListener( this );
	uGroovePromptButton.setEnabled(false);		
	// Unity Groove "display" -----------------------

	myUnityGrooveDisplayPanel = new UnityGroovePanel();
	myUnityGrooveDisplayPanel.init(instructorControlPanelSand);
	unityGrooveDisplayFrame = new JFrame("Unity Groove");
	unityGrooveDisplayFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	unityGrooveDisplayFrame.setLocationRelativeTo(null);
	unityGrooveDisplayFrame.setPreferredSize(new Dimension(500,500));
	unityGrooveDisplayFrame.getContentPane().add(myUnityGrooveDisplayPanel);
	unityGrooveDisplayFrame.pack();

	uGrooveDisplayButton = new JButton ( uGrooveDisplayIcon ); 
	uGroovePromptButton.setMargin(new Insets(0,0,0,0));
	uGrooveDisplayButton.setBorderPainted(false);
	uGrooveDisplayButton.addActionListener( this );
	uGrooveDisplayButton.setEnabled(false);
		
	// Sand Pointer ------------------
	imgPrefix = "http://nomads.music.virginia.edu/images/";

	try { 
	    imgWebBase = new URL(imgPrefix); 
	} 
	catch (Exception e) {}

	myPointerDisplayPanel = new SandPointerDisplay();
	myPointerDisplayPanel.init(instructorControlPanelSand);
	myPointerDisplayPanel.setAllSynthVol(0);

	myPointerDisplayPanel.backgroundImg = getImage(imgWebBase,"NOMADS_world_map.png");


	pointerDisplayFrame = new JFrame("Sand Pointer");
	pointerDisplayFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	pointerDisplayFrame.setLocationRelativeTo(null);
	pointerDisplayFrame.setPreferredSize(new Dimension(800,800));
	pointerDisplayFrame.getContentPane().add(myPointerDisplayPanel);
	pointerDisplayFrame.pack();



	pointerDisplayButton = new JButton ( pointerDisplayIcon );
	pointerDisplayButton.setMargin(new Insets(0,0,0,0));
	pointerDisplayButton.setBorderPainted(false);
	pointerDisplayButton.addActionListener( this );
	pointerDisplayButton.setEnabled(true);
		
	// Sound Mosaic ------------------

	myMosaicDisplayPanel = new SoundMosaicDisplay();
	myMosaicDisplayPanel.init(instructorControlPanelSand);
	mosaicDisplayFrame = new JFrame("Sound Mosaic");
	mosaicDisplayFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	mosaicDisplayFrame.setLocationRelativeTo(null);
	mosaicDisplayFrame.setPreferredSize(new Dimension(1100,700));
	mosaicDisplayFrame.getContentPane().add(myMosaicDisplayPanel);
	mosaicDisplayFrame.pack();

	mosaicDisplayButton = new JButton ( mosaicDisplayIcon );
	mosaicDisplayButton.setMargin(new Insets(0,0,0,0));
	mosaicDisplayButton.setBorderPainted(false);
	mosaicDisplayButton.addActionListener( this );
	mosaicDisplayButton.setEnabled(false);

	// "blank" button for the grid layout to be happy
	blankButton = new JLabel( "" );
	blankButton2 = new JLabel( "" );

	// on/off buttons

	discussButton.setBorderPainted(false);
	discussButton.addActionListener( this );
	discussButton.setPressedIcon(new ImageIcon(discussImgOn));

	cloudButton.setBorderPainted(false);
	cloudButton.addActionListener( this );
	cloudButton.setPressedIcon(new ImageIcon(cloudImgOn));

	pollButton.setBorderPainted(false);
	pollButton.addActionListener( this );
	pollButton.setPressedIcon(new ImageIcon(pollImgOn));

	mosaicButton.setBorderPainted(false);
	mosaicButton.addActionListener( this );
	mosaicButton.setPressedIcon(new ImageIcon(mosaicImgOn));

	pointerButton.setBorderPainted(false);
	pointerButton.addActionListener( this );
	pointerButton.setPressedIcon(new ImageIcon(pointerImgOn));

	uGrooveButton.setBorderPainted(false);
	uGrooveButton.addActionListener( this );
	uGrooveButton.setPressedIcon(new ImageIcon(uGrooveImgOn)); //UN COMMENT ME WHEN WE GET THE uGROOVE FILES

	// add the buttons to "button panel" screen --------

	butPanel.setBackground(Color.black);
	butPanel.add( discussButton, buttonGridLayout );
	butPanel.add( discussPromptButton, buttonGridLayout );
	butPanel.add( discussDisplayButton, buttonGridLayout );
	butPanel.add( cloudButton, buttonGridLayout );
	butPanel.add( cloudPromptButton, buttonGridLayout );
	butPanel.add( cloudDisplayButton, buttonGridLayout );
	butPanel.add( pollButton, buttonGridLayout );
	butPanel.add( pollPromptButton, buttonGridLayout );
	butPanel.add( pollDisplayButton, buttonGridLayout );
	// butPanel.add( mosaicButton, buttonGridLayout );
	// butPanel.add( blankButton, buttonGridLayout );
	// butPanel.add( mosaicDisplayButton, buttonGridLayout);
	butPanel.add( pointerButton, buttonGridLayout );
	butPanel.add( blankButton2, buttonGridLayout );
	butPanel.add( pointerDisplayButton, buttonGridLayout );
	// butPanel.add( uGrooveButton, buttonGridLayout );
	// butPanel.add( uGroovePromptButton, buttonGridLayout );
	// butPanel.add( uGrooveDisplayButton, buttonGridLayout );

	// add the "button panel" to the window ------------h

	add (butPanel);
    }

    public void setupImage( Image img ) {
	icon.setImage( img );
	imageLabel.setIcon( icon );
	repaint( );
    }    


    //============================= HANDLE ==============================
    public void handle() {
	NGlobals.dtPrint("InstructorControlPanel -> handle()");

	NGrain grain;
	byte tByte;


	setSandRead(false);
	grain = instructorControlPanelSand.getGrain();
	if (grain == null) {
	    setSandRead(true);
	    setHandleActive(true);
	    while(true) {
		// Force timeout for errCheckThread to restart
		try {
		    runner.sleep(1000);
		}
		catch (InterruptedException ie) {}
	    }
	}

	if (grain == null)
	    return;
	else
	    grain.print(); //prints grain data to console

	byte incAppID = grain.appID;
	byte tCmd = grain.command;
	byte tAppDataType = grain.dataType;
	int tDataLen = grain.dataLen;

	if ((grain.dataType == NDataType.CHAR || grain.dataType == NDataType.UINT8) && (grain.dataLen > 0)){
	    String input = new String(grain.bArray);
	    tByte = grain.bArray[0];
	    // NGlobals.dtPrint("INSTRUCTOR PANEL RCVD CMD: " + tCmd + " w/ BYTE: " + tByte);
	}
	else {
	    tByte = 0;
	}


	// Buttons --------------------------------------------------------------

	if (incAppID == NAppID.SERVER) {   // TODO:  may need to change this to NAppID.INSTRUCTOR_PANEL

	    if (tCmd == NCommand.SET_DISCUSS_STATUS) {
		discussOnOff = (int)tByte;
		if (tByte == 0) {
		    discussButton.setIcon(discussIconOff);
		}
		else if (tByte == 1) {
		    discussButton.setIcon(discussIconOn);
		}
	    }	    
	    else if (tCmd == NCommand.SET_CLOUD_STATUS) {
		cloudOnOff = (int)tByte;
		if (tByte == 0) {
		    cloudButton.setIcon(cloudIcon);
		}
		else if (tByte == 1) {
		    cloudButton.setIcon(cloudIconOn);
		}
	    }
	    else if (tCmd == NCommand.SET_POLL_STATUS) {
		pollOnOff = (int)tByte;
		if (tByte == 0) {
		    pollButton.setIcon(pollIcon);
		}
		else if (tByte == 1) {
		    pollButton.setIcon(pollIconOn);
		}
	    }
	    else if (tCmd == NCommand.SET_MOSAIC_STATUS) {
		mosaicOnOff = (int)tByte;
		if (tByte == 0) {
		    mosaicButton.setIcon(mosaicIcon);
		}
		else if (tByte == 1) {
		    mosaicButton.setIcon(mosaicIconOn);
		}
	    }

	    else if (tCmd == NCommand.SET_SWARM_STATUS) {
		pointerOnOff = (int)tByte;
		if (tByte == 0) {
		    pointerButton.setIcon(pointerIcon);
		}
		else if (tByte == 1) {
		    pointerButton.setIcon(pointerIconOn);
		}
	    }
	    else if (tCmd == NCommand.SET_UGROOVE_STATUS) {
		uGrooveOnOff = (int)tByte;
		if (tByte == 0) {
		    uGrooveButton.setIcon(uGrooveIcon);
		}
		else if (tByte == 1) {
		    uGrooveButton.setIcon(uGrooveIconOn);
		}
	    }
	}

	// end buttons ----------------------------

	// Send to DISCUSS ------------------------
	//	if (discussOnOff == 1) {
	if (incAppID == NAppID.INSTRUCTOR_PANEL || incAppID == NAppID.DISCUSS || incAppID == NAppID.INSTRUCTOR_DISCUSS || incAppID == NAppID.DISCUSS_PROMPT) {
	    myInstructorGroupDiscussPanel.handle(grain);
	}
	//	}
	
	// Send to CLOUD DISPLAY------------------------q
	//	if (cloudOnOff == 1) {
	if (incAppID == NAppID.INSTRUCTOR_PANEL || incAppID == NAppID.CLOUD_CHAT) {
	    myCloudDisplayPanel.handle(grain);
	}
	//	}
	
	// Send to POLL PROMPT------------------------
	//	if (pollOnOff == 1) {
	if (incAppID == NAppID.INSTRUCTOR_PANEL || incAppID == NAppID.STUDENT_POLL) {
	    myPollPromptPanel.handle(grain);
	}
	//	}
		
	// Send to POLL DISPLAY------------------------
	//	if (pollOnOff == 1) {
	if (incAppID == NAppID.INSTRUCTOR_PANEL || incAppID == NAppID.STUDENT_POLL ||incAppID == NAppID.TEACHER_POLL ) {
	    NGlobals.dtPrint("ICP:  sending to pollDisplay");
	    myPollDisplayPanel.handle(grain);
	}
	//	}
		
	// Send to SOUND MOSAIC DISPLAY------------------------
	if (incAppID == NAppID.INSTRUCTOR_PANEL || incAppID == NAppID.STUDENT_SEQUENCER ) {
	    // myMosaicDisplayPanel.handle(grain);
	}
		
	// Send to SAND POINTER DISPLAY------------------------
	if (incAppID == NAppID.INSTRUCTOR_PANEL || incAppID == NAppID.SOUND_SWARM ) {
	    NGlobals.dtPrint("ICP:  got SOUND SWARM DATA");
	    if (pointerOnOff == 1) {
		NGlobals.dtPrint("ICP:  sending to pointerDisplayPanel()");
		myPointerDisplayPanel.handle(grain);
	    }
	}

	// Send to UNITY GROOVE DISPLAY------------------------
	if (incAppID == NAppID.INSTRUCTOR_PANEL || incAppID == NAppID.INSTRUCT_EMRG_SYNTH_PROMPT) {
	    // myUnityGrooveDisplayPanel.handle(grain);
	}

    }

    public void open() {
	//	sendMessage("KHAN");
    }

    //-------------------------------------------------------------------------------
    public void actionPerformed(ActionEvent ae) {
	byte tByte[] = new byte[1];

	Object source = ae.getSource( );

	// ON/OF buttons ------------------------------------

	if( source == discussButton) {
	    if ( discussOnOff == 0) {
		discussButton.setIcon(discussIconOn);
		discussOnOff = 1;
	    }
	    else if ( discussOnOff == 1) {
		discussButton.setIcon(discussIcon);
		discussOnOff = 0;
	    }
	    tByte[0] = (byte)discussOnOff;
	    NGlobals.cPrint(NAppID.INSTRUCTOR_PANEL + "SET_DISCUSS_STATUS: " + discussOnOff);
	    instructorControlPanelSand.sendGrain(NAppID.INSTRUCTOR_PANEL,
						 NCommand.SET_DISCUSS_STATUS,
						 NDataType.UINT8,
						 1,
						 tByte);
	}

	else if( source == cloudButton ) {		
	    if ( cloudOnOff == 0) {
		cloudButton.setIcon(cloudIconOn);
		cloudOnOff = 1;
	    }
	    else if ( cloudOnOff == 1) {
		cloudButton.setIcon(cloudIcon);
		cloudOnOff = 0;
	    }
	    tByte[0] = (byte)cloudOnOff;
	    NGlobals.cPrint(NAppID.INSTRUCTOR_PANEL + "SET_CLOUD_STATUS: " + cloudOnOff);
	    instructorControlPanelSand.sendGrain(NAppID.INSTRUCTOR_PANEL,
						 NCommand.SET_CLOUD_STATUS,
						 NDataType.UINT8,
						 (byte)1,
						 tByte);
	}

	else if( source == mosaicButton ) {
	    if ( mosaicOnOff == 0) {
		mosaicButton.setIcon(mosaicIconOn);
		mosaicOnOff = 1;
	    }
	    else if ( mosaicOnOff == 1) {
		mosaicButton.setIcon(mosaicIcon);
		mosaicOnOff = 0;
	    }
	    tByte[0] = (byte)mosaicOnOff;
	    NGlobals.cPrint(NAppID.INSTRUCTOR_PANEL + "SET_MOSAIC_STATUS: " + cloudOnOff);
	    instructorControlPanelSand.sendGrain(NAppID.INSTRUCTOR_PANEL,
						 NCommand.SET_MOSAIC_STATUS,
						 NDataType.UINT8,
						 1,
						 tByte);
	}

	else if( source == pollButton ) {
	    if ( pollOnOff == 0) {
		pollButton.setIcon(pollIconOn);
		pollOnOff = 1;
	    }      
	    else if ( pollOnOff == 1) {
		pollButton.setIcon(pollIcon);
		pollOnOff = 0;
	    }
	    tByte[0] = (byte)pollOnOff;
	    NGlobals.cPrint(NAppID.INSTRUCTOR_PANEL + "SET_POLL_STATUS: " + pollOnOff);
	    instructorControlPanelSand.sendGrain(NAppID.INSTRUCTOR_PANEL,
						 NCommand.SET_POLL_STATUS,
						 NDataType.UINT8,
						 1,
						 tByte);
	}

	else if( source == pointerButton ) {	
	    if ( pointerOnOff == 0) {
		pointerButton.setIcon(pointerIconOn);
		pointerOnOff = 1;
	    }      
	    else if ( pointerOnOff == 1) {
		pointerButton.setIcon(pointerIcon);
		pointerOnOff = 0;
		myPointerDisplayPanel.deleteAllSynths();
	    }
	    tByte[0] = (byte)pointerOnOff;
	    NGlobals.cPrint(NAppID.INSTRUCTOR_PANEL + "SET_POINTER_STATUS: " + pointerOnOff);
	    instructorControlPanelSand.sendGrain(NAppID.INSTRUCTOR_PANEL,
						 NCommand.SET_POINTER_STATUS,
						 NDataType.UINT8,
						 1,
						 tByte);
	}

	else if( source == uGrooveButton ) {	
	    if ( uGrooveOnOff == 0) {
		uGrooveButton.setIcon(uGrooveIconOn);
		uGrooveOnOff = 1;
	    }      
	    else if ( uGrooveOnOff == 1) {
		uGrooveButton.setIcon(uGrooveIcon);
		uGrooveOnOff = 0;
	    }
	    tByte[0] = (byte)uGrooveOnOff;
	    NGlobals.cPrint(NAppID.INSTRUCTOR_PANEL + "SET_UGROOVE_STATUS: " + uGrooveOnOff);
	    instructorControlPanelSand.sendGrain(NAppID.INSTRUCTOR_PANEL,
						 NCommand.SET_UGROOVE_STATUS,
						 NDataType.UINT8,
						 1,
						 tByte);

	}

	else if( source == audioMenu_SoundOff ) {	
	    audioMenu_SoundOn.setSelected(false);
	    audioMenu_SoundOff.setSelected(true);
	    myPointerDisplayPanel.soundStatus = false;
	    myPointerDisplayPanel.setAllSynthVol(0);
	}

	else if( source == audioMenu_SoundOn ) {	
	    audioMenu_SoundOff.setSelected(false);
	    audioMenu_SoundOn.setSelected(true);
	    myPointerDisplayPanel.soundStatus = true;
	    myPointerDisplayPanel.setAllSynthVol(1);
	}

	else if( source == pointerMenu_BlackBackground ) {	
	    pointerMenu_BlackBackground.setSelected(true);
	    pointerMenu_MapBackground.setSelected(false);
	    NGlobals.dtPrint("calling setBackground(0)");
	    myPointerDisplayPanel.height=800;
	    myPointerDisplayPanel.width=800;
	    pointerDisplayFrame.setVisible(false);
	    pointerDisplayFrame.setPreferredSize(new Dimension(800,800));
	    pointerDisplayFrame.setSize(800,800);
	    myPointerDisplayPanel.setBackground(0);
	    pointerDisplayFrame.setVisible(true);

	    // xxxx put code to change background here
	}

	else if( source == pointerMenu_MapBackground ) {	
	    pointerMenu_MapBackground.setSelected(true);
	    pointerMenu_BlackBackground.setSelected(false);
	    NGlobals.dtPrint("calling setBackground(1)");
	    pointerDisplayFrame.setVisible(false);
	    myPointerDisplayPanel.width=1800;
	    myPointerDisplayPanel.height=900;
	    pointerDisplayFrame.setPreferredSize(new Dimension(1800,900));
	    pointerDisplayFrame.setSize(1800,900);
	    myPointerDisplayPanel.setBackground(1);
	    pointerDisplayFrame.setVisible(true);

	    // xxxx put code to change background here
	}




	// END ------ ON/OFF buttons ------------------------------------

	// Launch sub apps v2.0 ---------------------------------------

	else if( source == discussPromptButton ) {
	    groupDiscussPromptFrame.setVisible(true);
	    // getAppletContext().showDocument(discussPromptURL,"DiscussPrompt"); 	
	}

	else if( source == discussDisplayButton ) {
	    instructorGroupDiscussFrame.setVisible(true);
	    myInstructorGroupDiscussPanel.chatBottom();
	    
	    // xxx
	    // getAppletContext().showDocument(discussURL,"CloudPrompt"); 	
	}


	else if( source == cloudPromptButton ) {
	    cloudPromptFrame.setVisible(true);
	    //	getAppletContext().showDocument(cloudPromptURL,"CloudPrompt"); 	
	}
		
	else if( source == cloudDisplayButton ) {
	    cloudDisplayFrame.setVisible(true);
	    //	getAppletContext().showDocument(cloudDisplayURL,"CloudDisplay");
	}	

	else if( source == pollPromptButton ) {
	    pollPromptFrame.setVisible(true);
	    //			getAppletContext().showDocument(pollPromptURL,"PollPrompt"); 	
	}	

	else if( source == pollDisplayButton ) {
	    pollDisplayFrame.setVisible(true);
	    // getAppletContext().showDocument(pollDisplayURL,"PollDisplay"); 	
	}

	else if( source == mosaicDisplayButton ) {
	    mosaicDisplayFrame.setVisible(true);
	    // getAppletContext().showDocument(mosaicDisplayURL,"Sound Mosaic"); 	
	}

	else if( source == pointerDisplayButton ) {
	    pointerDisplayFrame.setVisible(true);
	    //			getAppletContext().showDocument(pointerDisplayURL,"Sand Pointer"); 	
	}

	else if( source == uGroovePromptButton ) {
	    unityGroovePromptFrame.setVisible(true);
	    //getAppletContext().showDocument(uGroovePromptURL,"Unity Groove Prompt"); 	
	}

	else if( source == uGrooveDisplayButton ) {
	    unityGrooveDisplayFrame.setVisible(true);
	    //getAppletContext().showDocument(uGrooveDisplayURL,"Unity Groove Display"); 	
	}

	else if ( source == pollMenu_VoteAgain ) {
	    tByte[0]=0;
	    instructorControlPanelSand.sendGrain(
						 NAppID.TEACHER_POLL,
						 NCommand.VOTE_AGAIN,
						 NDataType.UINT8,
						 1,
						 tByte);
	    NGlobals.cPrint("Vote Again selected");
	}
    }

    void sendMessage (String _output){
	String output = _output;
	int outputLen = output.length();
	byte[] outputAsBytes = output.getBytes();
	instructorControlPanelSand.sendGrain(
					     NAppID.INSTRUCTOR_PANEL,
					     NCommand.SEND_MESSAGE,
					     NDataType.BYTE,
					     outputLen,
					     outputAsBytes);
    }
}
