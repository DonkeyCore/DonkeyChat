package me.eli.donkeychat.io.packet;

public class Disconnect implements Packet {
	
	private static final long serialVersionUID = 176628328100795906L;
	private final String reason;
	private final boolean kick;
	
	public Disconnect(String reason) {
		this(reason, false);
	}
	
	public Disconnect(String reason, boolean kick) {
		this.reason = reason;
		this.kick = kick;
	}
	
	public String getReason() {
		return reason;
	}
	
	public boolean isKick() {
		return kick;
	}
	
}
