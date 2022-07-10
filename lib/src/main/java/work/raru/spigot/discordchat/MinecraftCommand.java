package work.raru.spigot.discordchat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.JDA.Status;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MinecraftCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1 || args[0].equals("help")) {
			if (sender.hasPermission("discordchat.command.link") && sender.hasPermission("discordchat.link")) {
				sender.sendMessage(label + " link: Link your accounts to Discord");
			}
			if (sender.hasPermission("discordchat.command.link") && sender.hasPermission("discordchat.link.other")) {
				sender.sendMessage(label + " link <player>: Issue other accounts token to link Discord");
			}
			if (sender.hasPermission("discordchat.command.reload")) {
				sender.sendMessage(label + " reload: Reload config file");
			}
			if (sender.hasPermission("discordchat.command.restart")) {
				sender.sendMessage(label + " restart: Restart JDA");
			}
			if (sender.hasPermission("discordchat.command.emojiful")) {
				sender.sendMessage(label + " emojiful: Generate emojiful resource pack");
			}
			sender.sendMessage(label + " help: Show this message");
			return true;
		}
		try {
			switch (args[0]) {
			case "link": {
				OfflinePlayer target;
				if (!sender.hasPermission("discordchat.command.link")) {
					sender.sendMessage("You don't have permission: discordchat.link");
					return true;
				}
				if (!sender.hasPermission("discordchat.link")) {
					sender.sendMessage("You don't have permission: discordchat.link");
					return true;
				}
				switch (args.length) {
				case 1:
					if (sender instanceof Player) {
						target = (Player) sender;
						break;
					} else {
						return false;
					}
				case 2:
					if (!sender.hasPermission("discordchat.link.other")) {
						sender.sendMessage("You don't have permission: discordchat.link.other");
						return true;
					}
					target = Utilitys.getPlayer(args[1]);
					if (target == null) {
						sender.sendMessage("Not found User");
						return false;
					}
					break;
				default:
					return false;
				}
				String token = RandomStringUtils.randomNumeric(6);
				int expirationSeconds = ConfigManager.getTokenExpire();
				try {
					UserLinkManager.linkQueue(target.getUniqueId(), token, expirationSeconds);
					String commandText = ConfigManager.getDiscordPrefix() + "link " + token;
					String messageText = String.format(
							"Please send '%s' in discord #%s or %s's DM in %d seconds (click here to copy command)",
							commandText, DiscordMessage.getChannel().getName(),
							DiscordMessage.getChannel().getGuild().getSelfMember().getEffectiveName(),
							expirationSeconds);
					TextComponent message = new TextComponent(messageText);
					message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, commandText));
					sender.spigot().sendMessage(message);
					return true;
				} catch (Exception e) {
					sender.sendMessage("Error Link queue");
					e.printStackTrace();
					DatabaseManager.cancel();
					return true;
				}
			}
			case "reload": {
				if (!sender.hasPermission("discordchat.command.reload")) {
					sender.sendMessage("You don't have permission: discordchat.command.reload");
					return true;
				}
				ConfigManager.reload();
				sender.sendMessage("Successfully reloaded");
				return true;
			}
			case "restart": {
				if (!sender.hasPermission("discordchat.command.restart")) {
					sender.sendMessage("You don't have permission: discordchat.command.restart");
					return true;
				}
				sender.sendMessage("Shutdowning JDA...");
				try {
					Main.instance.jda.shutdown();
					boolean shutdowned = false;
					for (int i = 0; i < 30; i++) {
						if (Main.instance.jda.getStatus().equals(Status.SHUTDOWN)) {
							shutdowned = true;
							break;
						}
						Thread.sleep(100);
					}
					if (shutdowned) {
						sender.sendMessage("JDA successfully shutdowned");
					} else {
						sender.sendMessage("shutdown timeout. Force shutdown...");
						Main.instance.jda.shutdownNow();
						sender.sendMessage("Force shutdown requested.");
					}
				} catch (Exception e) {
					e.printStackTrace();
					sender.sendMessage("Unknown error has occured while shutdown JDA: " + e.getMessage());
					sender.sendMessage("Stacktrace is available in server console.");
					sender.sendMessage("Ignore and continue restart...");
				}
				sender.sendMessage("Starting JDA...");
				Main.instance.startJDA();
				Main.instance.jda.awaitReady();
				sender.sendMessage("JDA started.");
				return true;
			}
			case "emojiful":
				if (!sender.hasPermission("discordchat.command.emojiful")) {
					sender.sendMessage("You don't have permission: discordchat.command.emojiful");
					return true;
				}
				EmojifulGenerator.generate();
				sender.sendMessage("Successfully generated emojiful datapack.");
				TextComponent message = new TextComponent("Click here to '/datapack enable \"file/discordchat\"'");
				message.setClickEvent(
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/datapack enable \"file/discordchat\""));
				sender.spigot().sendMessage(message);
				message.setText("Warning: this command may cause client freeze few seconds");
				sender.spigot().sendMessage(message);
				return true;
			default:
				sender.sendMessage("Invalid subcommand. To check usage, use: " + label + " help");
				return true;
			}
		} catch (IOException | LoginException | InterruptedException e) {
			e.printStackTrace();
			sender.sendMessage("Unknown error has occured: " + e.getMessage());
			sender.sendMessage("Stacktrace is available in server console.");
			return true;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> result = new ArrayList<String>();
		if (args.length == 1) {
			if (sender.hasPermission("discordchat.command.link") && sender.hasPermission("discordchat.link")) {
				result.add("link");
			}
			if (sender.hasPermission("discordchat.command.reload")) {
				result.add("reload");
			}
			if (sender.hasPermission("discordchat.command.restart")) {
				result.add("restart");
			}
			if (sender.hasPermission("discordchat.command.restart")) {
				result.add("emojiful");
			}
			result.add("help");
		}
		if (args.length == 2) {
			if (args[1].equals("link")) {
				if (sender.hasPermission("discordchat.command.link")
						&& sender.hasPermission("discordchat.link.other")) {
					return null; // Can specify player
				} else {
					// Nothing arguments to add
				}
			}
			// Other, Nothing arguments to add
		}
		return result;
	}
}
