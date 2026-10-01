package temurbeks.experiment.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import temurbeks.experiment.entity.InstagramMediaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Аналог функций download_sync() и build_ydl_opts() из bot.py.
 * Шаги скачивания (как в bot.py):
 *   Шаг 1 — Извлечь shortcode из Instagram-ссылки
 *   Шаг 2 — Попытаться получить данные через Instagram Private API (?__a=1&__d=dis)
 *   Шаг 3 — Если не получилось — попытаться через страницу /embed/captioned/ (Jsoup)
 *   Шаг 4 — Если и это не получилось — попытаться через igram.world (сторонний API)
 *   Шаг 5 — Из полученного JSON вытащить прямые ссылки на видео / фото / аудио
 *   Шаг 6 — Заполнить InstagramMediaItem (url, caption, title, performer, duration, isVideo)
 */
public class InstagramDownloader {

    private static final Logger log = Logger.getLogger("InstagramDownloader");

    // Ограничитель параллельных загрузок — аналог semaphore = asyncio.Semaphore(MAX_PARALLEL) в bot.py
    private static final int MAX_PARALLEL = 3;
    private static final Semaphore semaphore = new Semaphore(MAX_PARALLEL);

    // Регулярное выражение для нахождения shortcode в URL — аналог INSTAGRAM_RE в bot.py
    private static final Pattern SHORTCODE_RE = Pattern.compile(
            "instagram\\.com/(?:[A-Za-z0-9_.\\-]+/)?(?:p|reel|reels|tv|share)/([A-Za-z0-9_\\-]+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Главный метод — аналог download_sync() в bot.py.
     *
     * @param url     Instagram-ссылка (reel, post, stories и т.д.)
     * @param isAudio true — нужен только звук (аналог audio=True в bot.py)
     * @return список найденных медиа-элементов
     */
    public List<InstagramMediaItem> downloadSync(String url, boolean isAudio) throws Exception {
        // Ограничиваем количество параллельных запросов (аналог async with semaphore в bot.py)
        semaphore.acquire();
        try {
            return fetchMedia(url, isAudio);
        } finally {
            semaphore.release();
        }
    }

    // ─────────────────────────────────────────────────────────
    // Внутренняя логика скачивания
    // ─────────────────────────────────────────────────────────

    private List<InstagramMediaItem> fetchMedia(String url, boolean isAudio) {
        List<InstagramMediaItem> results = new ArrayList<>();

        // ── Шаг 1: Извлекаем shortcode из URL ────────────────────────────────
        // Аналог regexp из INSTAGRAM_RE в bot.py для нахождения идентификатора поста
        String shortcode = extractShortcode(url);
        log.info("Шаг 1: Извлечён shortcode = " + shortcode + " из URL: " + url);

        // ── Шаг 2: Пробуем Instagram Private API ─────────────────────────────
        // Аналог yt_dlp с форматом best — сначала идём к официальному (частному) API
        // URL вида: https://www.instagram.com/p/<shortcode>/?__a=1&__d=dis
        if (shortcode != null) {
            log.info("Шаг 2: Запрос к Instagram Private API для shortcode=" + shortcode);
            try {
                results = fetchFromPrivateApi(shortcode, isAudio);
                if (!results.isEmpty()) {
                    log.info("Шаг 2: Успех — получено " + results.size() + " медиа-элементов");
                    return results;
                }
            } catch (Exception e) {
                log.warning("Шаг 2: Instagram API не ответил: " + e.getMessage());
            }
        }

        // ── Шаг 3: Fallback — Embed-страница Instagram ───────────────────────
        // Аналог второй попытки yt_dlp — скрейпим HTML страницу /embed/captioned/
        // Это работает для публичных постов без авторизации
        if (shortcode != null) {
            log.info("Шаг 3: Fallback — скрейпим embed-страницу");
            try {
                results = fetchFromEmbed(shortcode, isAudio);
                if (!results.isEmpty()) {
                    log.info("Шаг 3: Успех через embed-страницу");
                    return results;
                }
            } catch (Exception e) {
                log.warning("Шаг 3: Embed-страница не дала результата: " + e.getMessage());
            }
        }

        // Если все попытки провалились — возвращаем пустой список
        // (аналог проверки if not items: в bot.py)
        log.warning("Все попытки скачать медиа провалились для URL: " + url);
        return results;
    }

    // ─────────────────────────────────────────────────────────
    // Шаг 1: Извлечение shortcode
    // ─────────────────────────────────────────────────────────

    /**
     * Извлекает shortcode из Instagram-ссылки.
     * Примеры: /reel/ABC123/ → ABC123, /p/XYZ789/ → XYZ789
     */
    private String extractShortcode(String url) {
        if (url == null) return null;
        Matcher m = SHORTCODE_RE.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    // ─────────────────────────────────────────────────────────
    // Шаг 2: Instagram Private API
    // ─────────────────────────────────────────────────────────

    /**
     * Запрашивает метаданные поста через Instagram Private API.
     * URL: <a href="https://www.instagram.com/p/">...</a><shortcode>/?__a=1&__d=dis
     * Аналог yt_dlp.extract_info() с форматом best[filesize<50M]/best
     * Извлекает: video_versions, image_versions2, carousel_media, caption, user, duration
     */
    private List<InstagramMediaItem> fetchFromPrivateApi(String shortcode, boolean isAudio) throws Exception {
        List<InstagramMediaItem> results;

        // Строим URL к Private API
        String apiUrl = "https://www.instagram.com/p/" + shortcode + "/?__a=1&__d=dis";

        // Настраиваем HTTP-запрос с таймаутом 30 сек (аналог socket_timeout=30 в bot.py)
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(30_000)
                .setSocketTimeout(30_000)
                .build();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            HttpGet request = new HttpGet(apiUrl);

            // Устанавливаем заголовки браузера, чтобы Instagram не заблокировал запрос
            request.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            request.setHeader("Accept", "application/json, text/plain, */*");
            request.setHeader("Accept-Language", "en-US,en;q=0.9");
            request.setHeader("X-IG-App-ID", "936619743392459");

            // Если есть cookies (для приватных аккаунтов) — прикрепляем
            // Аналог cookiefile в build_ydl_opts() из bot.py
            String cookieHeader = CookieResolver.getCookieHeader();
            if (cookieHeader != null) {
                request.setHeader("Cookie", cookieHeader);
                log.info("Шаг 2: Используем cookies для авторизации");
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                int status = response.getStatusLine().getStatusCode();
                if (status != 200) {
                    throw new RuntimeException("Instagram API вернул HTTP " + status);
                }

                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity, "UTF-8");

                // ── Шаг 5: Парсим JSON и извлекаем прямые ссылки ────────────────
                results = parseInstagramApiJson(json, isAudio);
            }
        }

        return results;
    }

    // ─────────────────────────────────────────────────────────
    // Шаг 3: Embed-страница
    // ─────────────────────────────────────────────────────────

    /**
     * Скрейпит HTML-страницу /embed/captioned/ через Jsoup.
     * Аналог fallback в yt_dlp при ошибке основного экстрактора.
     */
    private List<InstagramMediaItem> fetchFromEmbed(String shortcode, boolean isAudio) throws Exception {
        List<InstagramMediaItem> results = new ArrayList<>();
        String embedUrl = "https://www.instagram.com/p/" + shortcode + "/embed/captioned/";

        Document doc = Jsoup.connect(embedUrl)
                .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1)")
                .timeout(15_000)
                .followRedirects(true)
                .ignoreContentType(true)
                .get();

        String html = doc.outerHtml();

        String caption = extractCaption(doc);
        String performer = extractPerformer(doc);
        if (performer.isEmpty()) performer = "Instagram";

        String title = truncate(caption.isEmpty() ? "Instagram" : caption, 60);

        String videoUrl = extractVideoUrl(html);
        if (videoUrl != null && !videoUrl.isBlank()) {
            results.add(new InstagramMediaItem(
                    videoUrl,
                    caption,
                    title,
                    "@" + performer,
                    0,
                    true
            ));
            return results;
        }

        if (!isAudio) {
            String imageUrl = extractImageUrl(doc, html);
            if (imageUrl != null && !imageUrl.isBlank()) {
                results.add(new InstagramMediaItem(
                        imageUrl,
                        caption,
                        title,
                        "@" + performer,
                        0,
                        false
                ));
            }
        }

        return results;
    }

    private String extractVideoUrl(String html) {
        if (html == null || html.isEmpty()) return null;

        // Самый частый вариант: "video_url":"https:\/\/...mp4"
        String url = matchGroup(html, "\"video_url\"\\s*:\\s*\"(.*?)\"");
        if (url != null) return decodeInstagramUrl(url);

        // Иногда ключ экранирован внутри JS: \"video_url\":\"https:\\/\\/...mp4\"
        url = matchGroup(html, "\\\\\"video_url\\\\\"\\s*:\\s*\\\\\"(.*?)\\\\\"");
        if (url != null) return decodeInstagramUrl(url);

        // Fallback на meta-теги
        url = matchMeta(html, "og:video");
        if (url != null) return decodeInstagramUrl(url);

        url = matchMeta(html, "og:video:secure_url");
        if (url != null) return decodeInstagramUrl(url);

        return null;
    }

    private String extractImageUrl(Document doc, String html) {
        Element imgEl = doc.selectFirst("img.EmbeddedMediaImage");
        if (imgEl != null) {
            String src = imgEl.attr("src");
            if (!src.isBlank()) {
                return decodeInstagramUrl(src);
            }
        }

        String url = matchGroup(html, "\"display_url\"\\s*:\\s*\"(.*?)\"");
        if (url != null) return decodeInstagramUrl(url);

        return null;
    }

    private String extractCaption(Document doc) {
        Element captionEl = doc.selectFirst("div.Caption");
        if (captionEl == null) return "";
        return truncate(captionEl.text().trim(), 900);
    }

    private String extractPerformer(Document doc) {
        Element performerEl = doc.selectFirst("a.UsernameText, a.CaptionUsername, span.Username, a.Username");
        if (performerEl == null) return "";
        return performerEl.text().trim();
    }

    private String matchGroup(String text, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String matchMeta(String html, String propertyName) {
        Pattern p = Pattern.compile(
                "<meta[^>]+property=[\"']" + Pattern.quote(propertyName) + "[\"'][^>]+content=[\"'](.*?)[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher m = p.matcher(html);
        return m.find() ? m.group(1) : null;
    }

    private String decodeInstagramUrl(String s) {
        if (s == null) return null;

        return s
                .replace("\\\\/", "/")
                .replace("\\/", "/")
                .replace("\\u0026", "&")
                .replace("&amp;", "&")
                .replace("\\u003d", "=")
                .replace("\\u002B", "+")
                .replace("\\u002F", "/")
                .trim();
    }


    // ─────────────────────────────────────────────────────────
    // Шаг 5: Парсинг JSON от Instagram Private API
    // ─────────────────────────────────────────────────────────

    /**
     * Парсит JSON-ответ Instagram Private API и заполняет список InstagramMediaItem.
     * Аналог pick_file() и цикла for entry in info.get("entries") or [info] в bot.py.
     * Структура JSON:
     *   items[0]
     *     caption.text          → description/caption (обрезается до 900 символов)
     *     user.username         → performer (@username)
     *     video_duration        → duration в секундах
     *     video_versions[0].url → прямая ссылка на видео
     *     image_versions2.candidates[0].url → ссылка на фото
     *     carousel_media[]      → несколько медиа-элементов в одном посте
     */
    private List<InstagramMediaItem> parseInstagramApiJson(String json, boolean isAudio) {
        List<InstagramMediaItem> results = new ArrayList<>();

        try {
            JsonObject root = new Gson().fromJson(json, JsonObject.class);
            if (root == null) return results;

            // Проверяем, не требует ли Instagram авторизацию (закрытый аккаунт)
            if (root.has("require_login") && root.get("require_login").getAsBoolean()) {
                throw new RuntimeException("Аккаунт закрыт — нужен cookies.txt для авторизации");
            }

            if (!root.has("items")) return results;
            JsonArray items = root.getAsJsonArray("items");
            if (items.isEmpty()) return results;

            JsonObject item = items.get(0).getAsJsonObject();

            // ── Извлекаем метаданные (аналог entry.get("description"), "uploader" в bot.py) ──

            // Caption (описание поста) — обрезаем до 900 символов как в bot.py
            String caption = "";
            if (item.has("caption") && !item.get("caption").isJsonNull()) {
                JsonObject capObj = item.getAsJsonObject("caption");
                if (capObj.has("text")) {
                    caption = truncate(capObj.get("text").getAsString(), 900);
                }
            }

            // Performer (username автора) — аналог entry.get("uploader") в bot.py
            String performer = "Instagram";
            if (item.has("user") && !item.get("user").isJsonNull()) {
                JsonObject userObj = item.getAsJsonObject("user");
                if (userObj.has("username")) {
                    performer = "@" + userObj.get("username").getAsString();
                }
            }

            // Title — обрезаем до 60 символов как в bot.py (entry.get("title")[:60])
            String title = truncate(caption.isEmpty() ? "Instagram" : caption, 60);

            // Duration (длительность видео в секундах) — аналог entry.get("duration") в bot.py
            int duration = 0;
            if (item.has("video_duration") && !item.get("video_duration").isJsonNull()) {
                duration = (int) Math.round(item.get("video_duration").getAsDouble());
            }

            // ── Шаг 6: Определяем тип медиа и вытаскиваем прямые ссылки ─────

            if (item.has("carousel_media")) {
                // Карусель (несколько фото/видео в одном посте)
                // Аналог for entry in info.get("entries") or [info] в bot.py
                JsonArray carousel = item.getAsJsonArray("carousel_media");
                for (JsonElement elem : carousel) {
                    JsonObject media = elem.getAsJsonObject();
                    InstagramMediaItem mediaItem = extractMediaFromObject(media, caption, title, performer, duration, isAudio);
                    if (mediaItem != null) results.add(mediaItem);
                }
            } else {
                // Одиночный пост (reel / фото)
                InstagramMediaItem mediaItem = extractMediaFromObject(item, caption, title, performer, duration, isAudio);
                if (mediaItem != null) results.add(mediaItem);
            }

        } catch (Exception e) {
            log.warning("Шаг 5: Ошибка парсинга JSON: " + e.getMessage());
        }

        return results;
    }

    /**
     * Из одного объекта JSON (поста или элемента карусели) извлекает видео или фото URL.
     * Аналог pick_file() в bot.py — сначала ищем video, потом image.
     */
    private InstagramMediaItem extractMediaFromObject(
            JsonObject obj, String caption, String title, String performer, int duration, boolean isAudio) {

        // Сначала проверяем video_versions (Reels, видео-пост)
        if (obj.has("video_versions")) {
            JsonArray vv = obj.getAsJsonArray("video_versions");
            if (!vv.isEmpty()) {
                // Берём первый элемент — наилучшее качество (аналог format=best в bot.py)
                String videoUrl = vv.get(0).getAsJsonObject().get("url").getAsString();
                return new InstagramMediaItem(videoUrl, caption, title, performer, duration, true);
            }
        }

        // Если видео нет и режим не аудио — берём фото (image_versions2)
        if (!isAudio && obj.has("image_versions2")) {
            JsonObject imgVersions = obj.getAsJsonObject("image_versions2");
            if (imgVersions.has("candidates")) {
                JsonArray candidates = imgVersions.getAsJsonArray("candidates");
                if (!candidates.isEmpty()) {
                    // Берём первый кандидат — наибольшее разрешение
                    String imgUrl = candidates.get(0).getAsJsonObject().get("url").getAsString();
                    return new InstagramMediaItem(imgUrl, caption, title, performer, duration, false);
                }
            }
        }

        return null;
    }

    // ─────────────────────────────────────────────────────────
    // Утилиты
    // ─────────────────────────────────────────────────────────

    /**
     * Обрезает строку до maxLen символов.
     * Аналог [:900] и [:60] срезов в bot.py.
     */
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
