//
//  TEST CLIENT - DISCUSS
//

import java.awt.*;
import java.lang.Math;
import java.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.net.*;
import java.io.*;
import nomads.v210.*;

public class DiscussTest implements Runnable {   

    private class NomadsAppThread extends Thread {
	DiscussTest client; //Replace with current class name

	public NomadsAppThread(DiscussTest _client) {
	    client = _client;
	}
	public void run()    {			
	    NGlobals.lPrint("NomadsAppThread -> run()");
	    while (true)  {
		client.handle();
	    }
	}
    }

    private Thread       thread = null;

    NSand discussTestSand;
    private NomadsAppThread nThread;

    Random randNum;

    URL imgWebBase;

    Thread runner;

    String discussString[] = new String[31];

    int tNum, tNumL;

    public static void main(String args[])
    {

	DiscussTest  applet = null;
	applet = new DiscussTest();

    }

    public void init()
    {  	

	int i;

	NGlobals.cPrint("DiscussTest -> init()");

	discussTestSand = new NSand(); 
	discussTestSand.connect();


	i=0;
	discussString[i++] = new String("Hello everyone");
	discussString[i++] = new String("This is wonderful");
	discussString[i++] = new String("This is cool");
	discussString[i++] = new String("Will this be on the final?");
	discussString[i++] = new String("I love techno");
	discussString[i++] = new String("I love metal");
	discussString[i++] = new String("I love Radiohead");
	discussString[i++] = new String("I love DEADMAU5");
	discussString[i++] = new String("I like Matmos");
	discussString[i++] = new String("I like the Beatles");
	discussString[i++] = new String("I hate when people play loud music");
	discussString[i++] = new String("Hello from the back of the room");
	discussString[i++] = new String("Hello from the front of the room");
	discussString[i++] = new String("Hello from the closet");
	discussString[i++] = new String("Hello from my dorm room");
	discussString[i++] = new String("This is giving me a lot to think about");
	discussString[i++] = new String("Hi");
	discussString[i++] = new String("Hello");
	discussString[i++] = new String("Hi");
	discussString[i++] = new String("Hello");
	discussString[i++] = new String("Yo!");
	discussString[i++] = new String("Woohoo");
	discussString[i++] = new String("I feel bad for people who do not study");
	discussString[i++] = new String("I like computers");
	discussString[i++] = new String("Will this help us?");
	discussString[i++] = new String("Will I get an A in this class?");
	discussString[i++] = new String("How can I help?");
	discussString[i++] = new String("What can we do?");
	discussString[i++] = new String("Hmmm...");
	discussString[i++] = new String("Ok");
	discussString[i++] = new String("I think I understand");


	int d[] = new int[1];
	d[0] = 0;

	nThread = new NomadsAppThread(this);
	nThread.start();

	discussTestSand.sendGrain((byte)NAppID.BINDLE, (byte)NCommand.REGISTER, (byte)NDataType.UINT8, 1, d );

	String tString = new String("testUsers");
	int tLen = tString.length();

	byte[] tStringAsBytes = tString.getBytes();

	discussTestSand.sendGrain((byte)NAppID.BINDLE, (byte)NCommand.LOGIN, (byte)NDataType.UINT8, tLen, tStringAsBytes );

	



    }	

    public DiscussTest() {  	    
	randNum = new Random();
	init();
	start();
    }

    public void start()  {  
	NGlobals.cPrint("DiscussTest -> start()");

	runner = new Thread(this);
	runner.start();

    }

    public void stop() {  
	if (thread != null)  {  
	    thread.stop(); 
	    thread = null;
	}
    }

    public void run () {
	NGlobals.cPrint("DiscussTest -> run()");
	int i = 0;

	while (true) {
	    String tString;
	    // if (i>500) {
	    // 	tString = new String("RESETTING DISCUSS TEXT TAG TO 0");
	    // 	i=0;
	    // }
	    // else {
	    // 	tString = new String("DISCUSSION TEST " + i++);
	    // }
	    // xxx
	    tNumL = tNum;
	    System.out.println("tNumL = " + tNumL);
	    tNum = randNum.nextInt(31);
	    while (tNum == tNumL) {
		tNum = randNum.nextInt(31);
		System.out.println("tNum = " + tNum);
	    }
	    System.out.println("#tNum = " + tNum);
	    tString = new String(discussString[tNum]);

	    int tLen = tString.length();
	    byte[] tBytes = tString.getBytes();

	    try {
		NGlobals.cPrint("DiscussTest -> NSand.send()");
		discussTestSand.sendGrain((byte)NAppID.DISCUSS, (byte)NCommand.SEND_MESSAGE, (byte)NDataType.CHAR, tLen, tBytes );
		runner.sleep(1000);
	    }
	    catch (InterruptedException ie) {}

	}

    }

    public void handle() {

	NGrain grain;

	// NGlobals.cPrint("DiscussTest -> handle()");

	grain = discussTestSand.getGrain();
	// grain.print();
	// System.out.println("DiscussTest handle()");

	    
    }



}
