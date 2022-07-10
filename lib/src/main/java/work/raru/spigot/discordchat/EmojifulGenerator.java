package work.raru.spigot.discordchat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class EmojifulGenerator {
	private static final String TEMPLATE = "{\n"
			+ "  \"category\": \"{category}\",\n"
			+ "  \"name\": \"{name}\",\n"
			+ "  \"url\": \"{url}\",\n"
			+ "  \"type\": \"emojiful:emoji_recipe\"\n"
			+ "}";
	static void generate() throws IOException {
		// ready dir
		File datapack_dir = new File(Main.instance.getServer().getWorlds().get(0).getWorldFolder(), "datapacks");
		assert (datapack_dir.isDirectory());
		File pack_dir = new File(datapack_dir, "discordchat");
		File json_dir = new File(pack_dir, "data/emojiful/recipes");
		// clear old data
		if (json_dir.exists()) {
			try {
				for (File file : json_dir.listFiles()) {
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// generate folder
		json_dir.mkdirs();
		// save mcmeta from plugin resource
		InputStream sourceMcmeta = Main.instance.getResource("pack.mcmeta");
		FileOutputStream destMcmeta = new FileOutputStream(new File(pack_dir, "pack.mcmeta"), false);
		destMcmeta.write(sourceMcmeta.readAllBytes());
		// get emotes and save it
		List<RichCustomEmoji> emotes = DiscordMessage.getChannel().getGuild().getEmojis();
		for (RichCustomEmoji emote : emotes) {
			String jsonData = TEMPLATE.replace("{url}",emote.getImageUrl());
			jsonData = jsonData.replace("{name}", emote.getName());
			jsonData = jsonData.replace("{category}", emote.getGuild().getName());
			File jsonFile = new File(json_dir, emote.getId()+".json");
			jsonFile.createNewFile();
			FileWriter writer = new FileWriter(jsonFile);
			writer.write(jsonData);
			writer.flush();
			writer.close();
		}
	}
}