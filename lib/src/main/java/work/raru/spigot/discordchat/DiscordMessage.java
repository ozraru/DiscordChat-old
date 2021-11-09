package work.raru.spigot.discordchat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordMessage extends ListenerAdapter{
	
	static long channelID;
	static String prefix;
	static private TextChannel channel;
	
	DiscordMessage() {
		reloadConfig();
	}
	
	static void reloadConfig() {
		DiscordMessage.channelID = ConfigManager.getDiscordChannelID();
		DiscordMessage.prefix = ConfigManager.getDiscordPrefix();
		DiscordMessage.channel = Main.instance.jda.getTextChannelById(channelID);
		webhook = new DiscordWebhook(ConfigManager.getWebhookURL());
	}
	
	static TextChannel getChannel() {
		if (channel == null) {
			channel = Main.instance.jda.getTextChannelById(channelID);
		}
		return channel;
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getChannel() != getChannel()) {
			return;
		}
		if (event.getMessage().getContentRaw().startsWith(prefix)) {
			String command = event.getMessage().getContentRaw().substring(prefix.length());
			String[] splitCommand = command.split(" ");
			switch (splitCommand[0]) {
			case "link":
				link(event.getMessage(), splitCommand);
				break;
			}
		} else {
			if (!(event.isWebhookMessage() || event.getAuthor().equals(event.getJDA().getSelfUser()))) {
				MinecraftChat.fromDiscord(event.getMember(), event.getMessage().getContentDisplay(), !event.getMessage().getAttachments().isEmpty(), event.getMessage().getReferencedMessage());
			}
		}
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().startsWith(prefix)) {
			String command = event.getMessage().getContentRaw().substring(prefix.length());
			String[] splitCommand = command.split(" ");
			switch (splitCommand[0]) {
			case "link":
				link(event.getMessage(), splitCommand);
				break;
			}
		} else {
		}
	}
	
	private void link(Message msg, String[] splitCommand) {
		if (splitCommand.length < 2) {
			msg.reply("Usage: "+prefix+"link <token>").queue();;
			return;
		}
		try {
			String minecraftUUID = UserLinkManager.useToken(msg.getMember(), splitCommand[1]);
			if (minecraftUUID != null) {
				msg.reply("Successfully linked to "+minecraftUUID).queue();
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
				String discordName = ConfigManager.ChatFormats.getDiscordUnlinkedName().replaceAll("%m", player.getDisplayName());
				webhook.setUsername(discordName);
				webhook.setAvatarUrl(null);
			} else {
				String discordName = ConfigManager.ChatFormats.getDiscordLinkedName().replaceAll("%m", player.getDisplayName());
				webhook.setUsername(discordName.replaceAll("%d", discord.getEffectiveName()));
				webhook.setAvatarUrl(discord.getUser().getEffectiveAvatarUrl());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			String discordName = ConfigManager.ChatFormats.getDiscordUnlinkedName().replaceAll("%m", player.getDisplayName());
			webhook.setUsername(discordName);
			webhook.setAvatarUrl(null);
		}
		String[] splitMessage = message.split(":");
		StringBuilder content = new StringBuilder(splitMessage[0]);
		for (int i = 1; i < splitMessage.length; i++) {
			List<Emote> emotes = getChannel().getJDA().getEmotesByName(splitMessage[i], true);
			if (emotes.isEmpty()) {
				content.append(":"+splitMessage[i]);
				if (i == splitMessage.length - 1 && message.endsWith(":")) {
					content.append(":");
				}
				continue;
			}
			if (emotes.size() == 1) {
				content.append(emotes.get(0).getAsMention());
				continue;
			}
			boolean selected = false;
			for (Emote emote : emotes) {
				if (emote.getGuild().equals(getChannel().getGuild())) {
					content.append(emote.getAsMention());
					selected = true;
					break;
				}
			}
			if (!selected) {
				content.append(emotes.get(0).getAsMention());
			}
		}
		
		webhook.setContent(content.toString());
		webhook.setTts(false);
		try {
			webhook.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
