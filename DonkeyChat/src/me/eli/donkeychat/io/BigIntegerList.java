package me.eli.donkeychat.io;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class BigIntegerList implements Serializable {
	
	private static final long serialVersionUID = 960934154427916161L;
	private final List<BigInteger> l;
	
	public BigIntegerList(BigInteger[] arr) {
		this(Arrays.asList(arr));
	}
	
	public BigIntegerList(List<BigInteger> l) {
		this.l = l;
	}
	
	public List<BigInteger> get() {
		return l;
	}
	
}
