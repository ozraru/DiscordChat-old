package work.raru.spigot.discordchat;

import java.sql.SQLException;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class MinecraftChat implements Listener {
	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent e) {
		DiscordMessage.fromMinecraft(e.getPlayer(), e.getMessage());
	}

	static void fromDiscord(Member member, String message, boolean haveAttachment, Message referencedMessage) {
		String content;
		String minecraftName = null;
		try {
			UUID minecraftUUID = UserLinkManager.getMinecraftUUID(member.getIdLong());
			if (minecraftUUID != null) {
				OfflinePlayer minecraft = Main.instance.getServer().getOfflinePlayer(minecraftUUID);
				if (minecraft != null) {
					minecraftName = minecraft.getName();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (minecraftName == null) {
			content = ConfigManager.ChatFormats.getMinecraftFormat(false, referencedMessage != null, haveAttachment);
		} else {
			content = ConfigManager.ChatFormats.getMinecraftFormat(true, referencedMessage != null, haveAttachment);
			content = content.replaceAll("%m", minecraftName);
		}
		content = content.replaceAll("%d", member.getEffectiveName());
		content = content.replaceAll("%t", message);
		if (referencedMessage != null) {
			try {
				UUID minecraftUUID = UserLinkManager.getMinecraftUUID(member.getIdLong());
				if (minecraftUUID != null) {
					OfflinePlayer minecraft = Main.instance.getServer().getOfflinePlayer(minecraftUUID);
					if (minecraft != null) {
						content = content.replaceAll("%rm", minecraft.getName());
						content = content.replaceAll("%rn", minecraft.getName());
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			content = content.replaceAll("%rm", "");
			content = content.replaceAll("%rn", referencedMessage.getMember().getEffectiveName());
			content = content.replaceAll("%rd", referencedMessage.getMember().getEffectiveName());
			content = content.replaceAll("%rt", referencedMessage.getContentDisplay());
		}
		content = EmojiParser.parseToAliases(content);
		content = MarkdownConverter.toMinecraft(content);
		Main.instance.getServer().broadcast(content, "discordchat.recieve");
	}

	/**
	 * 
	 * @param player
	 * @param message
	 * @return Is player exist
	 */
	static boolean tell(String player, String message) {
		return tell(UUID.fromString(player), message);
	}

	/**
	 * 
	 * @param player
	 * @param message
	 * @return Is player exist
	 */
	static boolean tell(UUID player, String message) {
		Player target = Main.instance.getServer().getPlayer(player);
		if (target == null) {
			return false;
		} else {
			tell(Main.instance.getServer().getPlayer(player), message);
			return true;
		}
	}

	static void tell(@Nonnull Player player, String message) {
		player.sendMessage(message);
	}
}
