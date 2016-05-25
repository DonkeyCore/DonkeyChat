package me.eli.donkeychat.io.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;

import me.eli.donkeychat.io.BigIntegerList;

public interface Packet extends Serializable {
	
	public default BigIntegerList encrypt(BigInteger sharedKey) {
		ByteArrayOutputStream bos = null;
		ObjectOutputStream out = null;
		try {
			bos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bos);
			out.writeObject(this);
			byte[] data = bos.toByteArray();
			BigInteger[] bigData = new BigInteger[data.length];
			for(int i = 0; i < data.length; i++)
				bigData[i] = sharedKey.multiply(new BigInteger("" + data[i]));
			return new BigIntegerList(bigData);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (bos != null)
					bos.close();
			} catch(IOException e) {}
			try {
				if (out != null)
					out.close();
			} catch(IOException e) {}
		}
	}
	
	public static Packet decrypt(BigIntegerList bigData, BigInteger sharedKey) {
		ByteArrayInputStream bin = null;
		ObjectInputStream in = null;
		try {
			byte[] data = new byte[bigData.get().size()];
			for(int i = 0; i < data.length; i++)
				data[i] = bigData.get().get(i).divide(sharedKey).byteValueExact();
			bin = new ByteArrayInputStream(data);
			in = new ObjectInputStream(bin);
			return (Packet) in.readObject();
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (bin != null)
					bin.close();
			} catch(IOException e) {}
			try {
				if (in != null)
					in.close();
			} catch(IOException e) {}
		}
	}
}
