//
//  Nomads Opera Main v.210
//

import java.awt.*;
import java.lang.Math;
import java.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.net.*;
import java.io.*;
import netscape.javascript.*;
import nomads.v210_auk.*;
import java.awt.image.BufferedImage;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.view11x.ExponentialPortFader;
import com.softsynth.jsyn.view11x.LabelledFader;
import com.softsynth.jsyn.view11x.PortFader;
import com.softsynth.jsyn.view11x.SynthScope;

public class OperaMain extends Applet implements MouseListener, MouseMotionListener, ActionListener, Runnable {   


    private class NomadsAppThread extends Thread {
	OperaMain client; //Replace with current class name
	Calendar now;
	long handleStart=0;
	long handleEnd=1;
	long millis=0;
	Boolean runState=false;


	public synchronized long getHandleStart() {
	    return handleStart;
	}

	public synchronized long getHandleEnd() {
	    return handleEnd;
	}

	public synchronized void setHandleStart(long hs) {
	    handleStart = hs;
	}

	public synchronized void setHandleEnd(long he) {
	    handleEnd = he;
	}

	public synchronized void setRunState(Boolean state) {
	    runState = state;
	}

	public synchronized Boolean getRunState() {
	    return runState;
	}


	public NomadsAppThread(OperaMain _client) {
	    client = _client;
	}
	public void run()    {			
	    NGlobals.lPrint("NomadsAppThread -> run()");
	    while (getRunState() == true)  {
		now = Calendar.getInstance();
		setHandleStart(now.getTimeInMillis());
		client.handle();
		client.setHandleActive(false);
		handleEnd = now.getTimeInMillis();
		millis = getHandleEnd()-getHandleStart();
		// System.out.println("handle() proc time:" + millis);
	    }
	}
    }

    private class NomadsErrCheckThread extends Thread {
	OperaMain client; //Replace with current class name

	public NomadsErrCheckThread(OperaMain _client) {
	    client = _client;
	}
	public void run()    {			
	    NGlobals.lPrint("NomadsErrCheckThread -> run()");
	    while (true)  {
		client.errCheck();
	    }
	}
    }


    NSand operaSand;
    private NomadsAppThread nThread;
    private NomadsErrCheckThread nECThread;

    int skipper = 0;
    int maxSkip = 1;
    Random randNum;
    int numOscs = 0;
	
    int currentBackgroundImageName = 3; //SELECT WHICH IMAGE TO USE: 0=800x600, 1=1024x768, 2=1280x1024, 3=1920x1080
    String backgroundImageName[]; //Stores background images
    float textImageSizeScaler = 1.0F; //Change depending on image size

    String imgPrefix;
    Image backgroundIce;
    URL imgWebBase;

    int     MAX_THREADS = 100000;
    int oscNum[];
    int tOscNum[];
    Boolean            oscCheck[];
    LineOut            lineOut;
    int                lineOutType;
    BusReader          myBusReader;
    BusWriter          myBusWriter[];
    NoiseSwarm		myNoiseSwarm[];
    SynthEnvelope      	envData[];
    EnvelopePlayer     	envPlayer[];

    int t,i,j,clear, picker;
    int width,height,twidth,theight,fontSize, centerX, centerY;
    Font textFont;
    int startFontSize, minFontSize, maxFontSize;

    int tRow, tCol,rows,cols,tRows, tCols;
    int chatRows;

    int x,y,w,h,dx,dy,dw,dh;
    int wait;
    int pbi;

    int posX, posY, origX, origY; //STK used to get H value for frequency
    double origXScaler, origYScaler;
    double hMax, hMaxSqr;
    int diagonal;
    double diagonalSQ;
    float freqMultiply, mainVolumeFromSlider;
    double[] startFreq;
    double[][]	data;
    int[] startX;
    int[] startY;

    int padX, padY; 
    double weightX, weightY, weight;
    int stringLength;
    int tPass, tNewFontSize;
    int wordFound;

    Color textColor,backGroundColor;

    int chatSpace, chatXLoc, chatYLoc, tH;

    int chatA, cloudA, pointerA;
    int numChatColors,maxChatColors;
    int numCloudColors,maxCloudColors;
    int numPointerColors,maxPointerColors;
    int tChatColorNum, tCloudColorNum, tPointerColorNum;

    Color chatColors[] = new Color[8];

    Color cloudColors[];
    Color pointerColors[];

    JButton	clearButton;
    String chatLines[];
    int numChatLines = 60;
    int activeChatLines = 0;

    int mx, my; // recent mouse coords
    Boolean isMouseDraggingBox = false;

    Boolean handleActive = false;
    Boolean sandRead = false;
    Boolean connected;

    int MAX_OSCS = 50;

    public synchronized Boolean getSandRead() {
	return sandRead;
    }

    public synchronized void setSandRead(Boolean sr) {
	sandRead = sr;
    }

    public synchronized Boolean getHandleActive() {
	return handleActive;
    }

    public synchronized void setHandleActive(Boolean ha) {
	handleActive = ha;
    }



    int errFlag = 0;
    int lastThread = 0;

    public class Sprite {
	int x, y;
	int r;
	int g;
	int b;
	int a;
	Color color;
    }

    Sprite sprites[];

    Sprite tSprite;

    public synchronized Boolean isOsc(int i) {
	return oscCheck[i];
    }

    public synchronized void deleteOsc(int i) {
	oscCheck[i] = false;
    }

    public synchronized void deleteSprite(int i) {
	sprites[i] = null;
    }

    public synchronized Sprite getSprite(int i) {
	return sprites[i];
    }

    //  static final double MAX_MOD_DEPTH = 500.0;

    public class HistoElt {
	String text;  // The actual text we're printing
	int pass;  // What pass the word was entered (ie., time)
	int size, x, y, cols, quad;
	Font font;
	int r,g,b,a;
	Color color;
    }    

    // Number of times we've checked our global word list array
    int numPasses;

    // List of all our elements
    ArrayList<HistoElt> histoGram;
    //  Temporary placeholder
    HistoElt tHist;

    int guesser,rGuess,cGuess;

    BufferedImage offScreen;
    Graphics2D offScreenGrp;
    Image player;

    Font chatFont;


    Color chatColor;

    // mist vars made global to try and improve performance

    double tVolumeVal;
    int tNum;
    float tAmp;
    float tVolume;
    int scaledX;
    int scaledY;
    double myX, myY, myH_Sqr;
    double myH;
    double tFreq;
    String text;
    int xMin;
    int xVar;
    int yMin;
    int yVar;
    int xRand;
    int yRand;
    int xpoints[] = new int[4];
    int ypoints[] = new int[4];
    int txpoints[] = new int[4];
    int typoints[] = new int[4];
    int threadNum;
    int quad;
    int lastQuad;

    int numRChatLines;
    int curLine;
    int rLineCntr;
    int rLineSlots[];

    Thread runner;

    public static void main(String args[])
    {
	/* DO: Change TUT_SineFreq to match the name of your class. Must match file name! */


	OperaMain  applet = new OperaMain();

	int width = applet.getSize().width;
	int height = applet.getSize().height;

	AppletFrame frame = new AppletFrame("OperaMain", applet);
	frame.resize(width,height);

	//	frame.resize(700,400);
	frame.show();

	/* Begin test after frame opened so that DirectSound will use Java window. */
	frame.test();



    }

    // public void setChatColors(int alpha) {
    // 	chatColors[0] = new Color(158, 55, 33, alpha);
    //   	chatColors[1] = new Color(145, 86, 65, alpha);
    //   	chatColors[2] = new Color(187, 137, 44, alpha);
    //   	chatColors[3] = new Color(191, 140, 44, alpha);
    //   	chatColors[4] = new Color(233, 158, 37, alpha);
    //   	chatColors[5] = new Color(242, 197, 126, alpha);
    //   	chatColors[6] = new Color(254, 205, 129, alpha);
    //   	chatColors[7] = new Color(249, 241, 131, alpha);
    //     }
    // 
    //     public void setCloudColors(int alpha) {
    // 	cloudColors[0] = new Color(158, 55, 33, alpha);
    //   	cloudColors[1] = new Color(145, 86, 65, alpha);
    //   	cloudColors[2] = new Color(187, 137, 44, alpha);
    //   	cloudColors[3] = new Color(191, 140, 44, alpha);
    //   	cloudColors[4] = new Color(233, 158, 37, alpha);
    //   	cloudColors[5] = new Color(242, 197, 126, alpha);
    //   	cloudColors[6] = new Color(254, 205, 129, alpha);
    //   	cloudColors[7] = new Color(249, 241, 131, alpha);
    //     }
    // 
    //     public void setPointerColors(int alpha) {
    // 	pointerColors[0] = new Color(158, 55, 33, alpha);
    //   	pointerColors[1] = new Color(145, 86, 65, alpha);
    //   	pointerColors[2] = new Color(187, 137, 44, alpha);
    //   	pointerColors[3] = new Color(191, 140, 44, alpha);
    //   	pointerColors[4] = new Color(233, 158, 37, alpha);
    //   	pointerColors[5] = new Color(242, 197, 126, alpha);
    //   	pointerColors[6] = new Color(254, 205, 129, alpha);
    //   	pointerColors[7] = new Color(249, 241, 131, alpha);
    //     }

    public void setChatColors(int alpha) {
	chatColors[0] = new Color(204, 255, 255, alpha);
	chatColors[1] = new Color(145, 86, 65, alpha);
	chatColors[2] = new Color(145, 86, 65, alpha);
	chatColors[3] = new Color(145, 86, 65, alpha);
	chatColors[4] = new Color(145, 86, 65, alpha);
	chatColors[5] = new Color(145, 86, 65, alpha);
	chatColors[6] = new Color(145, 86, 65, alpha);
	chatColors[7] = new Color(145, 86, 65, alpha);
    }

    public void setCloudColors(int alpha) {
	cloudColors[0] = new Color(130, 240, 255, alpha);
	cloudColors[1] = new Color(130, 240, 255, alpha);
	cloudColors[2] = new Color(130, 240, 255, alpha);
	cloudColors[3] = new Color(130, 240, 255, alpha);
	cloudColors[4] = new Color(130, 240, 255, alpha);
	cloudColors[5] = new Color(130, 240, 255, alpha);
	cloudColors[6] = new Color(130, 240, 255, alpha);
	cloudColors[7] = new Color(130, 240, 255, alpha);
    }

    public void setPointerColors(int alpha) {
	pointerColors[0] = new Color(255, 255, 255, alpha);
	pointerColors[1] = new Color(255, 255, 255, alpha);
	pointerColors[2] = new Color(255, 255, 255, alpha);
	pointerColors[3] = new Color(255, 255, 255, alpha);
	pointerColors[4] = new Color(255, 255, 255, alpha);
	pointerColors[5] = new Color(255, 255, 255, alpha);
	pointerColors[6] = new Color(255, 255, 255, alpha);
	pointerColors[7] = new Color(255, 255, 255, alpha);
    }
	
    public void init()
    {  	

	backgroundImageName = new String[4];
	backgroundImageName[0] = "BackgroundDisplay1_800x600.jpg";
	backgroundImageName[1] = "BackgroundDisplay1_1024x768.jpg";
	backgroundImageName[2] = "BackgroundDisplay1_1280x1024.jpg";
	backgroundImageName[3] = "BackgroundDisplay1_1920x1080.jpg";
		
	//Resizing text/pointer based on which image we're using
	if (currentBackgroundImageName == 0)
	    textImageSizeScaler = 1.0F;
	else if (currentBackgroundImageName == 1)
	    textImageSizeScaler = 1.28F;
	else if (currentBackgroundImageName == 2)
	    textImageSizeScaler = 1.7F;
	else if (currentBackgroundImageName == 3)
	    textImageSizeScaler = 1.8F;
		

	chatFont = new Font("TimesRoman", Font.PLAIN, (int)(14 * textImageSizeScaler));
				


	imgPrefix = "http://nomads.music.virginia.edu/images/";

	quad = 0;
	lastQuad = 0;

	try { 
	    imgWebBase = new URL(imgPrefix); 
	} 
	catch (Exception e) {}

	width = getSize().width;
	height = getSize().height;

	offScreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

	offScreenGrp = (Graphics2D) offScreen.getGraphics();
	backgroundIce = getImage(imgWebBase,backgroundImageName[currentBackgroundImageName]);
	//	backgroundIce = getImage(imgWebBase,"BackgroundDisplay1_1920x1080.jpg");

	// backgroundIce = getImage(imgWebBase,"NOMADSMainDisplay_5760x1200Background.jpg");

	//	backgroundIce = getImage(imgWebBase,"NOMADSMainDisplay_11520x1200Background.jpg");

	offScreenGrp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	randNum = new Random();

	int i;

	numRChatLines = 24;

	rLineSlots = new int[numRChatLines+1];

	int tNum;
	for(j=0;j<numRChatLines;j++) {
	    
	    tNum = randNum.nextInt(numRChatLines);
	    for (i=0;i<numRChatLines;i++) {
		if (rLineSlots[i] == tNum) {
		    i=0;
		    tNum = randNum.nextInt(numRChatLines);
		}
	    }
	    rLineSlots[j] = tNum;
	}

	startX = new int[100];
	startY = new int[100];


	int wStart = (int)(width * 0.1);
	int wEnd = (int)(width * 0.9);

	int hStart = (int)(height * 0.1);
	int hEnd = (int)(height * 0.9);

	int wDiff = wEnd-wStart;
	int hDiff = hEnd-hStart;

	float hIncr = (int)(hDiff/numRChatLines);  // divide by numRChatLines vertically

	int tX;
	int tY;
	int tLine = 0;
	int tSlot = 0;
	tX = wStart;

	centerX = (width/2);
	centerY = (height/2); 

	for (int l=0; l<numRChatLines; l++) {
	    tSlot = rLineSlots[l];
	    if (tSlot%3 == 2) {
		startX[tSlot] = wStart;
	    }
	    else if (tSlot%3 == 1) {
		startX[tSlot] = wStart+(int)(wDiff/3);
	    }
	    else {
		startX[tSlot] = wStart+(int)((2*wDiff)/3);
	    }
	    tY = (int)(tSlot*hIncr);
	    startY[tSlot] = tY;
	    tLine++;
	}


	NGlobals.dtPrint("width = " + width);
	NGlobals.dtPrint("height = " + height);
	setBackground(Color.black);	

	chatLines = new String[numChatLines];

	chatA = 255;
	setChatColors(chatA);
	chatColor = chatColors[0];


	tChatColorNum = 0;
	maxChatColors = 7;

	cloudColors = new Color[8];
	cloudA = 180;
	setCloudColors(cloudA);
	tCloudColorNum = 0;
	maxCloudColors = 7;

	pointerColors = new Color[8];
	pointerA = 180;
	setPointerColors(cloudA);
	tPointerColorNum = 0;
	maxPointerColors = 7;

	i = 0;
	j = 0;
	clear = 0;

	histoGram = new ArrayList<HistoElt>();

	wordFound = 0;
	numPasses = 0;
	startFontSize = (int)(30 * textImageSizeScaler);
	minFontSize = (int)(2 * textImageSizeScaler);
	maxFontSize = (int)(100 * textImageSizeScaler);

	x = width / 2 - 20;
	y = height / 2 - 20;
	origX = x;
	origY = y;
	posX = origX;
	posY = origY;

	//****STK 8/6/12 TopLeftMin/Max values with origin in top left, from 0-1000 (eventually to be scaled to 0-1 floats)
	origXScaler = (double)(width/1000.0);
	origYScaler = (double)(height/1000.0);
	NGlobals.cPrint("OMP: origXScaler = " + origXScaler + " origYScaler = " + origYScaler);

	diagonalSQ = Math.pow(width, 2) + Math.pow(height, 2);
	diagonal = (int)(Math.sqrt(diagonalSQ)/2); //figure out 1/2 of diagonal
	startFreq = new double [9];
	// Frequencies: 883.61hz, 700.16hz, 624.90hz, 441.8hz, 343.56hz, 280.43hz, 214.29hz, 171.89hz, 129.17hz
	startFreq[0] = 883.61;
	startFreq[1] = 700.16;
	startFreq[2] = 624.9;
	startFreq[3] = 441.8;
	startFreq[4] = 343.56;
	startFreq[5] = 280.43;
	startFreq[6] = 214.29;
	startFreq[7] = 171.89;
	startFreq[8] = 129.17;

	freqMultiply = 2.0F;
	mainVolumeFromSlider = 1.0F;



	hMaxSqr = Math.pow(x, 2) + Math.pow(y, 2); //Pythagoras' Theorem 
	hMax = (float)Math.sqrt(hMaxSqr); //Maximum distance from center
	NGlobals.cPrint("Maximum H = " + hMax);

	addMouseListener(this);
	addMouseMotionListener(this);

	sprites = new Sprite[MAX_THREADS];
	oscCheck = new Boolean[MAX_THREADS];
	oscNum = new int[MAX_THREADS];
	tOscNum = new int[MAX_THREADS];

	data  = new double[MAX_THREADS][];

	for (i=0;i<numChatLines;i++) {
	    chatLines[i] = "";
	}

	for (i=0;i<MAX_THREADS;i++) {
	    oscCheck[i] = false;
	}

	try
	    {
		// Synth.startEngine(0);

		// myNoiseSwarm = new NoiseSwarm[MAX_THREADS];
		// envPlayer = new EnvelopePlayer[MAX_THREADS];
		// envData = new SynthEnvelope[MAX_THREADS];
		// lineOut  = new LineOut();

		// myBusReader = new BusReader();

		// myBusWriter = new BusWriter[MAX_THREADS];

		// /* Synchronize Java display to make buttons appear. */
		// getParent().validate();
		// getToolkit().sync();

		// myBusReader.output.connect(0, lineOut.input, 0 );
		// myBusReader.output.connect(0, lineOut.input, 1 );

		// myBusReader.start();
		// lineOut.start();

		//	myBusReader.output.connect(lineOut.input, 0 );


	    } catch(SynthException e) {
	    SynthAlert.showError(this,e);
	}
	operaSand = new NSand(); 
	operaSand.connect();
	connected = true;

	nThread = new NomadsAppThread(this);
	nThread.setRunState(true);
	nThread.start();

	nECThread = new NomadsErrCheckThread(this);
	nECThread.start();

	int d[] = new int[1];
	d[0] = 0;
	operaSand.sendGrain((byte)NAppID.OPERA_MAIN, (byte)NCommand.REGISTER, (byte)NDataType.UINT8, 1, d );

	redraw();
    }	

    public void deleteAllSynths() {
	for (int i=0;i<numOscs;i++) {
	    int tNum = 	oscNum[i];
	    deleteSynth(tNum);
	}
	// numOscs = 0;
    }


    public void deleteSynth(int threadNum) {

	if (isOsc(threadNum)) {
	    deleteSprite(threadNum);
	    sprites[threadNum] = null;
	    // deleteOsc(threadNum);
	    // oscCheck[threadNum] = false;
	    // envPlayer[threadNum].stop();
	    // envPlayer[threadNum].delete();
	    // myNoiseSwarm[threadNum].delete();


	    int j=0;

	    for (int i=0;i<numOscs;i++) {
		int tNum = 	oscNum[i];
		if (sprites[tNum] != null) {
		    tOscNum[j] = oscNum[i];
		    j++;
		}
	    }

	    numOscs--;
	    for (int i=0;i<numOscs;i++) {
		oscNum[i] = tOscNum[i];
	    }
	}
    }

    public void makeSynth(int threadNum) {

	int tNum;
	float tAmp;
	Random randStartFreq;
	randStartFreq = new Random();
	int tStartFreq = randStartFreq.nextInt(8);
	NGlobals.cPrint("tstartFreq = " + tStartFreq);


	if (!isOsc(threadNum)) {

	    NGlobals.dtPrint("CREATING OSC" + numOscs);

	    sprites[threadNum] = new Sprite();
	    sprites[threadNum].x = width / 2 - 20;
	    sprites[threadNum].y = height / 2 - 20;
	    //	    sprites[threadNum].color = new Color(randNum.nextInt(255),randNum.nextInt(255),randNum.nextInt(255), pointerA);
	    // sprites[threadNum].r = randNum.nextInt(255);
	    // 	    sprites[threadNum].g = randNum.nextInt(255);
	    // 	    sprites[threadNum].b = randNum.nextInt(255);
	    sprites[threadNum].r = 255;
	    sprites[threadNum].g = 255;
	    sprites[threadNum].b = 255;
	    sprites[threadNum].a = pointerA;

	    oscNum[numOscs++] = threadNum;
	    if (numOscs < 20) {
		maxSkip = 1+(int)(numOscs/10);
	    }
	    else if (numOscs < 40) {
		maxSkip = 1+(int)(numOscs/5);
	    }
	    else if (numOscs < 60) {
		maxSkip = 1+(int)(numOscs/4);
	    }
	    else if (numOscs < 100) {
		maxSkip = 1+(int)(numOscs/3);
	    }

	    // if (myNoiseSwarm[threadNum] == null) {
	    // 	myNoiseSwarm[threadNum] = new NoiseSwarm();
	    // }
	    // if (envPlayer[threadNum] == null) {
	    // 	envPlayer[threadNum] = new EnvelopePlayer();
	    // }

	    // for (int i=0;i<numOscs;i++) {
	    // tNum = oscNum[i];
	    // tAmp = (float)2/numOscs; //default amp = 2.0
	    // NGlobals.cPrint(i + ":resetting amp for osc " + tNum + " to " + tAmp);
	    // myNoiseSwarm[tNum].amplitude.set(tAmp * mainVolumeFromSlider);
	    // float tVolume = tAmp * mainVolumeFromSlider;
	    // NGlobals.cPrint("Amplitude = " + tVolume);
	    // }

	    //	    envPlayer[threadNum].output.connect( myNoiseSwarm[threadNum].frequency );

	    // define shape of envelope as an array of doubles
	    // data[threadNum] = new double[2];
	    // data[threadNum][0] = 0.1; //time point value
	    // data[threadNum][1] = (startFreq[tStartFreq]); //frequency
	    // NGlobals.cPrint( "starting freq= " + data[threadNum][1]);

	    // if (envData[threadNum] == null) {
	    // 	envData[threadNum] = new SynthEnvelope( 1 );
	    // }
	    // envData[threadNum].write(0, data[threadNum], 0, 1); // 1 = number of frames

	    //	    myBusWriter[threadNum]   = new BusWriter(); /* Create bus writers. */

	    //	    myNoiseSwarm[threadNum].output.connect(myBusWriter[threadNum].input);
	    // myBusWriter[threadNum].busOutput.connect( myBusReader.busInput );	    
	    // myNoiseSwarm[threadNum].frequency.set((float)(startFreq[tStartFreq]));

	    // envPlayer[threadNum].start();
	    // myNoiseSwarm[threadNum].start();
	    // myBusWriter[threadNum].start();
	    oscCheck[threadNum] = true;
	    // envPlayer[threadNum].envelopePort.queue( envData[threadNum] );

	}
	else {
	    NGlobals.cPrint("Synth already created for thread: " + threadNum);

	}
    }


    // For swarm display

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
	mx = e.getX();
	my = e.getY();
	if (x < mx && mx < x + 40 && y < my && my < y + 40) {
	    isMouseDraggingBox = true;
	}
	e.consume();
    }

    public void mouseReleased(MouseEvent e) {
	/*if (isMouseDraggingBox) {
	  try {
	  double myx = (mx - (width / 2)) / ((double) width * 3);
	  double myy = (my - (height / 2)) / ((double) height * 3);
	  String towrite = "move" + " " + personnum + " " + myx + " "
	  + myy;
	  System.out.println("Byte is " + app_id.STUDENT_SAND_POINTER + " and write is "
	  + towrite);
	  streamOut.writeByte(app_id.STUDENT_SAND_POINTER);
	  streamOut.writeUTF(towrite);
	  } catch (IOException ioe) {
	  System.out.println("Error writing...");
	  }
	  }*/
	isMouseDraggingBox = false;
	e.consume();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
	if (isMouseDraggingBox) {
	    // get the latest mouse position
	    int new_mx = e.getX();
	    int new_my = e.getY();

	    // displace the box by the distance the mouse moved since the last
	    // event
	    // Note that "x += ...;" is just shorthand for "x = x + ...;"
	    // if (new_mx < 5)
	    //	new_mx = 5;
	    // if (new_mx > 890)
	    //	new_mx = 890;
	    // if (new_my < 5)
	    //	new_my = 5;
	    // if (new_my > 590)
	    //	new_my = 590;

	    x += new_mx - mx;
	    y += new_my - my;

	    // update our data
	    mx = new_mx;
	    my = new_my;


	    redraw();
	    e.consume();
	}
    }

    public void update ( Graphics g ) {
	paint (g);
    }


    public void errCheck() {
	Calendar now;
	long mSecN=0;
	long mSecH=0;
	long mSecDiff=0;

	try {

	    if ((getHandleActive() == true) && (getSandRead() == true)) {

		now = Calendar.getInstance();
		mSecN = now.getTimeInMillis();
		mSecH = nThread.getHandleStart();

		mSecDiff = mSecN-mSecH;

		System.out.println(">>> handleErrCheck time diff: " + mSecDiff);

		if (mSecDiff > 1000) {
		    errFlag += 1;
		    if (errFlag > 0) {
			System.out.println(">>> INCR ERROR COUNT: " + errFlag);
		    }
		    if ((errFlag > 3) && (connected == true)) {
			System.out.println("######### CRITICAL ERROR");
			System.out.println(">>> handleErrCheck time diff: " + mSecDiff);
			System.out.println(">>> halting thread ...");
			nThread.setRunState(false);
			NomadsErrCheckThread.sleep(1000);
			// deleteSynth(lastThread);
			nThread = null;
			System.out.println(">>> disconnecting ...");
			operaSand.disconnect();
			NomadsErrCheckThread.sleep(1000);
			operaSand = null;
			connected = false;
			System.out.println(">>> disconneced ...");
			// System.out.println(">>> deleting sprites/synths ...");
			// deleteAllSynths();
			// System.out.println(">>> sprites/synths deleted ...");
			System.out.println("+++++ Attempting reconnect ...");
			NomadsErrCheckThread.sleep(1000);
			operaSand = new NSand(); 
			operaSand.connect();
			int d[] = new int[1];
			d[0] = 0;
			operaSand.sendGrain((byte)NAppID.OPERA_MAIN, (byte)NCommand.REGISTER, (byte)NDataType.UINT8, 1, d );
			connected = true;
			NomadsErrCheckThread.sleep(1000);
			System.out.println("+++ reconnected!");			
			System.out.println("+++ attempting to restart thread ...");			
			NomadsErrCheckThread.sleep(1000);
			nThread = new NomadsAppThread(this);
			nThread.setRunState(true);
			nThread.start();
			System.out.println("+++ thread restarted!");			
			errFlag = 0;

			now = Calendar.getInstance();
			mSecN = now.getTimeInMillis();
			nThread.setHandleStart(mSecN);

		    }
		}
		else if (errFlag > 0) {
		    errFlag--;
		    System.out.println(">>> DECR ERROR COUNT: " + errFlag);
		}
	    }
	    NomadsErrCheckThread.sleep(10);
	}
	catch (InterruptedException ie) {}

    }


    // ------------------------------------------------------------------------------------------------
    // BEGIN handle()
    // ------------------------------------------------------------------------------------------------

    public void handle() { //bite text
	int i,j,fc,sc,x,y;
	float freq,amp;
	String temp,tAlpha,input, tTest;
	int THREAD_ID;
	float xput,yput;

	int incCmd, incAppID, incDType, incDLen;
	int incIntData[] = new int[1000];
	byte incByteData[] = new byte[1000];  // Cast as chars here because we're using chars -> strings
	NGrain grain;

	NGlobals.cPrint("OperaMain -> handle()");
	// System.out.println("handle()");

	setSandRead(false);
	grain = operaSand.getGrain();
	setSandRead(true);
	setHandleActive(true);
	//grain.print(); //prints grain data to console

	incAppID = grain.appID;
	incCmd = grain.command;

	NGlobals.cPrint("...");
	NGlobals.cPrint("OM: incAppID= " + incAppID + " incCmd= " + incCmd);
	NGlobals.cPrint("...");

	if (incAppID == NAppID.SERVER) {
	    if (grain.command == NCommand.DELETE_SPRITE) {
		THREAD_ID = grain.iArray[0];

		NGlobals.cPrint("DELETING SPRITE: " + THREAD_ID);
		deleteSynth(THREAD_ID);
	    }

	}


	// CONDUCTOR PANEL ================================================================================

	else if (incAppID == NAppID.CONDUCTOR_PANEL) {


	    if (incCmd == NCommand.SET_DISCUSS_ALPHA) {
		chatA = grain.iArray[0];
		setChatColors(chatA);
		NGlobals.cPrint("Setting ChA to " + chatA);
		redraw();
	    }

	    else if (incCmd == NCommand.SET_CLOUD_ALPHA) {
		cloudA = grain.iArray[0];;
		setCloudColors(cloudA);
		NGlobals.cPrint("Setting ClA to " + cloudA);
		redraw();
	    }
	    else if (incCmd == NCommand.SET_POINTER_ALPHA) {
		pointerA = grain.iArray[0];;
		setPointerColors(pointerA);
		NGlobals.cPrint("Setting PtA to " + pointerA);
		redraw();
	    }
	    else if (incCmd == NCommand.SET_CLOUD_DISPLAY_STATUS) {  // Cloud reset
		if(grain.bArray[0] == 0) {
		    histoGram.clear();

		    // int tSize = histoGram.size();
		    // for (i=0;i<tSize;i++) {
		    //     histoGram.remove(i);
		    // }
		    NGlobals.cPrint("Resetting cloud...\n");
		    redraw();
		}
	    }
	    else if (incCmd == NCommand.SET_DISCUSS_DISPLAY_STATUS) {  // Discuss reset
		if(grain.bArray[0] == 0) {
		    for (i=0;i<numChatLines;i++) {
			chatLines[i] = "";
		    }
		    tH = (int)(height*1.1);
		    chatSpace = tH/numChatLines;
		    chatYLoc = height-chatSpace;
		    chatXLoc = 20;
		    NGlobals.cPrint("Resetting discuss...\n");
		    redraw();
		}
	    }
	    else if (incCmd == NCommand.SET_SYNTH_VOLUME) {	
		// tVolumeVal = (double)grain.iArray[0];
		// mainVolumeFromSlider = (float)(Math.pow(tVolumeVal, 2)/10000.0);
		// for (i=0;i<numOscs;i++) {
		//     tNum = oscNum[i];
		//     tAmp = (float)2/numOscs; //default amp = 2.0
		//     NGlobals.cPrint(i + ":resetting amp for osc " + tNum + " to " + tAmp);
		//     myNoiseSwarm[tNum].amplitude.set(tAmp * mainVolumeFromSlider);
		//     tVolume = tAmp * mainVolumeFromSlider;
		//     NGlobals.cPrint("Amplitude = " + tVolume);
		// }
		//TO DO: Make this a log function. . .
	    }




	}

	// ========= Pointer (regular) ============================================

	else if (incAppID == NAppID.OC_POINTER) {
	    NGlobals.cPrint("OMP: OC_POINTER\n");
	    if (grain.command == NCommand.SEND_SPRITE_THREAD_XY) {
		THREAD_ID = grain.iArray[0];
		lastThread = THREAD_ID;
		x = grain.iArray[1];
		y = grain.iArray[2];
		NGlobals.cPrint("OPERA_MAIN:  got SEND_SPRITE_XY from SOUND_SWARM: " + x + "," + y);

		if (numOscs < MAX_OSCS) {
		    makeSynth(THREAD_ID);
		}
		else if (numOscs >= MAX_OSCS) {
		    NGlobals.dtPrint("OPERA_MAIN:  MAX_OSCS");
		}

		NGlobals.cPrint("OMP: THREAD_ID = " + THREAD_ID);

		freq = (float)x;
		amp = (float)(y/1000);

		//				float fx = (float)(x+1000)/(float)2000;
		//				float fy = (float)(y+1000)/(float)2000;
		//
		//				x = (int)(fx*width);
		//				y = (int)(fy*height);

		scaledX = (int)(x * origXScaler);
		scaledY = (int)(y * origYScaler);
		x = scaledX;
		y = scaledY;

		NGlobals.cPrint("OMP: x = " + x);
		//		amp = 1;
		NGlobals.cPrint("OMP: y = " + y);
		NGlobals.cPrint("OMP: scaledX = " + scaledX);
		//		amp = 1;
		NGlobals.cPrint("OMP: scaledY = " + scaledY);

		//if (x > 900)
		//	x = 900;
		//				xput = (float)(x/0.5);
		xput = x;
		if (xput < 50)
		    xput = 50;
		if (xput > 900)
		    xput = 900;

		//if (y > 900)
		//	y = 900;

		//			yput = (float)((y/0.5));
		yput = y;
		if (yput < 0)
		    yput = 0;
		if (yput > 900)
		    yput = 900;


		//=============== STK code to get H value for frequency =======================

		double myX, myY, myH_Sqr;
		double myH;		


		if (isOsc(THREAD_ID)) {
		    NGlobals.cPrint("setting osc values for thread: " + THREAD_ID);

		    tSprite = getSprite(THREAD_ID);

		    tSprite.x = x;
		    tSprite.y = y;

		    if (x >= origX)
			myX = (double)(x - origX); //if X value is bigger than origin value, distance = X-origin (x - 230)
		    else 
			myX = (double)(origX - x);

		    if (posY >= y)
			myY = (double)(y - origY);
		    else
			myY = (double)(origY - y);

		    NGlobals.cPrint( "x = " + x + "y = " + y + "myX = " + myX + "myY" + myY);


		    myH_Sqr = Math.pow(myX, 2) + Math.pow(myY, 2); //Pythagoras' Theorem 

		    myH = Math.sqrt(myH_Sqr); //distance from center
		    NGlobals.cPrint( "H = " + myH + "Diagonal = " + diagonal);

		    // double tFreq = (float)( 10.00 * Math.pow(1.005, myH));
		    tFreq = (float)myH * 4.0;

		    if (tFreq > 22050.0)
			tFreq = 22050.0;

		    if (tFreq < 20.0)
			tFreq = 20.0;

		    NGlobals.cPrint("tFreq " + THREAD_ID + " set to " + tFreq);
		    // data[THREAD_ID][1] = tFreq;
		    //	System.out.println("data[1] = " + data[THREAD_ID][1]);
		    // envData[THREAD_ID].write(0, data[THREAD_ID], 0, 1); // 1 = number of frames
		    // envPlayer[THREAD_ID].envelopePort.clear();
		    // envPlayer[THREAD_ID].envelopePort.queue( envData[THREAD_ID] );

		    redraw();
		}
	    }

	}

	// ========= Pointer (Java based) ============================================

	else if (incAppID == NAppID.JOC_POINTER) {
	    NGlobals.cPrint("OMP: JOC_POINTER\n");
	    if (grain.command == NCommand.SEND_SPRITE_THREAD_XY) {
		if (skipper == 0) {

		    THREAD_ID = grain.iArray[0];
		    x = grain.iArray[1];
		    y = grain.iArray[2];
		    NGlobals.cPrint("OPERA_MAIN:  got SEND_SPRITE_XY from SOUND_SWARM: " + x + "," + y);

		    if (numOscs < MAX_OSCS) {
			makeSynth(THREAD_ID);
		    }
		    else if (numOscs >= MAX_OSCS) {
			NGlobals.dtPrint("OPERA_MAIN:  MAX_OSCS");
		    }

		    NGlobals.cPrint("OMP: THREAD_ID = " + THREAD_ID);

		    freq = (float)x;
		    amp = (float)(y/1000);

		    //				float fx = (float)(x+1000)/(float)2000;
		    //				float fy = (float)(y+1000)/(float)2000;
		    //
		    //				x = (int)(fx*width);
		    //				y = (int)(fy*height);

		    scaledX = (int)(x * origXScaler);
		    scaledY = (int)(y * origYScaler);
		    x = scaledX;
		    y = scaledY;

		    NGlobals.cPrint("OMP: x = " + x);
		    //		amp = 1;
		    NGlobals.cPrint("OMP: y = " + y);
		    NGlobals.cPrint("OMP: scaledX = " + scaledX);
		    //		amp = 1;
		    NGlobals.cPrint("OMP: scaledY = " + scaledY);

		    //if (x > 900)
		    //	x = 900;
		    //				xput = (float)(x/0.5);
		    xput = x;
		    if (xput < 50)
			xput = 50;
		    if (xput > 900)
			xput = 900;

		    //if (y > 900)
		    //	y = 900;

		    //			yput = (float)((y/0.5));
		    yput = y;
		    if (yput < 0)
			yput = 0;
		    if (yput > 900)
			yput = 900;


		    //=============== STK code to get H value for frequency =======================

		    double myX, myY, myH_Sqr;
		    double myH;		


		    if (isOsc(THREAD_ID)) {
			NGlobals.cPrint("setting osc values for thread: " + THREAD_ID);

			tSprite = getSprite(THREAD_ID);

			tSprite.x = x;
			tSprite.y = y;

			if (x >= origX)
			    myX = (double)(x - origX); //if X value is bigger than origin value, distance = X-origin (x - 230)
			else 
			    myX = (double)(origX - x);

			if (posY >= y)
			    myY = (double)(y - origY);
			else
			    myY = (double)(origY - y);

			NGlobals.cPrint( "x = " + x + "y = " + y + "myX = " + myX + "myY" + myY);


			myH_Sqr = Math.pow(myX, 2) + Math.pow(myY, 2); //Pythagoras' Theorem 

			myH = Math.sqrt(myH_Sqr); //distance from center
			NGlobals.cPrint( "H = " + myH + "Diagonal = " + diagonal);

			// double tFreq = (float)( 10.00 * Math.pow(1.005, myH));
			tFreq = (float)myH * 4.0;

			if (tFreq > 22050.0)
			    tFreq = 22050.0;

			if (tFreq < 20.0)
			    tFreq = 20.0;

			NGlobals.cPrint("tFreq " + THREAD_ID + " set to " + tFreq);
			// data[THREAD_ID][1] = tFreq;
			//	System.out.println("data[1] = " + data[THREAD_ID][1]);
			// envData[THREAD_ID].write(0, data[THREAD_ID], 0, 1); // 1 = number of frames
			// envPlayer[THREAD_ID].envelopePort.clear();
			// envPlayer[THREAD_ID].envelopePort.queue( envData[THREAD_ID] );

			//	myNoiseSwarm[THREAD_ID].frequency.set(((startFreq + myH) * freqMultiply));
			redraw();
		    }
		}
		else {
		    // System.out.println("skipping: " + skipper);
		}
		skipper++;
		if (skipper > maxSkip)
		    skipper = 0;
	    }
	}

	// ========= CLOUD INPUT ============================================

	else if (incAppID == NAppID.OC_CLOUD) {
	    if (incCmd == NCommand.SEND_MESSAGE) {
		text = new String(grain.bArray);
		NGlobals.cPrint("OM: CloudText: " + text);
		NGlobals.cPrint("OC_CLOUD\n");

		stringLength = text.length(); 

		// Then check text locations to avoid collisions *************************

		wordFound = 0;

		// Check our histogram =============================================

		for (i=0;i<histoGram.size();i++) {
		    tHist = histoGram.get(i);
		    NGlobals.cPrint("...");
		    NGlobals.cPrint("checking histogram ----- tHist.text = ||>> " + tHist.text + " <<||");
		    NGlobals.cPrint("...");
		    NGlobals.cPrint("  tHist.size = " + tHist.size);
		    NGlobals.cPrint("...");
		    NGlobals.cPrint(" histoGram.size() = " + histoGram.size());

		    // 1.  Histogram element [i] matches incoming text -----

		    x=tHist.x;
		    y=tHist.y;	       	

		    if (tHist.text.compareToIgnoreCase(text) == 0) {
			wordFound = 1;
			NGlobals.cPrint("...");
			NGlobals.cPrint(">>>FOUND " + tHist.text + " at [" + x + "]" + "[" + y + "]");
			NGlobals.cPrint("  INCreasing text size");

			// This will change to be a combination of rank v time (ie., numPasses);
			tHist.size += 4;

			quad = tHist.quad;
			NGlobals.cPrint("  quad = " + quad);
			if (quad > 2) {
			    tHist.x-=8;
			    tHist.y-=5;
			    if (tHist.x < centerX)
				tHist.x = centerX;
			    if (tHist.y < centerY)
				tHist.y = centerY;
			}
			else if (quad > 1) {
			    tHist.x-=8;
			    tHist.y+=5;
			    if (tHist.x < centerX)
				tHist.x = centerX;
			    if (tHist.y > centerY)
				tHist.y = centerY;
			}
			else if (quad > 0) {
			    tHist.x+=6;
			    tHist.y+=5;
			    if (tHist.x > centerX)
				tHist.x = centerX;
			    if (tHist.y > centerY)
				tHist.y = centerY;
			}
			else {
			    tHist.x+=6;
			    tHist.y-=5;
			    if (tHist.x > centerX)
				tHist.x = centerX;
			    if (tHist.y < centerY)
				tHist.y = centerY;
			}

			if (tHist.size > maxFontSize) {
			    fontSize = maxFontSize;
			}
			else {
			    fontSize = tHist.size;
			}
			tHist.font = new Font("TimesRoman", Font.PLAIN, fontSize);

			//i = histoGram.size();  // exit the loop

		    }

		    // 2a.  Blank cell ... do nothing
		    else if (tHist.text.compareToIgnoreCase("") == 0) {
			NGlobals.cPrint("|_|");
		    }

		    // 2.  Histogram element [i] DOES NOT match incoming text and is > min size -----

		    else if (tHist.size > minFontSize) {  // Decrease size (if > min AND modulo 2)
			NGlobals.cPrint("...");
			NGlobals.cPrint("  DECreasing word size for " + tHist.text);
			NGlobals.cPrint("...");
			NGlobals.cPrint("  numPasses = " + numPasses);
			if (numPasses%2 == 0) {
			    tHist.size -= 4;
			    quad = tHist.quad;
			    if (quad > 2) {
				tHist.x+=2;
				tHist.y+=2;
			    }
			    else if (quad > 1) {
				tHist.x+=2;
				tHist.y-=2;
			    }
			    else if (quad > 0) {
				tHist.x-=2;
				tHist.y-=2;
			    }
			    else {
				tHist.x-=2;
				tHist.y+=2;
			    }
			    if (tHist.x < 10) 
				tHist.x = 10;
			    if (tHist.x > (width-10))
				tHist.x = width-10;
			    if (tHist.y < 10) 
				tHist.y = 10;
			    if (tHist.y > (height-10))
				tHist.y = height-10;
			}
			if (tHist.size > maxFontSize)
			    tHist.size = maxFontSize;
			fontSize = tHist.size;
			tHist.font = new Font("TimesRoman", Font.PLAIN, fontSize);

			NGlobals.cPrint("...");

			NGlobals.cPrint("  tHist.x,tHist.y = " + tHist.x + "," + tHist.y);
			NGlobals.cPrint("...");
			NGlobals.cPrint("  tHist.size= " + tHist.size);
		    }

		    // 3.  Histogram element [i] DOES NOT match and is < min size ... delete it

		    else if (tHist.size <= minFontSize) {
			NGlobals.cPrint("...");
			NGlobals.cPrint("  tHist.x,tHist.y = " + tHist.x + "," + tHist.y);
			NGlobals.cPrint("...");
			NGlobals.cPrint("  tHist.size= " + tHist.size);
			NGlobals.cPrint("...");
			NGlobals.cPrint("  REMoving word: " + tHist.text + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

			fontSize = tHist.size;
			if (fontSize < minFontSize)
			    fontSize = minFontSize;

			histoGram.remove(i);
			i--;
		    }			  	
		}  // end for (i=0;i<histoGram.size();i++)


		// No words found, add new word and store relevant data
		if (wordFound == 0) {
		    // Figure out where to put the text =============================================

		    // Find a free cell
		    picker = 1;

		    NGlobals.cPrint("...");

		    // figure out center, then expand range over time 

		    x=y=0;

		    tHist = new HistoElt();
		    tHist.text = new String(text);

		    xMin = (int)(width * 0.0);
		    xVar = (int)(width * 0.4);

		    yMin = (int)(height * 0.0);
		    yVar = (int)(height * 0.4);

		    xRand = xMin + randNum.nextInt(xVar);
		    yRand = yMin + randNum.nextInt(yVar);

		    curLine = rLineSlots[rLineCntr];

		    /// xxx
		    x = startX[curLine]+randNum.nextInt(100);

		    int tLen = text.length();
		    int tEnd = x+((int)(startFontSize*0.6)*tLen);
		    int tDiff = (int)((width*0.9)-tEnd);
		    if (tDiff < 0) {
			x+=tDiff;
		    }
		    if (x < (int)(width*0.1)) {
			x = (int)(width*0.1);
		    }

		    y = startY[curLine]+randNum.nextInt(10);

		    rLineCntr++;
		    if (rLineCntr >= numRChatLines)
			rLineCntr = 0;


		    if ((x > centerX) && (y > centerY)) {
			quad = 3;
		    }
		    if ((x > centerX) && (y < centerY)) {
			quad = 2;
		    }
		    if ((x < centerX) && (y < centerY)) {
			quad = 1;
		    }
		    if ((x < centerX) && (y > centerY)) {
			quad = 0;
		    }
		    tHist.quad = quad;


		    NGlobals.cPrint(">>>NEW WORD " + tHist.text + " at [" + x + "]" + "[" + y + "]");
		    NGlobals.cPrint("setting quad = " + quad);

		    NGlobals.cPrint("<<<< ADDING new word: " + text + " at " + "[" + x + "]" + "[" + y + "]");

		    tHist.color = cloudColors[tCloudColorNum]; 	
		    tCloudColorNum++;
		    if (tCloudColorNum > maxCloudColors)
			tCloudColorNum = 0;	

		    fontSize = tHist.size = startFontSize;
		    tHist.pass = numPasses;

		    tHist.x = x;
		    tHist.y = y;

		    tHist.font = new Font("TimesRoman", Font.PLAIN, tHist.size);
		    histoGram.add(tHist);

		    // This will change to be a combination of rank v time (ie., numPasses);

		    // DRAW THE TEXT ======================================

		    NGlobals.cPrint("...");
		    NGlobals.cPrint("Drawing word: " + text);

		}

		// CODE TO CLEAR THE SCREEN, NOT USED AS OF 2/15/2010 ============================
		//**** If we fill up the cells they clear SK 12/03/09
		//****Ultimately we should make a button that does this

		clear = 0;
		if (clear == 1) {
		    NGlobals.cPrint("CLEAR!");
		    histoGram.clear();
		    clear = 0;
		    i = 0;
		    j = 0;
		    clear = 0;
		    guesser = 0;		
		    NGlobals.cPrint("CLEAR:  clearing rows/cols");
		}

		// END CLEAR CODE ==================================================================

		numPasses++;
		NGlobals.cPrint("...");
		NGlobals.cPrint("END handle(" + text + ") numPasses = " + numPasses + " -----");
		redraw();
	    }
	}
	// END OC_CLOUD ------------------------------------------------------------------------------------

	// OC_DISCUSS ============================================================================================

	else if (incAppID == NAppID.OC_DISCUSS) {
	    if (incCmd == NCommand.SEND_MESSAGE) {
		text = new String(grain.bArray);
		NGlobals.cPrint("OM: DiscussText: " + text);
		for (i=(numChatLines-1);i>0;i--) {
		    chatLines[i] = chatLines[i-1];
		}
		chatLines[0] = text;
		redraw();
	    }
	}

	//		if (bite == app_id.MONITOR) {
	//			if (text.equals("CHECK")) {
	//				try {
	//					streamOut.writeByte((byte)app_id.MONITOR);
	//					streamOut.writeUTF("PING");
	//				}
	//				catch(IOException ioe) {
	//					System.out.println("Error writing to output stream: ");
	//				}
	//			}	 
	//		}   

	NGlobals.cPrint ("-------------------------------------------------[OM]\n");

	setHandleActive(false);

    }

    // ------------------------------------------------------------------------------------------------
    // END handle()
    // ------------------------------------------------------------------------------------------------

    public void actionPerformed( ActionEvent ae )
    {
	Object obj = ae.getSource();
	if( obj == clearButton ) {
	    NGlobals.cPrint("You pressed clear");
	    NGlobals.cPrint("...");
	    NGlobals.cPrint("actionPerformed():  clearing histogram");
	    for (i=0;i<histoGram.size();i++) {

		NGlobals.cPrint("...");
		String tempString = Integer.toString(histoGram.size());

		NGlobals.cPrint(tempString);
		tHist = histoGram.get(i);
		tHist.pass = 0;
		x=tHist.x;
		y=tHist.y;
		tHist.font = new Font("TimesRoman", Font.PLAIN, startFontSize);
	    }
	    NGlobals.cPrint("CLEAR!");
	    histoGram.clear();
	    clear = 0;
	    i = 0;
	    j = 0;
	    clear = 0;
	    guesser = 0;		
	    NGlobals.cPrint("...");
	    NGlobals.cPrint("actionPerformed():  clearing rows/cols");
	}

    }
    // DT 6/30/10:  not sure we need these anymore

    public void start() {
	runner = new Thread(this);
	runner.start();
    }

    public void run () {
	NGlobals.cPrint("I'm running!");
	while (true) {
	    try {
		repaint();
		runner.sleep(100);
	    }
	    catch (InterruptedException ie) {}
	}


    }

    public void paint(Graphics g) {
	super.paint(g);
	redraw();
	g.drawImage(offScreen, 0, 0, width, height, this);
	// NGlobals.cPrint("painting...");
    }

    public synchronized void redraw() {

	int tx, ty, r,gr,b,a;
	Color tc;
	int ksize = (int)(7 * textImageSizeScaler);
	int ssize = (int)(5 * textImageSizeScaler);

	int len1,len2;
	//	g.dispose();
	//setBackground(Color.black);
	//g.setColor(Color.black);

	//setBackground(Color.black);
	// g.setColor(Color.black); //STK here's where to change the background color

	// DT DB method
	offScreenGrp.drawImage(backgroundIce, 0, 0, width, height, this);

	// DT old method
	// g.drawImage(backgroundIce, 0, 0, width, height, this);
	//g.fillRect(0,0,width,height);

	//g.setPaintMode();
	//g.dispose();
	xpoints[0] = x-(int)(ksize/2);
	xpoints[1] = x+(int)(ksize/2);
	xpoints[2] = x+(int)(ksize*1.5);
	xpoints[3] = x+(int)(ksize/2);

	ypoints[0] = y+(int)(ksize/1);
	ypoints[1] = y-(int)(ksize/1);
	ypoints[2] = y+(int)(ksize/1);
	ypoints[3] = y+(int)(ksize*3);

	//g.setColor(Color.RED);
	//g.fillPolygon(xpoints, ypoints, xpoints.length);

	//	setBackground(Color.black);

	for (int i=0;i<numOscs;i++) {
	    threadNum = oscNum[i];

	    tSprite = getSprite(threadNum);

	    tx = tSprite.x;
	    ty = tSprite.y;

	    r = tSprite.r;
	    gr = tSprite.g;
	    b = tSprite.b;
	    a = pointerA;
	    tc = new Color(r,gr,b,a);

	    offScreenGrp.setColor(tc);

	    if (numOscs > 1) {
		len1 = 0;
		if (i > 0) {
		    int tt1 = oscNum[i-1];
		    tSprite = getSprite(tt1);
		    int x2 = tSprite.x;
		    int y2 = tSprite.y;
		    // len1 = sqrt(abs(tx-x2)+abs(ty-y2));	
		    offScreenGrp.drawLine(tx,ty,x2,y2);
		    len2 = 0;
		    if ((numOscs > 2) && (i > 1)) {
			tt1 = oscNum[i-2];
			tSprite = getSprite(tt1);
			x2 = tSprite.x;
			y2 = tSprite.y;
			// len2 = sqrt(abs(tx-x2)+abs(ty-y2));
			offScreenGrp.drawLine(tx,ty,x2,y2);
		    }
		}
	    }
	    // if (len1 != 0) {
	    // 	ssize += 100/len1;
	    // 	if (len2 != 0) {
	    // 	    ssize += ((100/len2) + (100/len1));
	    // 	}
	    // }


	    tc = new Color(r,gr,b,a);

	    offScreenGrp.setColor(tc);

	    txpoints[0] = tx-(int)(ssize/2);
	    txpoints[1] = tx+(int)(ssize/2);
	    txpoints[2] = tx+(int)(ssize*1.5);
	    txpoints[3] = tx+(int)(ssize/2);

	    typoints[0] = ty+(int)(ssize/2);
	    typoints[1] = ty-(int)(ssize/2);
	    typoints[2] = ty+(int)(ssize/2);
	    typoints[3] = ty+(int)(ssize*1.5);

	    offScreenGrp.fillPolygon(txpoints, typoints, txpoints.length);
	    //	    g.fillRect(tx, ty, 10, 10);
	}

	//	setBackground(Color.black);

	for (i=0;i<histoGram.size();i++) {
	    tHist = histoGram.get(i);

	    r = tHist.color.getRed();
	    gr = tHist.color.getGreen();
	    b = tHist.color.getBlue();
	    a = cloudA;

	    tc = new Color(r,gr,b,a);

	    offScreenGrp.setColor(tc);
	    offScreenGrp.setFont(tHist.font);
	    offScreenGrp.drawString(tHist.text, tHist.x, tHist.y);
	    // int tLen = tHist.text.length();
	    // int tH = 10; // TODO set to font height
	    // int tW = ((int)(startFontSize*0.6)*tLen);
	    // offScreenGrp.drawRect(tHist.x, tHist.y, tW, tH);
	    
	}

	tH = (int)(height*1.1);
	chatSpace = tH/numChatLines;
	chatYLoc = height-chatSpace;
	chatXLoc = 20;



	chatColor = chatColors[0];
	offScreenGrp.setFont(chatFont);
	offScreenGrp.setColor(chatColor);

	for (i=0;i<numChatLines;i++) {
	    offScreenGrp.drawString(chatLines[i], chatXLoc, chatYLoc);
	    chatYLoc -= chatSpace;
	}




    }

}
