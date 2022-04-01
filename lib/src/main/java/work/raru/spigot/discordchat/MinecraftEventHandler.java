package work.raru.spigot.discordchat;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class MinecraftEventHandler implements Listener {
	@EventHandler
	public void loginEvent(PlayerLoginEvent e) {
		try {
			if (e.getPlayer().hasPermission("discordchat.login")) {
				e.allow();
				return;
			} else {
				if (e.getPlayer().hasPermission("discordchat.login.linked")) {
					if (UserLinkManager.getDiscordId(e.getPlayer().getUniqueId()) > 0) {
						e.allow();
						return;
					} else {
						e.disallow(Result.KICK_WHITELIST, "You have to link Discord before login.");
					}
				} else {
					e.disallow(Result.KICK_BANNED, "You are not allowed to login. By DiscordChat");
				}
				if (e.getPlayer().hasPermission("discordchat.link.login")) {
					String token = RandomStringUtils.randomNumeric(6);
					int expirationSeconds = ConfigManager.getTokenExpire();
					UserLinkManager.linkQueue(e.getPlayer().getUniqueId(), token, expirationSeconds);
					e.disallow(Result.KICK_WHITELIST, 
							"Please send '" + ConfigManager.getDiscordPrefix() + "link " + token + "' in discord channnel #"
									+ DiscordMessage.getChannel().getName() + " or DM in " + expirationSeconds + " seconds");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			e.disallow(Result.KICK_OTHER, "Sorry, unknown error occured in DiscordChat. Please call Administrator.");
		}
	}
	

	@EventHandler
	public void joinEvent(PlayerJoinEvent e) {
		if (e.getJoinMessage() != null) {
			DiscordMessage.sendWebhook(null, e.getJoinMessage().replaceAll("§.", ""));
		}
	}

	@EventHandler
	public void quitEvent(PlayerQuitEvent e) {
		if (e.getQuitMessage() != null) {
			DiscordMessage.sendWebhook(null, e.getQuitMessage().replaceAll("§.", ""));
		}
	}
	
	@EventHandler
	public void deathEvent(PlayerDeathEvent e) {
		if (e.getDeathMessage() != null) {
			DiscordMessage.sendWebhook(null, e.getDeathMessage().replaceAll("§.", ""));
		}
	}
}
