package work.raru.spigot.discordchat;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utilitys {
	static @Nullable OfflinePlayer getPlayer(CommandSender sender, @Nullable String PlayerIdentity) {
		if (PlayerIdentity == null) {
			if (sender instanceof Player) {
				return (Player) sender;
			} else {
				return null;
			}
		} else {
			return getPlayer(PlayerIdentity);
		}
	}
	
	@SuppressWarnings("deprecation")
	static @Nullable OfflinePlayer getPlayer(@Nonnull String PlayerIdentity) {
		try {
			UUID uuid = UUID.fromString(PlayerIdentity);
			return Main.instance.getServer().getOfflinePlayer(uuid);
		} catch (IllegalArgumentException e) {
		}
		return Main.instance.getServer().getOfflinePlayer(PlayerIdentity);
	}
}
