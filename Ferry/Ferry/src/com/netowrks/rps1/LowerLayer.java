package com.netowrks.rps1;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

@SuppressLint("UseSparseArrays")
public class LowerLayer {

	private int nodeID = 0;
	final int port = 8888;
	private ServerSocket servSock;
	static HashMap<Integer, String> nodeID_IPAddrs = new HashMap<Integer, String>();
	static private List<LlPacket> outputQueue = new ArrayList<LlPacket>();
	static int availableBuffer = 10000000; //10MB
	static int instanceCount = 0;

	LowerLayer() {
		try {
			servSock = new ServerSocket(port);
			if (instanceCount++ == 0) {
				Thread fst = new Thread(new QueueHandler());
				fst.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void Ll_close() {
		try {
			// make sure you close the socket upon exiting
			if (servSock != null) {
				servSock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class SendHelper extends AsyncTask<LlPacket, Void, Void> {
		protected Void doInBackground(LlPacket... params) {
			/* Create the out going packet */
			LlPacket sendPkt = new LlPacket();
			try {

				/*
				 * Fill the outgoing packet : Note - this needs to come above
				 * the socket creating part
				 */
				sendPkt.fromID = nodeID;
				sendPkt.toID = params[0].toID;
				sendPkt.payload = params[0].payload;
				sendPkt.type = params[0].type;

				/* Create and prepare sending socket */
				String ipAddr = nodeID_IPAddrs.get(sendPkt.toID);
				Socket sendSock = new Socket(ipAddr, port);
				OutputStream os = sendSock.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);

				oos.writeObject(sendPkt);

				/* Close */
				oos.close();
				os.close();
				sendSock.close();

				return null;
			} catch (Exception e) {
				e.printStackTrace();
				/*
				 * Store the packet for later transmission if something goes
				 * wrong
				 */
				if (availableBuffer - sendPkt.toString().length() > 0
						&& sendPkt != null) {
					outputQueue.add(sendPkt);
				}
				return null;
			}
		}
	}

	public class RecieveHelper extends AsyncTask<Void, Integer, LlPacket> {
		@Override
		protected LlPacket doInBackground(Void... params) {

			/* Creating a new variable for receiving is necessary */
			LlPacket sendToMl = new LlPacket();

			try {
				if (servSock == null) {
					servSock = new ServerSocket(port);
				}
				Socket receiveSock = servSock.accept();
				InputStream is = receiveSock.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				LlPacket recvPkt = (LlPacket) ois.readObject();

				/* Check if the toID matches its own nodeID */
				if (recvPkt != null && recvPkt.toID == nodeID) {
					String ipAddr = receiveSock.getRemoteSocketAddress()
							.toString();
					ipAddr = ipAddr.substring(1, ipAddr.indexOf(":"));
					nodeID_IPAddrs.put(recvPkt.fromID, ipAddr);
					sendToMl = recvPkt;
					
				/* Else this is a wrongly delivered packet. Return null. */
				} else {
					// Do nothing, may be go ahead and scold the sender :P
				}

				is.close();
				receiveSock.close();

			} catch (Exception e) {
				System.out.println("Error: "+e.toString());
				e.printStackTrace();
				return null;
			}

			/* Send the received packet content to the middle layer */
			return sendToMl;
		}

	}

	/*
	 * This is a function that runs in the background waiting to send out
	 * packets in the output buffer
	 */
	private class QueueHandler implements Runnable {
		// @Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(2 * 1000);
					Iterator<LlPacket> queueIterator = outputQueue.iterator();
					while (queueIterator.hasNext()) {
						LlPacket out = queueIterator.next();
						/* Create and prepare sending socket */
						String ipAddr = nodeID_IPAddrs.get(out.toID);
						Socket sendSock = new Socket(ipAddr, port);
						OutputStream os = sendSock.getOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(os);
						oos.writeObject(out);
						/* Close */
						oos.close();
						os.close();
						sendSock.close();
						queueIterator.remove();
						availableBuffer += out.toString().length();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* This functionality is not required for the ferry */
/*	
	private String intToIp(int ipAddrs) {

		return ((ipAddrs & 0xFF) + "." + ((ipAddrs >> 8) & 0xFF) + "."
				+ ((ipAddrs >> 16) & 0xFF) + "." + ((ipAddrs >> 24) & 0xFF));
	}
*/
}