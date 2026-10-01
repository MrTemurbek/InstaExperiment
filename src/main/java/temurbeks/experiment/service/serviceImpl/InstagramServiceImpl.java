package temurbeks.experiment.service.serviceImpl;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import temurbeks.experiment.entity.InstagramMediaItem;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.entity.TelegramUser;
import temurbeks.experiment.service.InstagramService;
import temurbeks.experiment.service.TelegramService;
import temurbeks.experiment.utils.InstagramDownloader;
import temurbeks.experiment.utils.SendMessageToBot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class InstagramServiceImpl implements InstagramService {

    private static final Logger log = Logger.getLogger("InstagramServiceImpl");
    private static final String FILE_PATH = "telegramUsers.txt";

    // Список зарегистрированных пользователей (загружается из файла при старте)
    public static List<TelegramUser> chatIdList = new ArrayList<>();
    private final List<String> userIds = new ArrayList<>();

    // Единственный экземпляр загрузчика — содержит всю логику скачивания с Instagram
    private final InstagramDownloader downloader = new InstagramDownloader();

    @Inject
    TelegramService telegramService;

    /**
     * При старте приложения читаем список пользователей из файла.
     */
    public void onStart(@Observes StartupEvent event) {
        chatIdList = readStaticListFromFile();
        for (TelegramUser user : chatIdList) {
            userIds.add(user.getId());
        }
        log.info("Загружено " + chatIdList.size() + " пользователей из " + FILE_PATH);
    }

    /**
     * Добавляет нового пользователя в список и сохраняет в файл.
     */
    public void addChanelToStaticList(TelegramUser tgUser) {
        chatIdList.add(tgUser);
        userIds.add(tgUser.getId());
        saveStaticListToFile(chatIdList);
        log.info("Добавлен новый пользователь: " + tgUser.getId());
    }

    /**
     * Скачивает медиа с Instagram.
     *
     * Делегирует всю логику в InstagramDownloader.downloadSync() —
     * аналог asyncio.to_thread(download_sync, url, tmpdir) из bot.py.
     *
     * @param url     Instagram-ссылка (reel, post, stories и т.д.)
     * @param isAudio true — нужен только звук (для кнопки "🎵 Скачать MP3")
     * @return список медиа-элементов с прямыми ссылками
     */
    public List<InstagramMediaItem> downloadMedia(String url, boolean isAudio) throws Exception {
        log.info("Начинаем скачивание: url=" + url + ", isAudio=" + isAudio);
        List<InstagramMediaItem> items = downloader.downloadSync(url, isAudio);
        log.info("Скачивание завершено: найдено " + items.size() + " медиа-элементов");
        return items;
    }

    /**
     * Устаревший метод (оставлен для совместимости с REST API).
     * Новый бот использует downloadMedia() напрямую.
     */
    @Override
    public String getLinkVideo(InstagramRequest data, TelegramUser tgUser) {
        if (!userIds.contains(tgUser.getId())) {
            addChanelToStaticList(tgUser);
        }
        
        LocalDateTime requestTime = LocalDateTime.now();
        
        try {
            new SendMessageToBot().sendMessage("Скачивание началось ! ✔️ \n Ссылка 🔗: " + data.getUrl(), data.getChat());
            List<InstagramMediaItem> items = downloadMedia(data.getUrl(), false);
            telegramService.sendAllToBotFromUrl(items, data.getUrl(), requestTime, data.getChat());
        } catch (Exception e) {
            log.warning("getLinkVideo: ошибка при скачивании — " + e.getMessage());
        }
        return "SUCCESS";
    }

    /**
     * Рассылает сообщение всем зарегистрированным пользователям бота.
     * Аналог команды TO_ALL в TelegramBotHandler.
     */
    @Override
    public Boolean sendToAll(StringEntity message, TelegramUser tgUser) {
        if (!userIds.contains(tgUser.getId())) {
            addChanelToStaticList(tgUser);
        }
        for (TelegramUser user : chatIdList) {
            try {
                new SendMessageToBot().sendMessage(message.getMessage(), user.getId());
            } catch (IOException | InterruptedException e) {
                log.warning("sendToAll: не удалось отправить " + user.getId() + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Отправляет список всех пользователей запрашивающему.
     * Аналог команды GET_ALL в TelegramBotHandler.
     */
    @Override
    public Boolean getAll(TelegramUser tgUser) {
        if (!userIds.contains(tgUser.getId())) {
            addChanelToStaticList(tgUser);
        }
        StringBuilder sb = new StringBuilder();
        for (TelegramUser user : chatIdList) {
            sb.append(user.getId())
              .append(" - ").append(user.getName())
              .append(" - @").append(user.getUsername())
              .append("\n");
        }
        try {
            new SendMessageToBot().sendMessage(sb.toString(), tgUser.getId());
        } catch (IOException | InterruptedException e) {
            log.warning("getAll: не удалось отправить список: " + e.getMessage());
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────
    // Вспомогательные методы для работы с файлом пользователей
    // ─────────────────────────────────────────────────────────

    /**
     * Читает список пользователей из файла telegramUsers.txt.
     * Формат строки: id - name - username
     */
    private static List<TelegramUser> readStaticListFromFile() {
        List<TelegramUser> userList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length == 3) {
                    TelegramUser user = new TelegramUser();
                    user.setId(parts[0]);
                    user.setName(parts[1]);
                    user.setUsername(parts[2]);
                    userList.add(user);
                } else {
                    System.err.println("Некорректная строка в файле: " + line);
                }
            }
        } catch (IOException ignored) {
            // Файл ещё не существует — это нормально при первом запуске
        }
        return userList;
    }

    /**
     * Сохраняет список пользователей в файл telegramUsers.txt.
     * Формат строки: id - name - username
     */
    private static void saveStaticListToFile(List<TelegramUser> userList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (TelegramUser user : userList) {
                writer.write(user.getId() + " - " + user.getName() + " - " + user.getUsername());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
