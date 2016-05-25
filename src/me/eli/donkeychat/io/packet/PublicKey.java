package me.eli.donkeychat.io.packet;

import java.math.BigInteger;

public class PublicKey implements Packet {
	
	private static final long serialVersionUID = 7478977627918299790L;
	private final BigInteger key;
	
	public PublicKey(BigInteger key) {
		this.key = key;
	}
	
	public BigInteger getKey() {
		return key;
	}
	
}
