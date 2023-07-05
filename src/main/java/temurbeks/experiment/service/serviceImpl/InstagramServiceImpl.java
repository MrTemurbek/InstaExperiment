package temurbeks.experiment.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import jakarta.enterprise.context.ApplicationScoped;


import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@ApplicationScoped
public class InstagramServiceImpl implements InstagramService {
    static Type instaType;
    @Inject
    TelegramService telegramService;

    @Override
    public String getLinkVideo(InstagramRequest data) throws JsonProcessingException {
        SendMessageToBot sendMessageToBot = new SendMessageToBot();
        try {
            sendMessageToBot.sendMessage("Скачивание началось ! ✔️ " +
                    "\n Ссылка на видео 🔗: " + data.getUrl(), data.getChat());
        } catch (Exception ignored) {
        }
        instaType = getInstaType(data.getUrl());
        LocalDateTime requestTime = LocalDateTime.now();
        int index = data.getUrl().indexOf("?");
        if (index != -1) {
//            url = url.substring(0, index) + "?__a=1&__d=dis";
        } else {
//            url =url+"?__a=1&__d=dis";
        }
        List<String> responseUrl = new ArrayList<>();
        if (instaType.equals(Type.STORIES)) {
            String json = new GetDownloadUrlHelper().getUrl(data.getUrl(), instaType);
            responseUrl = extractUrlsFromData(json);
        } else {
            String json = new GetDownloadUrlHelper().getUrl(data.getUrl(), instaType);
            TemporaryResponse temporaryResponse = new ObjectMapper().readValue(json, TemporaryResponse.class);
            responseUrl = extractUrlsFromData(temporaryResponse.getData());
        }
        try {
            telegramService.sendAllToBotFromUrl(responseUrl, data.getUrl(), requestTime, data.getChat(), instaType, sendMessageToBot);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("ERROR TELEGRAM");
        }
        return "SUCCESS";
    }

    private static List<String> extractUrlsFromData(String data) {
        if (instaType.equals(Type.POST) || instaType.equals(Type.REELS)) {
            return imageSrcOrHrefExtractor(data);
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

    public static List imageSrcOrHrefExtractor(String html) {
        Document doc = Jsoup.parse(html);
        List<String> srcUrl = new ArrayList<>();
        List<String> hrefUrl = new ArrayList<>();
        // Извлечение значений атрибута src из тегов img и href

        Elements hrefTags = doc.select("a");
        for (Element hrefTag : hrefTags) {

            if (hrefTag.attr("href").contains(".xyz")) {
                hrefUrl.add(hrefTag.attr("href"));
            }
        }
            Elements imgTags = doc.select("img");
            for (Element imgTag : imgTags) {
                if (imgTag.attr("src").contains(".xyz")) {
                    srcUrl.add(imgTag.attr("src"));
                }
            }
            if (!hrefUrl.isEmpty()){
                instaType = Type.REELS;
                return hrefUrl;
            }
        return srcUrl;
    }

    public static List getDownloadUrlForStories(String data){
        System.out.println("data" + data);
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = jsonParser.parse(data);

        JsonObject jsonObject = rootElement.getAsJsonObject();
        JsonArray videoVersions = null;
        try {
            videoVersions = jsonObject.get("result")
                    .getAsJsonArray().get(0).getAsJsonObject()
                    .get("video_versions").getAsJsonArray();
        }
        catch (Exception ignored){

        }
        JsonArray candidates =jsonObject.get("result")
                .getAsJsonArray().get(0).getAsJsonObject()
                .get("image_versions2").getAsJsonObject()
                .get("candidates").getAsJsonArray();


        if (videoVersions == null ) {
            for (JsonElement element : candidates) {
                JsonObject candidate = element.getAsJsonObject();
                int width = candidate.get("width").getAsInt();
                int height = candidate.get("height").getAsInt();
                String url = candidate.get("url").getAsString();
                JsonObject urlSignatureObject = candidate.getAsJsonObject("url_signature");
                UrlSignature urlSignature = gson.fromJson(urlSignatureObject, UrlSignature.class);

                PhotoStories photoStory = new PhotoStories();
                photoStory.setWidth(width);
                photoStory.setHeight(height);
                photoStory.setUrl(url);
                photoStory.setUrl_signature(urlSignature);
                instaType = Type.POST;
                return List.of(photoStory.getUrl());
            }
        }
        for (JsonElement element : videoVersions) {
            JsonObject videoVersion = element.getAsJsonObject();
            int type = videoVersion.get("type").getAsInt();
            int width = videoVersion.get("width").getAsInt();
            int height = videoVersion.get("height").getAsInt();
            String url = videoVersion.get("url").getAsString();
            JsonObject urlSignatureObject = videoVersion.getAsJsonObject("url_signature");
            UrlSignature urlSignature = gson.fromJson(urlSignatureObject, UrlSignature.class);

            VideoStories videoStory = new VideoStories();
            videoStory.setType(type);
            videoStory.setWidth(width);
            videoStory.setHeight(height);
            videoStory.setUrl(url);
            videoStory.setUrl_signature(urlSignature);
            return List.of(videoStory.getUrl());
        }
        throw  new NoSuchElementException("Такое не возможно, или код не правильный");
    }


}

