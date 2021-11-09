package work.raru.spigot.discordchat;

import javax.security.auth.login.LoginException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Main extends JavaPlugin {
	
	static Main instance;
	JDA jda;
	
	@Override
	public void onEnable() {
		getLogger().info("DiscordChat enabling...");
		instance = this;
		saveDefaultConfig();
		ConfigManager.init();
		getCommand("discordchat").setExecutor(new MinecraftCommand());
		DatabaseManager.init(getDataFolder());
		startJDA();
		getServer().getPluginManager().registerEvents(new MinecraftChat(), this);
		getServer().getPluginManager().registerEvents(new LoginHandler(), this);
		super.onEnable();
	}
	
	void startJDA() {
		try {
			jda = JDABuilder.createDefault(ConfigManager.getToken()).build();
			jda.addEventListener(new DiscordMessage());
		} catch (LoginException e) {
			getLogger().severe("Discord login failed. Please check token.");
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void onDisable() {
		jda.shutdown();
		DatabaseManager.disconnect();
		super.onDisable();
	}
}
