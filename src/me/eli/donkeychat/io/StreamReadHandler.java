package me.eli.donkeychat.io;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import me.eli.donkeychat.client.ChatServer;
import me.eli.donkeychat.server.Client;

public class StreamReadHandler {
	
	private final List<Thread> threads = new ArrayList<>();
	private final List<Future<Object>> readers = new ArrayList<>();
	private boolean running = true;
	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	private final ObjectReadListener onRead;
	
	public StreamReadHandler(ObjectReadListener onRead) {
		this.onRead = onRead;
	}
	
	public void stop() {
		running = false;
		for(Future<Object> f : readers) {
			if (f != null)
				f.cancel(true);
		}
		for(Thread t : threads) {
			if (t != null)
				t.interrupt();
		}
	}
	
	public void readFrom(ChatServer s) {
		Thread t = new Thread(() -> {
			try {
				do {
					Future<Object> f = executor.submit(() -> s.getIO().read());
					readers.add(f);
					onRead.onRead(s, f.get());
					readers.remove(f);
				} while(running);
			} catch(CancellationException | InterruptedException | ExecutionException e) {} catch(Exception e) {
				e.printStackTrace();
			}
		});
		threads.add(t);
		t.start();
	}
	
	public void readFrom(Client c) {
		Thread t = new Thread(() -> {
			try {
				do {
					Future<Object> f = executor.submit(() -> c.getIO().read());
					readers.add(f);
					onRead.onRead(c, f.get());
					readers.remove(f);
				} while(running);
			} catch(Exception e) {}
		});
		threads.add(t);
		t.start();
	}
}
