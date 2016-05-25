package me.eli.donkeychat.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import me.eli.donkeychat.io.packet.Packet;
import me.eli.donkeychat.io.packet.PublicKey;

public class IOHandler implements Closeable {
	
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private BigInteger sharedKey = BigInteger.ONE;
	
	public IOHandler(InputStream in, OutputStream out) throws IOException {
		if (in == null || out == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		this.out = new ObjectOutputStream(out);
		this.in = new ObjectInputStream(in);
	}
	
	public void write(Object obj) throws IOException {
		out.writeObject(obj);
	}
	
	public void write(Packet packet) throws IOException {
		if (packet instanceof PublicKey)
			write((Object) packet);
		else
			write(packet.encrypt(sharedKey));
	}
	
	public Object read() throws IOException {
		try {
			Object o = in.readObject();
			if (o instanceof BigIntegerList)
				return Packet.decrypt((BigIntegerList) o, sharedKey);
			return o;
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ObjectOutputStream getOut() {
		return out;
	}
	
	public ObjectInputStream getIn() {
		return in;
	}
	
	public BigInteger getSharedKey() {
		return sharedKey;
	}
	
	public BigInteger setSharedKey(BigInteger sharedKey) {
		BigInteger old = this.sharedKey;
		this.sharedKey = sharedKey;
		return old;
	}
	
	@Override
	public void close() throws IOException {
		in.close();
		in = null;
		out.close();
		out = null;
	}
	
	public boolean isClosed() {
		return in == null && out == null;
	}
}
