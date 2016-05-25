package me.eli.donkeychat.io.packet;

public class Nickname implements Packet {
	
	private static final long serialVersionUID = -1620233923525520353L;
	private final String nickname;
	
	public Nickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getNickname() {
		return nickname;
	}
	
}
