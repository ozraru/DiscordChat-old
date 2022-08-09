package work.raru.spigot.discordchat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * I wrote this. But I can't read this :P
 * 
 * @author ozraru
 *
 */
public class MarkdownConverter {

	private static final Pattern pattern = Pattern.compile("((\\*\\*\\*?[^§]+?\\*?\\*\\*)|(\\*(\\*\\*)?[^§]+?(\\*\\*)?\\*)|(~~[^§]+?~~)|(__[^§]+?__))");

	final static char BOLD_C = 'l';
	final static char ITALIC_C = 'o';
	final static char STRIKE_C = 'm';
	final static char UNDER_C = 'n';
	
	enum Kind {
		BOLD(2,2,BOLD_C),
		ITALIC(3,1,ITALIC_C),
		STRIKE(6,2,STRIKE_C),
		UNDER(7,2,UNDER_C);
		
		final int group;
		final int length;
		final char minecraft;
		
		Kind(int group, int length, char minecraft) {
			this.group = group;
			this.length = length;
			this.minecraft = minecraft;
		}
	}

	public static void main(String[] args) {
		System.out.println(toMinecraft(args[0]));
	}

	static String toMinecraft(String markdown) {
		String converted = markdown;
		while (true) {
			Matcher matcher = pattern.matcher(converted);
			if (!matcher.find()) {
				break;
			}
			for (Kind kind : Kind.values()) {
				String matched = matcher.group(kind.group);
				if (matched != null && matched.length() != 0) {
					converted = matcher.replaceFirst("§" + kind.minecraft + matched.substring(kind.length, matched.length() - kind.length) + "§" + kind.minecraft);
					break;
				}
			}
		}
		StringBuilder resultString = new StringBuilder();
		boolean bold = false;
		boolean italic = false;
		boolean strike = false;
		boolean under = false;
		for (int i = 0; i < converted.length(); i++) {
			if (converted.charAt(i) == '§' && converted.length() > i+1) {
				boolean reset = false;
				switch (converted.charAt(i+1)) {
				case BOLD_C:
					bold = !bold;
					if (bold) {
						resultString.append("§"+BOLD_C);
					} else {
						reset = true;
					}
					i++;
					break;
				case ITALIC_C:
					italic = !italic;
					if (italic) {
						resultString.append("§"+ITALIC_C);
					} else {
						reset = true;
					}
					i++;
					break;
				case STRIKE_C:
					strike = !strike;
					if (strike) {
						resultString.append("§"+STRIKE_C);
					} else {
						reset = true;
					}
					i++;
					break;
				case UNDER_C:
					under = !under;
					if (under) {
						resultString.append("§"+UNDER_C);
					} else {
						reset = true;
					}
					i++;
					break;
				}
				if (reset) {
					resultString.append("§r");
					if (bold) {
						resultString.append("§"+BOLD_C);
					}
					if (italic) {
						resultString.append("§"+ITALIC_C);
					}
					if (strike) {
						resultString.append("§"+STRIKE_C);
					}
					if (under) {
						resultString.append("§"+UNDER_C);
					}
				}
			} else {
				resultString.append(converted.charAt(i));
			}
		}
		return resultString.toString();
	}
}
