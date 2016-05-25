package me.eli.donkeychat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ClientAcceptor {
	
	private final Thread t;
	private boolean running = true;
	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	private Future<Socket> acceptor;
	
	public ClientAcceptor(ServerSocket server, Consumer<Socket> onAccept) {
		t = new Thread(() -> {
			try {
				do {
					acceptor = executor.submit(() -> server.accept());
					onAccept.accept(acceptor.get());
				} while(running);
			} catch(CancellationException | InterruptedException e) {} catch(ExecutionException e) {
				e.printStackTrace();
			}
		});
		t.start();
	}
	
	public void stop() {
		running = false;
		if (acceptor != null)
			acceptor.cancel(true);
		if (t != null)
			t.interrupt();
	}
}
