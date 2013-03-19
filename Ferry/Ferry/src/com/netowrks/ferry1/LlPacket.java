package com.netowrks.ferry1;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LlPacket implements Serializable {
	//Sender's node ID when node sends
	//Reciever's ID when ferry forwards - so that reciever can check if the packet is his
	int fromID;
	int toID;
	int type;
	Object payload;
	
//Following elements are just for testing purposes	
	int Recv_ID;
	String ipAddr;
	int port;
}
