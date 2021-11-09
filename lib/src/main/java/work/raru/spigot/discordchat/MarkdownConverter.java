package work.raru.spigot.discordchat;

import java.util.HashMap;

/**
 * I wrote this.
 * But I can't read this :P
 * @author ozraru
 *
 */
public class MarkdownConverter {

	private static final int BOLD = 1;
	private static final int ITALIC = 2;
	private static final int STRIKE = 3;
	private static final int UNDER = 4;
	
	public static void main(String[] args) {
		System.out.println(toMinecraft(args[0]));
	}
	
	static String toMinecraft(String markdown) {
		int boldPos = -1;
		int italicPos = -1;
		int strikePos = -1;
		int underPos = -1;
		HashMap<Integer,Deco> decos = new HashMap<Integer,Deco>();
		for (int i = 0; i < markdown.length(); i++) {
			int clearPos = -1;
			switch (markdown.charAt(i)) {
			case '*':
				if (markdown.length() > i+1 && markdown.charAt(i+1) == '*') {
					if (boldPos < 0) {
						boldPos = i;
					} else {
						decos.put(boldPos, new Deco(BOLD, true));
						decos.put(i, new Deco(BOLD, false));
						clearPos = boldPos;
					}
					i++;
				} else {
					if (italicPos < 0) {
						italicPos = i;
					} else {
						decos.put(italicPos, new Deco(ITALIC, true));
						decos.put(i, new Deco(ITALIC, false));
						clearPos = italicPos;
					}
				}
				break;
			case '~':
				if (markdown.length() > i+1 && markdown.charAt(i+1) == '~') {
					if (strikePos < 0) {
						strikePos = i;
					} else {
						decos.put(strikePos, new Deco(STRIKE, true));
						decos.put(i, new Deco(STRIKE, false));
						clearPos = strikePos;
					}
					i++;
				}
				break;
			case '_':
				if (markdown.length() > i+1 && markdown.charAt(i+1) == '_') {
					if (underPos < 0) {
						underPos = i;
					} else {
						decos.put(underPos, new Deco(UNDER, true));
						decos.put(i, new Deco(UNDER, false));
						clearPos = underPos;
					}
					i++;
				}
				break;
			}
			if (clearPos > 0) {
				if (clearPos <= boldPos && boldPos <= i) boldPos = -1;
				if (clearPos <= italicPos && italicPos <= i) italicPos = -1;
				if (clearPos <= strikePos && strikePos <= i) strikePos = -1;
				if (clearPos <= underPos && underPos <= i) underPos = -1;
			}
		}
		StringBuilder resultString = new StringBuilder();
		boolean bold = false;
		boolean italic = false;
		boolean strike = false;
		boolean under = false;
		for (int i = 0; i < markdown.length(); i++) {
			if (decos.containsKey(i)) {
				switch (decos.get(i).mark) {
				case BOLD:
					bold = decos.get(i).start;
					if (bold) {
						resultString.append("§l");
					}
					break;
				case ITALIC:
					italic = decos.get(i).start;
					if (italic) {
						resultString.append("§o");
					}
					break;
				case STRIKE:
					strike = decos.get(i).start;
					if (strike) {
						resultString.append("§m");
					}
					break;
				case UNDER:
					under = decos.get(i).start;
					if (under) {
						resultString.append("§n");
					}
					break;
				}
				if (!decos.get(i).start) {
					resultString.append("§r");
					if (bold) {
						resultString.append("§l");
					}
					if (italic) {
						resultString.append("§o");
					}
					if (strike) {
						resultString.append("§m");
					}
					if (under) {
						resultString.append("§n");
					}
				}
				if (decos.get(i).mark != ITALIC) {
					i++;
				}
			} else {
				resultString.append(markdown.charAt(i));
			}
		}
		return resultString.toString();
	}
	
	static class Deco {
		final int mark;
		final boolean start;
		Deco(int mark, boolean start) {
			this.mark = mark;
			this.start = start;
		}
	}
}
