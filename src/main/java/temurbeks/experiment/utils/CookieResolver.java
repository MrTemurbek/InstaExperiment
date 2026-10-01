package temurbeks.experiment.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CookieResolver {

    private static final Logger log = Logger.getLogger("CookieResolver");
    private static String cachedCookieHeader = null;
    private static boolean resolved = false;

    public static synchronized String getCookieHeader() {
        if (resolved) {
            return cachedCookieHeader;
        }
        resolved = true;
        cachedCookieHeader = resolveCookies();
        return cachedCookieHeader;
        }

    private static String resolveCookies() {
        String cookiesData = System.getenv("COOKIES_DATA");
        if (cookiesData != null && !cookiesData.trim().isEmpty()) {
            cookiesData = cookiesData.trim();
            String raw = cookiesData;
            if (!raw.contains("\t") && !raw.startsWith("#")) {
                try {
                    String cleanBase64 = raw.replaceAll("\\s+", "");
                    byte[] decoded = Base64.getDecoder().decode(cleanBase64);
                    raw = new String(decoded, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warning("COOKIES_DATA не похоже ни на base64, ни на cookies.txt — игнорирую");
                    raw = null;
                }
            }
            if (raw != null) {
                log.info("Куки взяты из COOKIES_DATA");
                return parseNetscapeCookies(raw);
            }
        }

        String cookiesFile = System.getenv("COOKIES_FILE");
        if (cookiesFile == null || cookiesFile.trim().isEmpty()) {
            cookiesFile = "cookies.txt";
        }

        Path path = Paths.get(cookiesFile);
        if (Files.exists(path)) {
            try {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                log.info("Куки взяты из файла " + path.toAbsolutePath());
                return parseNetscapeCookies(content);
            } catch (IOException e) {
                log.warning("Ошибка чтения файла куков " + path + ": " + e.getMessage());
            }
        }

        log.info("Куки не заданы — приватные посты будут недоступны");
        return null;
    }

    private static String parseNetscapeCookies(String netscapeContent) {
        if (netscapeContent == null) return null;
        Map<String, String> cookieMap = new HashMap<>();
        String[] lines = netscapeContent.split("\r?\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\t");
            if (parts.length >= 7) {
                String name = parts[5].trim();
                String value = parts[6].trim();
                if (!name.isEmpty()) {
                    cookieMap.put(name, value);
                }
            }
        }
        if (cookieMap.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
