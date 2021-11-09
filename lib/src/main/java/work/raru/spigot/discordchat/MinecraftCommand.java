package work.raru.spigot.discordchat;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.JDA.Status;

public class MinecraftCommand implements CommandExecutor {
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
			sender.sendMessage(label + " help: Show this message");
			return true;
		}
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
				sender.sendMessage(
						"Please send '" + ConfigManager.getDiscordPrefix() + "link " + token + "' in discord channnel #"
								+ DiscordMessage.getChannel().getName() + " or DM in " + expirationSeconds + " seconds");
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
			sender.sendMessage("Stopping JDA...");
			Main.instance.jda.shutdown();
			try {
				for (int i = 0; i < 30; i++) {
					if (Main.instance.jda.getStatus().equals(Status.SHUTDOWN)) {
						sender.sendMessage("Successfully shutdowned. Starting JDA...");
						Main.instance.startJDA();
						Main.instance.jda.awaitReady();
						sender.sendMessage("JDA started.");
						return true;
					}
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				sender.sendMessage("Unexpected interrupt.");
				return true;
			}
			sender.sendMessage("Shutdown timeout.s");
			return true;
		}
		default:
			sender.sendMessage("Invalid subcommand. To check usage, use: " + label + " help");
			return true;
		}
	}
}
