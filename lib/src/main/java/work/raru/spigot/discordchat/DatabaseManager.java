package work.raru.spigot.discordchat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class DatabaseManager {
	static Connection conn;

	static void init() throws SQLException {
		initConnection();
		String linkListTableName = ConfigManager.getDatabaseTablePrefix() + "LinkList";
		String linkTokenTableName = ConfigManager.getDatabaseTablePrefix() + "LinkToken";
		initTable(linkListTableName + "(" + "minecraft CHAR(36) NOT NULL," + "discord BIGINT NOT NULL)");
		initTable(linkTokenTableName + "(" + "minecraft CHAR(36) NOT NULL," + "token VARCHAR(12) NOT NULL,"
				+ "expireTimeStamp TIMESTAMP NOT NULL)");

		cleanupStatement = conn.prepareStatement("DELETE FROM " + linkTokenTableName + " WHERE expireTimeStamp > ?");
		createTokenStatement = conn.prepareStatement(
				"INSERT INTO " + linkTokenTableName + " (minecraft,token,expireTimeStamp) VALUES (?,?,?)");
		useTokenStatement = conn.prepareStatement("SELECT minecraft FROM " + linkTokenTableName + " WHERE token = ?");
		removeTokenStatement = conn.prepareStatement("DELETE FROM " + linkTokenTableName + " WHERE token = ?");
		addLinkStatement = conn
				.prepareStatement("INSERT INTO " + linkListTableName + " (minecraft,discord) VALUES (?,?)");
		getMinecraftStatement = conn
				.prepareStatement("SELECT minecraft FROM " + linkListTableName + " WHERE discord = ?");
		getDiscordStatement = conn
				.prepareStatement("SELECT discord FROM " + linkListTableName + " WHERE minecraft = ?");
		removeLinkMinecraftStatement = conn
				.prepareStatement("DELETE FROM " + linkListTableName + " WHERE minecraft = ?");
		removeLinkDiscordStatement = conn.prepareStatement("DELETE FROM " + linkListTableName + " WHERE discord = ?");
	}

	static void initConnection() throws SQLException {
		try {
			switch (ConfigManager.getDatabaseType()) {
			case "postgresql":
				Class.forName("org.postgresql.Driver");
				conn = DriverManager.getConnection(ConfigManager.getDatabaseUrl(), ConfigManager.getDatabaseUser(),
						ConfigManager.getDatabasePassword());
				break;
			case "sqlite":
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection(ConfigManager.getDatabaseUrl());
				break;
			default:
				throw new IllegalArgumentException("Unknown database type");
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn.setAutoCommit(false);
	}

	static void initTable(String tableSettings) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.execute("CREATE TABLE IF NOT EXISTS " + tableSettings);
		stmt.close();
		conn.commit();
	}

	static private PreparedStatement cleanupStatement;

	static void cleanup() throws SQLException {
		cleanupStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
		cleanupStatement.executeUpdate();
		cleanupStatement.clearParameters();
	}

	static private PreparedStatement createTokenStatement;

	public static void createToken(UUID minecraft, String token, Timestamp expire) throws SQLException {
		createToken(minecraft.toString(), token, expire);
	}

	public static void createToken(String minecraft, String token, Timestamp expire) throws SQLException {
		createTokenStatement.setString(1, minecraft);
		createTokenStatement.setString(2, token);
		createTokenStatement.setTimestamp(3, expire);
		createTokenStatement.executeUpdate();
		createTokenStatement.clearParameters();
	}

	static private PreparedStatement useTokenStatement;
	static private PreparedStatement removeTokenStatement;

	public static String useToken(String token) throws SQLException {
		useTokenStatement.setString(1, token);
		ResultSet result = useTokenStatement.executeQuery();
		useTokenStatement.clearParameters();
		try {
			if (result.next()) {
				String minecraft = result.getString("minecraft");
				removeTokenStatement.setString(1, token);
				removeTokenStatement.executeUpdate();
				removeTokenStatement.clearParameters();
				return minecraft;
			} else {
				return null;
			}
		} finally {
			result.close();
		}
	}

	static private PreparedStatement addLinkStatement;

	public static void addLink(UUID minecraft, long discord) throws SQLException {
		addLink(minecraft.toString(), discord);
	}

	public static void addLink(String minecraft, long discord) throws SQLException {
		addLinkStatement.setString(1, minecraft);
		addLinkStatement.setLong(2, discord);
		addLinkStatement.executeUpdate();
		addLinkStatement.clearParameters();
	}

	static private PreparedStatement getMinecraftStatement;

	public static String getMinecraft(long discord) throws SQLException {
		getMinecraftStatement.setLong(1, discord);
		ResultSet result = getMinecraftStatement.executeQuery();
		getMinecraftStatement.clearParameters();
		try {
			if (result.next()) {
				return result.getString("minecraft");
			} else {
				return null;
			}
		} finally {
			result.close();
		}
	}

	static private PreparedStatement getDiscordStatement;

	public static long getDiscord(UUID minecraft) throws SQLException {
		return getDiscord(minecraft.toString());
	}

	public static long getDiscord(String minecraft) throws SQLException {
		getDiscordStatement.setString(1, minecraft);
		ResultSet result = getDiscordStatement.executeQuery();
		getDiscordStatement.clearParameters();
		try {
			if (result.next()) {
				return result.getLong("discord");
			} else {
				return -1;
			}
		} finally {
			result.close();
		}
	}

	static private PreparedStatement removeLinkMinecraftStatement;

	public static void removeLink(UUID minecraft) throws SQLException {
		removeLink(minecraft.toString());
	}

	public static void removeLink(String minecraft) throws SQLException {
		removeLinkMinecraftStatement.setString(1, minecraft);
		removeLinkMinecraftStatement.executeUpdate();
		removeLinkMinecraftStatement.clearParameters();
	}

	static private PreparedStatement removeLinkDiscordStatement;

	public static void removeLink(long discord) throws SQLException {
		removeLinkDiscordStatement.setLong(1, discord);
		removeLinkDiscordStatement.executeUpdate();
		removeLinkDiscordStatement.clearParameters();
	}

	public static boolean confirm() {
		try {
			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean cancel() {
		try {
			conn.rollback();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean disconnect() {
		try {
			conn.rollback();
			conn.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}