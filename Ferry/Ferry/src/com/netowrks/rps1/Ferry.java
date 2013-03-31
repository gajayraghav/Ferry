package com.netowrks.rps1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
List<LlPacket> messageBuffer = new ArrayList<LlPacket>();
final HashMap <String,String> Nodeidlookup = new HashMap<String, String>();
final HashMap <String,String> Phonelookup = new HashMap<String, String>();
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
		Button Updates =(Button) findViewById(R.id.Updates);

		textOut = (TextView) findViewById(R.id.textout);
		buttonSend.setOnClickListener(buttonSendOnClickListener);
		connWifi.setOnClickListener(buttonConnWifiOnClickListener);
		Updates.setOnClickListener(buttonUpdatesOnClickListener);
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

// This button takes care of updating the DB
private Button.OnClickListener buttonUpdatesOnClickListener = new Button.OnClickListener() {
	public void onClick(View arg0) 
	{
		
         final String urlbuilder="http://www.klusterkloud.com/RPS/api/getall.json";
         Log.v("URL", urlbuilder.toString());
         Thread thread = new Thread()
         {
             @Override
             public void run() {
                 try {
                 	try {
                 	    HttpClient client = new DefaultHttpClient();  
                 	   // String getURL = "http://www.google.com";
                 	    HttpGet get = new HttpGet(urlbuilder);
                 	    HttpResponse responseGet = client.execute(get);  
                 	    HttpEntity resEntityGet = responseGet.getEntity();  
                 	    if (resEntityGet != null) {  
                 	        // do something with the response
                 	        String response = EntityUtils.toString(resEntityGet);
                 	       Log.i("GET RESPONSE", response);
                 	     // JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                 	       JSONArray Nodeids = new JSONArray(response);
                 	       for (int i=0;i<Nodeids.length();i++)
                 	       {
                 	    	   String temp = Nodeids.get(i).toString();
                 	    	  Log.i("temp",temp);
                 	    	   //JSONArray array = new JSONArray(temp);
                 	    	   JSONObject obj = (JSONObject)Nodeids.get(i);
                 	    	   String id = obj.get("NodeID").toString();
                 	    	   String ph = obj.get("Phone").toString();
                 	    	   
                 	    	   
                 	       Log.i("id",id);
                 	      Log.i("ph",ph);
                 	     Nodeidlookup.put(ph,id);
                 	     Phonelookup.put(id,ph);
                 	       }
                 	      for (String key : Nodeidlookup.keySet()) {
                 			  String msg = "" + key + ":" + Nodeidlookup.get(key); 
                 	    	  Log.i("Nodeidlookup",msg);
                 			}
                 	     for (String key : Phonelookup.keySet()) {
                 			 
                 			  String msg = "" + key + ":" + Phonelookup.get(key); 
                 	    	  Log.i("Phonelookup",msg);
                 			}
                 	    }
                 	
                 	            //TextView tv = (TextView)findViewById(R.);
                 	    		String a=PhonefromNodeID("$1$nJn33kxP$tWllqimRowa2tjtyVeQbX0");
                 	    		String b=NodeIDfromPhone("95956494");
                 	    		Log.i("Sample NodeID Lookup",a);
                 	    		Log.i("Sample Phone Lookup",b);

                 	            }catch(Exception e)
                 	            {
                 	                Log.e("Exception","Exception occured in writing");
                 	               e.printStackTrace();
                 	            }
                 	        }
                 	    catch(Exception e)
                 	    {
                 	    	e.printStackTrace();	
                 	    }
                 	    
                 	            
                 	            
                 	        
                 	       
                 	      //  Log.i("GET RESPONSE", response);
                 	
             }
         };

         thread.start(); 

     
	}
};
//API for lookup of NodeID and Phone number
public String NodeIDfromPhone(String Phone)
{
	
	return Nodeidlookup.get(Phone);
}

public String PhonefromNodeID(String NodeID)
{
	
	return Phonelookup.get(NodeID);
}

/* This function takes care of toggling the Wifi State */
private Button.OnClickListener buttonConnWifiOnClickListener = new Button.OnClickListener() {
	public void onClick(View arg0) 
	{
		Iterator<Entry<String, String>> gpsIter = Nodeidlookup.entrySet().iterator();
		
		while (gpsIter.hasNext()) {
			String ID = gpsIter.next().getValue();
			String Ph = PhonefromNodeID(ID);
			if (Ph.equals("4049834075")) {
				textOut.append("\n Ph:" + Ph + "ID: " + ID);
			}
		}
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

							/* Find and store the sender's phNo and recvers nodeID */
							recv_pkt.Send_No = PhonefromNodeID(recv_pkt.fromID);
							recv_pkt.toID = NodeIDfromPhone(recv_pkt.Recv_No);
							
							/* Drop packet if either the sender or receiver is not registered (ignore recvID for gps) */
							if (recv_pkt.Send_No == null || (recv_pkt.toID == null && recv_pkt.type != 1)) {
								return;
							}
							
							switch (recv_pkt.type) {
							case 0:
								recv_pkt.fromID = LowerLayer.nodeID;
								
								// Call the Archana's method
								textOut.append("\n He:"
										+ recv_pkt.payload.toString());
								
								/* Store the packet in message buffer */
								messageBuffer.add(recv_pkt);
								break;
							case 1:
								/* When a GPS coordinate is received at the ferry */
								Toast.makeText(getApplicationContext(), recv_pkt.payload.toString(), Toast.LENGTH_LONG).show();
								/* Store the gps data */
								gpsList.put(recv_pkt.fromID, recv_pkt.payload.toString());
								
								/* Send out the entire GPS list to this node */ 
								LowerLayer.SendHelper Send_instance = Ll_instance.new SendHelper();
								LlPacket sendPkt = new LlPacket();
								sendPkt.fromID = LowerLayer.nodeID;
								sendPkt.toID = recv_pkt.fromID;
								sendPkt.payload = gpsList;
								sendPkt.type = 2;
								Send_instance.execute(sendPkt);
								
								/* Send out messages that are waiting for this node */
								Iterator<LlPacket> queueIterator = messageBuffer
										.iterator();
								
								/* Send out all the buffered packet for this node */
								while (queueIterator.hasNext()){
									LlPacket new_sendPkt = queueIterator.next();
									if (sendPkt.toID == recv_pkt.fromID) {
										LowerLayer.SendHelper Send_instance_temp = Ll_instance.new SendHelper();
										Send_instance_temp.execute(new_sendPkt);
									}
									/* Remove data from ferry */
									queueIterator.remove();
								}
								
								break;
							case 2:
								// Ferry ignores GPS lists
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
