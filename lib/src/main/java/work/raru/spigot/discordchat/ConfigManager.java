package work.raru.spigot.discordchat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
	
	static FileConfiguration config;
	
	static void init() {
		ConfigManager.config = Main.instance.getConfig();
		ConfigManager.ChatFormats.config = config.getConfigurationSection("chat_format");
	}
	
	static void reload() {
		Main.instance.reloadConfig();
		config = Main.instance.getConfig();
		DiscordMessage.reloadConfig();
	}
	
	static String getToken() {
		return config.getString("discord.token");
	}

	static long getDiscordChannelID() {
		return config.getLong("discord.channel");
	}
	static String getDiscordPrefix() {
		return config.getString("discord.prefix");
	}
	static String getWebhookURL() {
		return config.getString("discord.webhook");
	}
//	static String getInDiscordName() {
//		return config.getString("discord.chat_name");
//	}
	
	static int getTokenExpire() {
		return config.getInt("link.tokenExpire");
	}

	static String getDatabaseType() {
		return config.getString("database.type");
	}
	static String getDatabaseUrl() {
		return config.getString("database.url");
	}
	static String getDatabaseUser() {
		return config.getString("database.user");
	}
	static String getDatabasePassword() {
		return config.getString("database.password");
	}
	static String getDatabaseTablePrefix() {
		return config.getString("database.table_prefix");
	}
	
//	static ConfigurationSection getMinecraftConvert() {
//		return config.getConfigurationSection("minecraft");
//	}
	
	static class ChatFormats {
		
		public static ConfigurationSection config;

		static String getDiscordLinkedName() {
			return config.getString("discord.linked_name");
		}
		static String getDiscordUnlinkedName() {
			return config.getString("discord.unlinked_name");
		}

		static String getMinecraftFormat(boolean linked, boolean reply, boolean attachment) {
			StringBuilder key = new StringBuilder("minecraft.");
			if (reply) {
				if (attachment) {
					key.append("reply_attachment.");
				} else {
					key.append("reply.");
				}
			} else {
				if (attachment) {
					key.append("attachment.");
				} else {
					key.append("normal.");
				}
			}
			if (linked) {
				key.append("linked");
			} else {
				key.append("unlinked");
			}
			return config.getString(key.toString());
		}
	}
}
