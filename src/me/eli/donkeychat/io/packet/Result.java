package me.eli.donkeychat.io.packet;

public class Result implements Packet {
	
	private static final long serialVersionUID = -5772411777260041019L;
	private final int result;
	
	public Result(int result) {
		this.result = result;
	}
	
	public int getResult() {
		return result;
	}
	
}
