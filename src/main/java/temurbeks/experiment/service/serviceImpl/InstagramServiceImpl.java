package temurbeks.experiment.service.serviceImpl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.event.Observes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import temurbeks.experiment.entity.*;
import temurbeks.experiment.service.InstagramService;
import temurbeks.experiment.service.TelegramService;
import jakarta.inject.Inject;
import temurbeks.experiment.utils.GetDownloadUrlHelper;
import temurbeks.experiment.utils.SendMessageToBot;
import temurbeks.experiment.utils.TelegramSender;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import static temurbeks.experiment.utils.Extractor.getDownloadUrlForStories;
import static temurbeks.experiment.utils.Extractor.postOrReelsExtractor;


@ApplicationScoped
public class InstagramServiceImpl implements InstagramService {
    public static Type instaType;
    @Inject
    TelegramService telegramService;
    List<String> userIds = new ArrayList<>();

    private static final String FILE_PATH = "telegramUsers.txt";
    public static List<TelegramUser> chatIdList;

    public void onStart(@Observes StartupEvent event) {
        chatIdList = readStaticListFromFile();
        for (TelegramUser user : chatIdList) {
            userIds.add(user.getId());
        }
    }

    public void addChanelToStaticList(TelegramUser tgUser) {
        chatIdList.add(tgUser);
        userIds.add(tgUser.getId());
        saveStaticListToFile(chatIdList);
    }

    @Override
    public String getLinkVideo(InstagramRequest data, TelegramUser tgUser) throws IOException, InterruptedException {
        if (!userIds.contains(tgUser.getId())) {
            addChanelToStaticList(tgUser);
        }
        SendMessageToBot sendMessageToBot = new SendMessageToBot();
        try {
            sendMessageToBot.sendMessage("Скачивание началось ! ✔️ " +
                    "\n Ссылка 🔗: " + data.getUrl(), data.getChat());
        } catch (Exception ignored) {
        }
        instaType = getInstaType(data.getUrl());
        LocalDateTime requestTime = LocalDateTime.now();
        List<String> responseUrl;
        String json = new GetDownloadUrlHelper().getUrl(data.getUrl(), instaType, data.getChat());
        responseUrl = extractUrlsFromData(json, data.getChat());
        try {
            telegramService.sendAllToBotFromUrl(responseUrl, data.getUrl(), requestTime, data.getChat(), instaType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR TELEGRAM");
        }
        return "SUCCESS";
    }

    @Override
    public Boolean sendToAll(StringEntity message, TelegramUser tgUser) {
        if (!userIds.contains(tgUser.getId())) {
            addChanelToStaticList(tgUser);
        }
        for (TelegramUser user : chatIdList) {
            try {
                new SendMessageToBot().sendMessage(message.getMessage(), user.getId());
            } catch (IOException | InterruptedException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean getAll(TelegramUser tgUser) {
        if (!userIds.contains(tgUser.getId())) {
            addChanelToStaticList(tgUser);
        }
        String line = "";
        for (TelegramUser user : chatIdList) {
            line += user.getId() + " - " + user.getName() + " - @" + user.getUsername() + "\n";
        }
        try {
            new SendMessageToBot().sendMessage(line, tgUser.getId());
        } catch (IOException | InterruptedException e) {
            return false;
        }

        return true;
    }

    ;

    private static List<String> extractUrlsFromData(String data, String chatId) {
        if (instaType.equals(Type.POST) || instaType.equals(Type.REELS)) {
            return postOrReelsExtractor(data, chatId);
        } else {
            return getDownloadUrlForStories(data);
        }
    }


    private Type getInstaType(String url) {
        if (url.contains(".com/reel")) {
            return Type.REELS;
        } else if (url.contains(".com/stories/")) {
            return Type.STORIES;
        } else {
            return Type.POST;
        }
    }


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
                    // Handle invalid format in the file
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            // Обработка ошибки чтения файла
            e.printStackTrace();
        }

        return userList;
    }

    private static void saveStaticListToFile(List<TelegramUser> userList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (TelegramUser user : userList) {
                String line = user.getId() + " - " + user.getName() + " - " + user.getUsername();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            // Обработка ошибки записи в файл
            e.printStackTrace();
        }
    }
}

