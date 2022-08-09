package work.raru.spigot.discordchat;

public class Logger {
	static void debug(String string) {
		if (ConfigManager.getDebug()) {
			Main.instance.getLogger().info(string);
		}
	}
}
