package me.eli.donkeychat.io.packet;

import java.io.Serializable;
import java.util.Random;

public class PingRequest implements Serializable {

	private static final long serialVersionUID = -7772351995238692458L;	
	
	private final int key;
	
	public PingRequest() {
		this.key = new Random().nextInt();
	}
	
	public int getKey() {
		return key;
	}
	
}
