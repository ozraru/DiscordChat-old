package work.raru.spigot.discordchat;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Member;

public class UserLinkManager {
	public static void linkQueue(@Nonnull UUID minecraftUUID, @Nonnull String token, @Nonnull int expireSeconds)
			throws SQLException {
		DatabaseManager.createToken(minecraftUUID, token,
				Timestamp.valueOf(LocalDateTime.now().plusSeconds(expireSeconds)));
	}

	public static String useToken(@Nonnull Member member, @Nonnull String token) throws SQLException {
		String minecraft = DatabaseManager.useToken(token);
		if (minecraft == null) {
			return null;
		}
		linkWrite(minecraft, member);
		DatabaseManager.confirm();
		return minecraft;
	}

	public static void linkWrite(@Nonnull String minecraftUUID, @Nonnull Member member) throws SQLException {
		unlink(member);
		unlink(minecraftUUID);
		DatabaseManager.addLink(minecraftUUID, member.getIdLong());
		DatabaseManager.confirm();
		MinecraftChat.tell(minecraftUUID, "You are linked by "+member.getEffectiveName()+"("+member.getIdLong()+")");
	}

	public static void unlink(UUID minecraft) throws SQLException {
		DatabaseManager.removeLink(minecraft);
		DatabaseManager.confirm();
	}

	public static void unlink(String minecraft) throws SQLException {
		DatabaseManager.removeLink(minecraft);
		DatabaseManager.confirm();
	}

	public static void unlink(Member member) throws SQLException {
		DatabaseManager.removeLink(member.getIdLong());
		DatabaseManager.confirm();
	}

	public static Member getDiscordMember(UUID minecraftUUID) throws SQLException {
		try {
			long discordId = getDiscordId(minecraftUUID);
			if (discordId > 0) {
				return DiscordMessage.getChannel().getGuild().retrieveMemberById(getDiscordId(minecraftUUID)).complete();
			} else {
				return null;
			}
		} finally {
			DatabaseManager.confirm();
		}
	}

	public static long getDiscordId(UUID minecraftUUID) throws SQLException {
		try {
			return DatabaseManager.getDiscord(minecraftUUID);
		} finally {
			DatabaseManager.confirm();
		}
	}

	public static UUID getMinecraftUUID(Member member) throws SQLException {
		return getMinecraftUUID(member.getIdLong());
	}

	public static UUID getMinecraftUUID(long discord) throws SQLException {
		try {
			String stringUUID = DatabaseManager.getMinecraft(discord);
			if (stringUUID != null) {
				return UUID.fromString(stringUUID);
			} else {
				return null;
			}
		} finally {
			DatabaseManager.confirm();
		}
	}
}
