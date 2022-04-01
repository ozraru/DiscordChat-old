package work.raru.spigot.discordchat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
	
	private static ThreadManager instance;

	static public ThreadManager getInstance() {
		if (instance == null) {
			instance = new ThreadManager();
		}
		return instance;
	}

	ExecutorService pool = Executors.newCachedThreadPool();
	
	public void execute(Runnable command) {
		pool.execute(command);
	}
}
