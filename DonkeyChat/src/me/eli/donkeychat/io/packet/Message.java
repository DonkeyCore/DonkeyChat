package me.eli.donkeychat.io.packet;

public class Message implements Packet {
	
	private static final long serialVersionUID = -8563311612067834701L;
	private final String msg;
	
	public Message(String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return msg;
	}
	
}
