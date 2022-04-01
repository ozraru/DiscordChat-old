package work.raru.spigot.discordchat;

import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Copied from https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb
 * @author k3kdude
 * edited by ozraru
 *
 */
public class DiscordWebhook implements Runnable {

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }
    
    @Override
    public void run() {
    	try {
            if (this.content == null) {
                throw new IllegalArgumentException("Set content");
            }

            JsonObject json = new JsonObject();

            json.add("content", new JsonPrimitive(this.content));
            if (this.username != null) {
                json.add("username", new JsonPrimitive(this.username));
            }
            if (this.avatarUrl != null) {
                json.add("avatar_url", new JsonPrimitive(this.avatarUrl));
            }
            json.add("tts", new JsonPrimitive(this.tts));

            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStream stream = connection.getOutputStream();
            stream.write(json.toString().getBytes());
            stream.flush();
            stream.close();

            connection.getInputStream().close(); //I'm not sure why but it doesn't work without getting the InputStream
            connection.disconnect();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

//    private class JSONObject {
//
//        private final HashMap<String, Object> map = new HashMap<>();
//
//        void put(String key, Object value) {
//            if (value != null) {
//                map.put(key, value);
//            }
//        }
//
//        @Override
//        public String toString() {
//            StringBuilder builder = new StringBuilder();
//            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
//            builder.append("{");
//
//            int i = 0;
//            for (Map.Entry<String, Object> entry : entrySet) {
//                Object val = entry.getValue();
//                builder.append(quote(entry.getKey())).append(":");
//
//                if (val instanceof String) {
//                    builder.append(quote(String.valueOf(val)));
//                } else if (val instanceof Integer) {
//                    builder.append(Integer.valueOf(String.valueOf(val)));
//                } else if (val instanceof Boolean) {
//                    builder.append(val);
//                } else if (val instanceof JSONObject) {
//                    builder.append(val.toString());
//                } else if (val.getClass().isArray()) {
//                    builder.append("[");
//                    int len = Array.getLength(val);
//                    for (int j = 0; j < len; j++) {
//                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
//                    }
//                    builder.append("]");
//                }
//
//                builder.append(++i == entrySet.size() ? "}" : ",");
//            }
//
//            return builder.toString();
//        }
//
//        private String quote(String string) {
//            return "\"" + string + "\"";
//        }
//    }

}