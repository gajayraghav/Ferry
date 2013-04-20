package com.rps.utilities;


import java.io.Serializable;
import java.util.Date;


@SuppressWarnings("serial")
public class ChatMessage implements Serializable {
	private ChatMessageTypes messageType;
	private Object content;
	private Date timestamp;

	public ChatMessage(ChatMessageTypes messageType,
			Object content,Date timestamp) {
		this.messageType = messageType;
		this.content = content;
		this.timestamp = timestamp;
	}

	public ChatMessageTypes getMessageType() {
		return messageType;
	}

	public void setMessageType(ChatMessageTypes messageType) {
		this.messageType = messageType;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public Date getTimestamp() {
		return timestamp;
	}

}
