package work.raru.spigot.discordchat;

import javax.security.auth.login.LoginException;

import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main extends JavaPlugin {

	static Main instance;
	JDA jda;

	@Override
	public void onEnable() {
		getLogger().info("DiscordChat enabling...");
		try {
			instance = this;
			saveDefaultConfig();
			ConfigManager.init();
			Logger.debug("Loading DB...");
			DatabaseManager.init();
			Logger.debug("Starting JDA...");
			startJDA();
			Logger.debug("Setuping Minecraft commands...");
			MinecraftCommand mcCommand = new MinecraftCommand();
			getCommand("discordchat").setExecutor(mcCommand);
			getCommand("discordchat").setTabCompleter(mcCommand);
			getServer().getPluginManager().registerEvents(new MinecraftChat(), this);
			getServer().getPluginManager().registerEvents(new MinecraftEventHandler(), this);
			Logger.debug("Waiting for JDA...");
			jda.awaitReady();
			Logger.debug("JDA ready");
			DiscordMessage.getChannel().sendMessage("Plugin enabled").queue();
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					DiscordMessage.getChannel().sendMessage("Server started").queue();
				}
			});
			getLogger().info("DiscordChat enabled");
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().severe("Unknown error occured in enabling DiscordChat. disabling...");
			setEnabled(false);
		}
		super.onEnable();
	}

	void startJDA() throws LoginException {
		try {
			JDABuilder jdaBuilder = JDABuilder.createDefault(ConfigManager.getToken());
			jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
			jda = jdaBuilder.build();
			jda.addEventListener(new DiscordMessage());
		} catch (LoginException e) {
			getLogger().severe("Discord login failed. Please check token.");
			throw e;
		}
	}

	@Override
	public void onDisable() {
		DiscordMessage.getChannel().sendMessage("Plugin disabled (perhaps stopping server)").queue();
		jda.shutdown();
		DatabaseManager.disconnect();
		super.onDisable();
	}
}
