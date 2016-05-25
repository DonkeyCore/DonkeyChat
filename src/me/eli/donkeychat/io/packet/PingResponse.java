package me.eli.donkeychat.io.packet;

import java.io.Serializable;

public class PingResponse implements Serializable {

	private static final long serialVersionUID = -7817394359738541970L;
	
	private final int key;
	
	public PingResponse(PingRequest request) {
		this.key = request.getKey();
	}
	
	public int getKey() {
		return key;
	}
	
}
