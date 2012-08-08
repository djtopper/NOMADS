// Swarm.java
// Nomads Auksalaq
// Paul Turowski. 2012.08.02

package com.nomads;

import nomads.v210.*;
import nomads.v210.NGlobals.GrainTarget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
//import android.content.Intent;
import android.widget.TextView;

public class Swarm extends Activity
{
	NomadsApp app;

	private NSand sand;
	private NGrain grain;
	
//	Button buttonDiscuss, buttonCloud, buttonSettings;
	TextView chatWindow;
	ImageButton buttonDiscuss, buttonCloud, buttonSettings;
	Button buttonAudioTest;
	final Context context = this;
	AlertDialog.Builder alert;
	EditText alertInput;
	String tempString = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{		
		Log.i("Swarm", "onCreate()");
		super.onCreate(savedInstanceState);
		
		app = (NomadsApp)this.getApplicationContext();
		
		// send reference of Swarm to NomadsApp
		app.setSwarm(this);
		
		// get NSand instance from Join
		sand = app.getSand();
		
		setContentView(R.layout.swarm);
		
		chatWindow = (TextView)findViewById(R.id.chatWindow);
		chatWindow.setMovementMethod(new ScrollingMovementMethod());
		buttonDiscuss = (ImageButton)findViewById(R.id.buttonDiscuss);
		buttonDiscuss.setOnClickListener(discussListener);
		buttonCloud = (ImageButton)findViewById(R.id.buttonCloud);
		buttonCloud.setOnClickListener(cloudListener);
		buttonSettings = (ImageButton)findViewById(R.id.buttonSettings);
		buttonSettings.setOnClickListener(settingsListener);
		buttonAudioTest = (Button)findViewById(R.id.buttonAudioTest);
		buttonAudioTest.setOnClickListener(audioTestButtonListener);
		
	}
	
//	void goToJoin() {
//		Intent intent = new Intent(getApplicationContext(), Join.class);
//		startActivity(intent);
//	}
	
	//========================================================
	// Network
	//========================================================
	
	public void parseGrain(NGrain _grain)
	{
		Log.i("Swarm", "parseGrain()");
		grain = _grain;
//		String msg = new String(grain.bArray.toString());
//		Log.i("Swarm", msg);

//		if (grain.appID == NAppID.DISCUSS_PROMPT) {
//			topic.setText(msg);
//			tempString = new String(msg);
//		}
//		// Disable discuss when the student panel button is off
//		else if (grain.appID == NAppID.INSTRUCTOR_PANEL) {
//			if (msg.equals("DISABLE_DISCUSS_BUTTON")) {
//				topic.setText("Discuss Disabled");
//				chatWindow.setText("");
//			}
//			else if (msg.equals("ENABLE_DISCUSS_BUTTON")) {
//				topic.setText(tempString);
//			}			
//		}
//		else if (grain.appID == NAppIDAuk.OC_DISCUSS){
//		if (grain.appID == NAppIDAuk.OC_DISCUSS){
//			appendTextAndScroll(msg);
//			Log.i("Discuss", "ChatWindow: " + msg);
////			input.requestFocus();
//		}

		if (grain != null)
			grain = null;
	}
	
	//========================================================
	// Buttons
	//========================================================
	
	Button.OnClickListener discussListener = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			discussAlert();
		}
	};
	
	Button.OnClickListener cloudListener = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			cloudAlert();
		}
	};
	
	Button.OnClickListener settingsListener = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(), Settings.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}
	};
	
	Button.OnClickListener audioTestButtonListener = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			SoundManager.playSound(1, 1);
		}
	};
	
	//========================================================
	// Alerts
	//========================================================
	
	protected void discussAlert ()
	{
		alert = new AlertDialog.Builder(context);
		// need to create new input field each time
		alertInput = new EditText(context);
		
		alert.setTitle("Discuss:");
//		alert.setMessage("Message");
		alert.setView(alertInput);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				String value = alertInput.getText().toString();
				Log.d("Swarm->Discuss", value);
				byte[] discussMsg = value.getBytes();
				// eventually use this:
				// char[] discussMsg = value.toCharArray();
				sand.new Send(
						NAppIDAuk.OC_DISCUSS,
						NCommandAuk.SEND_MESSAGE,
						NDataType.CHAR,
						discussMsg.length,
						discussMsg )
				.execute();
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				// Canceled.
			}
		});
		
		alert.show();
	}
	
	protected void cloudAlert ()
	{
		alert = new AlertDialog.Builder(context);
		// need to create new input field each time
		alertInput = new EditText(context);
		
		alert.setTitle("Cloud:");
//		alert.setMessage("Message");
		alert.setView(alertInput);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				String value = alertInput.getText().toString();
				Log.d("Swarm->Discuss", value);
				byte[] cloudMsg = value.getBytes();
				sand.new Send(
						NAppIDAuk.OC_CLOUD,
						NCommandAuk.SEND_MESSAGE,
						NDataType.CHAR,
						cloudMsg.length,
						cloudMsg)
				.execute();
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				// Canceled.
		  }
		});
		
		alert.show();
	}
	
	//========================================================
	
	private void appendTextAndScroll(String text)
	{
	    if(chatWindow != null){
	    	chatWindow.append(text + "\n");
	        final Layout layout = chatWindow.getLayout();
	        if(layout != null){
	            int scrollDelta = layout.getLineBottom(chatWindow.getLineCount() - 1) 
	                - chatWindow.getScrollY() - chatWindow.getHeight();
	            if(scrollDelta > 0)
	            	chatWindow.scrollBy(0, scrollDelta);
	        }
	    }
	}
	
	@Override
	protected void onPause()
	{
		Log.i("Swarm", "onPause()");
		super.onPause();
//		Join.instance.threadRunLoop(false);
//		app.setAppState(false);
	}

	@Override
	protected void onResume()
	{
		Log.i("Swarm", "onResume()");
		super.onResume();
//		Join.instance.threadRunLoop(true);
//		app.setAppState(true);
		app.setGrainTarget(GrainTarget.SWARM);
	}
}
