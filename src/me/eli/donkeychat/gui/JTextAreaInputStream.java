package me.eli.donkeychat.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.swing.JTextArea;

public class JTextAreaInputStream extends InputStream {
	
	private byte[] contents;
	private int pointer = 0;
	private boolean closed = false;
	private int mark = -1;
	private int limit = -1;
	
	public JTextAreaInputStream(final JTextArea in) {
		in.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyChar() == '\n' && !e.isShiftDown()) {
					contents = in.getText().getBytes();
					pointer = 0;
					in.setText("");
				} else if(e.getKeyCode() == '\n')
					in.setText(in.getText() + "\n");
			}
		});
	}
	
	@Override
	public int read() throws IOException {
		if(closed)
			throw new IOException("Stream closed");
		while(contents == null) {
			try {
				Thread.sleep(1);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(pointer == contents.length)
			return '\n';
		if(pointer > contents.length) {
			contents = null;
			return -1;
		}
		if(limit > 0)
			limit--;
		if(limit == 0) {
			mark = -1;
			limit = -1;
		}
		return this.contents[pointer++];
	}
	
	public byte[] getBytes() {
		return Arrays.copyOf(contents, contents.length);
	}
	
	@Override
	public void close() throws IOException {
		if(closed)
			throw new IOException("Stream closed");
		closed = true;
	}
	
	@Override
	public synchronized void mark(final int limit) {
		if(limit < 0)
			throw new IllegalArgumentException();
		this.mark = pointer;
		this.limit = limit;
	}
	
	@Override
	public synchronized void reset() throws IOException {
		if(limit == -1)
			throw new IOException("No mark set");
		pointer = mark;
		limit = -1;
		limit = -1;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
}
