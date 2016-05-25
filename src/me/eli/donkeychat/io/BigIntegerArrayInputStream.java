package me.eli.donkeychat.io;

import java.math.BigInteger;

public class BigIntegerArrayInputStream {

	    private BigInteger[] data;
	    private int pos;
	    private int count;
	    private int mark = 0;
	    
	    public BigIntegerArrayInputStream(BigInteger[] data) {
	        this.data = data;
	        this.pos = 0;
	        this.count = data.length;
	    }
	    
	    public synchronized BigInteger read() {
	        return (pos < count) ? data[pos++].and(new BigInteger("" + 0xff)) : null;
	    }

	    public synchronized long skip(long n) {
	        long k = count - pos;
	        if (n < k)
	            k = n < 0 ? 0 : n;
	        pos += k;
	        return k;
	    }
	    
	    public synchronized int available() {
	        return count - pos;
	    }

	    public boolean markSupported() {
	        return true;
	    }

	    public void mark(int readAheadLimit) {
	        mark = pos;
	    }

	    public synchronized void reset() {
	        pos = mark;
	    }

}
