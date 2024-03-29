package work.raru.spigot.discordchat;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordMessage extends ListenerAdapter {

	static long channelID;
	static String prefix;
	static private GuildMessageChannel channel;
	
	static final Pattern emojiPattern = Pattern.compile("[a-zA-Z0-9_]{2,}");

	DiscordMessage() {
		reloadConfig();
	}

	static void reloadConfig() {
		DiscordMessage.channelID = ConfigManager.getDiscordChannelID();
		DiscordMessage.prefix = ConfigManager.getDiscordPrefix();
		DiscordMessage.channel = Main.instance.jda.getTextChannelById(channelID);
		webhook = new DiscordWebhook(ConfigManager.getWebhookURL());
	}

	static GuildMessageChannel getChannel() {
		if (channel == null) {
			channel = Main.instance.jda.getTextChannelById(channelID);
		}
		if (channel == null) {
			channel = Main.instance.jda.getThreadChannelById(channelID);
		}
		return channel;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromGuild() && event.getChannel() != getChannel()) {
			return;
		}
		if (event.getMessage().getContentRaw().startsWith(prefix)) {
			String command = event.getMessage().getContentRaw().substring(prefix.length());
			String[] splitCommand = command.split(" ");
			switch (splitCommand[0]) {
			case "link":
				link(event.getMessage(), splitCommand);
				break;
			default:
				event.getMessage().reply("Command not found").queue();
				break;
			}
		} else {
			if (event.isFromGuild() && !event.isWebhookMessage()
					&& !event.getAuthor().equals(event.getJDA().getSelfUser())) {
				MinecraftChat.fromDiscord(event.getMember(), event.getMessage().getContentDisplay(),
						!event.getMessage().getAttachments().isEmpty(), event.getMessage().getReferencedMessage());
			}
		}
	}

	private void link(Message msg, String[] splitCommand) {
		if (splitCommand.length < 2) {
			msg.reply("Usage: " + prefix + "link <token>").queue();
			;
			return;
		}
		try {
			String minecraftUUID = UserLinkManager.useToken(msg.getMember(), splitCommand[1]);
			if (minecraftUUID != null) {
				msg.reply("Successfully linked to " + minecraftUUID).queue();
			} else {
				msg.reply("Token not found").queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			msg.reply("Unexpected error has occurred in accessing database").queue();
			return;
		}
	}

	static DiscordWebhook webhook;

	static void fromMinecraft(Player player, String message) {
		try {
			Member discord = UserLinkManager.getDiscordMember(player.getUniqueId());
			if (discord == null) {
				String discordName = ConfigManager.ChatFormats.getDiscordUnlinkedName().replaceAll("%m",
						player.getDisplayName());
				webhook.setUsername(discordName);
				webhook.setAvatarUrl(null);
			} else {
				String discordName = ConfigManager.ChatFormats.getDiscordLinkedName().replaceAll("%m",
						player.getDisplayName());
				webhook.setUsername(discordName.replaceAll("%d", discord.getEffectiveName()));
				webhook.setAvatarUrl(discord.getUser().getEffectiveAvatarUrl());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			String discordName = ConfigManager.ChatFormats.getDiscordUnlinkedName().replaceAll("%m",
					player.getDisplayName());
			webhook.setUsername(discordName);
			webhook.setAvatarUrl(null);
		}
		String[] splitMessage = message.split(":");
		String result = message;
		HashSet<String> converted = new HashSet<String>();
		for (int i = 1; i < splitMessage.length; i++) {
			if (!converted.add(splitMessage[i])) {
				continue;
			}
			Matcher matcher = emojiPattern.matcher(splitMessage[i]);
			if (!matcher.matches()) {
				continue;
			}
			List<RichCustomEmoji> emojis = getChannel().getGuild().getEmojisByName(splitMessage[i], true);
			if (emojis.isEmpty()) {
				continue;
			}

			result = result.replaceAll(":"+splitMessage[i]+":", emojis.get(0).getAsMention());
		}

		webhook.setContent(result);
		webhook.setTts(false);
		ThreadManager.getInstance().execute(webhook);
	}

	static void sendWebhook(@Nullable String username, @Nonnull String message) {
		webhook.setUsername(username);
		webhook.setAvatarUrl(null);
		webhook.setContent(message);
		webhook.setTts(false);
		ThreadManager.getInstance().execute(webhook);
	}
}
