// Discuss Client
// Nomads - University of Virginia
// 2012.05

package com.nomads;

import nomads.v210.NAppID;
import nomads.v210.NGrain;
import nomads.v210.NSand;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Discuss extends Activity {
	private Context ctx;
	NSand sand;
	NGrain grain;
	
	EditText input;
	TextView topic;
	TextView chatWindow;
	Button speak;
	String tempString = "";

	AlertDialog.Builder alert;
	EditText alertInput;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.discuss);
	    input = (EditText)findViewById(R.id.textOut);
		topic = (TextView)findViewById(R.id.promptIn);
		chatWindow = (TextView)findViewById(R.id.messageViewer);
		chatWindow.setMovementMethod(new ScrollingMovementMethod());
		speak = (Button)findViewById(R.id.send);
		speak.setOnClickListener(buttonSendOnClickListener);
		
		ctx = TabbedBindle.context;
		alert = new AlertDialog.Builder(ctx);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.i("Discuss", "is resumed");
//		startThread();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.i("Discuss", "is paused");
//		stopThread();
	}
	
	public void parseGrain(NGrain _grain) {
		grain = _grain;

		Log.i("Discuss", "DiscussClient -> handle()");
		String msg = new String(grain.bArray);
		

		if (grain.appID == NAppID.DISCUSS_PROMPT) {
			topic.setText(msg);
			tempString = new String(msg);
		}
		// Disable discuss when the student panel button is off
		else if (grain.appID == NAppID.INSTRUCTOR_PANEL) {
			if (msg.equals("DISABLE_DISCUSS_BUTTON")) {
				speak.setEnabled(false);
				topic.setText("Discuss Disabled");
				chatWindow.setText("");
			}
			else if (msg.equals("ENABLE_DISCUSS_BUTTON")) {
				speak.setEnabled(true);
				topic.setText(tempString);
			}			
		}
		else if (grain.appID == NAppID.WEB_CHAT || grain.appID == NAppID.SERVER){
			appendTextAndScroll(msg);
			Log.i("Discuss", "ChatWindow: " + msg);
			input.requestFocus();
		}
		else {
			grain = null;
		}
		if (grain != null)
			grain = null;
	}
	 
	Button.OnClickListener buttonSendOnClickListener = new Button.OnClickListener(){
		
		@Override
		public void onClick(View v) {
			// need to create new input field each time
			alertInput = new EditText(ctx);
			
			alert.setTitle("Discuss:");
//			alert.setMessage("Message");
			alert.setView(alertInput);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  String value = alertInput.getText().toString();
			  	Log.d("Discuss", value);
			  }
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
			
			alert.show();
//			String tString = input.getText().toString();
//			int tLen = tString.length();
//			byte[] tStringAsBytes = tString.getBytes();
//
//			sand.sendGrain((byte)NAppID.WEB_CHAT, (byte)NCommand.SEND_MESSAGE, (byte)NDataType.BYTE, tLen, tStringAsBytes );
//			
//			// The data 
//			Log.i("Discuss", "sending:  (" + tLen + ") of this data type");
//
//			//                for (int i=0; i<tLen; i++) {
//			//                Log.i("Discuss", "sending:  " + tString.charAt(i));
//			//                streamOut.writeByte(tString.charAt(i));
//			//                }
//
//			Log.i("Discuss", "sending: (" + tString + ")");
//			input.setText("");
////			input.requestFocus();
//			InputMethodManager imm = (InputMethodManager)getSystemService(
//				      Context.INPUT_METHOD_SERVICE);
//			imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
//
		}
	};
	
	public void setSand(NSand _sand) {
		sand = _sand;
		Log.i("Discuss", "setSand()");
	}
	
//	public void setTB(TabbedBindle _tb){
//		tabbedBindle = _tb;
//	}
	
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

}