package com.netowrks.rps1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.netowrks.rps1.R;
import com.netowrks.rps1.LowerLayer.RecieveHelper;
import com.netowrks.rps1.LowerLayer.SendHelper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Ferry extends Activity {// extends Utility {

	TextView textOut;
	EditText textIn, ipIn, portIn;
	private Handler handler = new Handler();
	LowerLayer Ll_instance = new LowerLayer();
	LowerLayer.RecieveHelper receiveInstance = Ll_instance.new RecieveHelper();
	HashMap<String, String> gpsList = new HashMap<String, String>();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.basic_chat);

			textIn = (EditText) findViewById(R.id.textin);
			ipIn = (EditText) findViewById(R.id.ipin);
			portIn = (EditText) findViewById(R.id.portin);

			Button buttonSend = (Button) findViewById(R.id.send);
			Button connWifi = (Button) findViewById(R.id.connWifi);

			textOut = (TextView) findViewById(R.id.textout);
			buttonSend.setOnClickListener(buttonSendOnClickListener);
			connWifi.setOnClickListener(buttonConnWifiOnClickListener);
			Thread fst = new Thread(new BasicReciever());
			fst.start();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/* This function takes care of the sending of chat message */
	private Button.OnClickListener buttonSendOnClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {

			Socket socket = null;
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			try {
				LowerLayer.SendHelper Send_instance = Ll_instance.new SendHelper();
				LlPacket send_pkt = new LlPacket();
				send_pkt.payload = textIn.getText().toString();// ChatMessage
				send_pkt.type = 0;
				send_pkt.Recv_No = "0";
				send_pkt.toID = "2533"; /* Statically configured for testing */
				Send_instance.execute(send_pkt);
				textOut.append("\n Me:" + send_pkt.payload);
			}

			finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (dataOutputStream != null) {
					try {
						dataOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	};

	/* This function takes care of toggling the Wifi State */
	private Button.OnClickListener buttonConnWifiOnClickListener = new Button.OnClickListener() {
		public void onClick(View arg0) {
		}
	};

	/*
	 * This is a function that runs in the background waiting for incoming chat
	 * messages
	 */
	private class BasicReciever implements Runnable {
		// @Override
		public void run() {
			try {
				while (true) {
					// Call receive function only if the wifi is connected to
					// some network
					final LlPacket recv_pkt = receiveInstance.doInBackground();
					// runOnUiThread(new Runnable() {
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (recv_pkt != null) {

								switch (recv_pkt.type) {
								case 0:
									// Call the Archana's method
									textOut.append("\n He:"
											+ recv_pkt.payload.toString());
									break;
								case 1:

									Toast.makeText(getApplicationContext(), recv_pkt.payload.toString(), Toast.LENGTH_LONG).show();
									gpsList.put(recv_pkt.fromID, recv_pkt.payload.toString());
									LowerLayer.SendHelper Send_instance = Ll_instance.new SendHelper();
									LlPacket sendPkt = new LlPacket();
									sendPkt.fromID = LowerLayer.nodeID;
									sendPkt.toID = recv_pkt.fromID;
									sendPkt.payload = gpsList;
									sendPkt.type = 2;
									Send_instance.execute(sendPkt);
									break;
								case 2:
									// Call Ajay's method
									break;
								default:
									break;
								}
							}
						}
					});
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
