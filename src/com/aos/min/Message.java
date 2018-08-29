package com.aos.min;

import java.io.Serializable;

public class Message implements Serializable {
	private Integer Id;
	private Integer MessageType;
	public int phase;
	
	public Message(Integer Id,Integer MessageType) {
		this.Id = Id;
		this.MessageType = MessageType;
		this.phase = -1;
	}
	public Integer getMessageType() {
		return MessageType;
	}
	public Integer getID() {
		return Id;
	}
	
}
