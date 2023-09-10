package temurbeks.experiment.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import temurbeks.experiment.entity.PhotoStories;
import temurbeks.experiment.entity.Type;
import temurbeks.experiment.entity.UrlSignature;
import temurbeks.experiment.entity.VideoStories;
import temurbeks.experiment.service.serviceImpl.InstagramServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Extractor {
    public static List<String> postOrReelsExtractor(String json, String chatId) {

        System.out.println("DATA DATA -> " + json);
        Document doc = Jsoup.parse(json);
        List<String> photoUrls = new ArrayList<>();
        List<String> videoUrls = new ArrayList<>();
        List<String> extractUrl = new ArrayList<>();
        InstagramServiceImpl.instaType = Type.REELS;

        Gson gson = new Gson();

        JsonElement rootElement = gson.fromJson(json, JsonElement.class);

        JsonObject jsonObject = rootElement.getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();

        try {
            if (!jsonObject.has("video_versions")){
                InstagramServiceImpl.instaType = Type.POST;
                JsonArray photoInfos = jsonObject.getAsJsonArray("carousel_media");
                for (JsonElement jsonPhotoElement : photoInfos){
                    int width = jsonPhotoElement.getAsJsonObject().getAsJsonObject("original_width").getAsInt();
                    int height = jsonPhotoElement.getAsJsonObject().getAsJsonObject("original_height").getAsInt();
                    JsonArray imageUrls = jsonPhotoElement.getAsJsonObject().getAsJsonObject("image_versions2").getAsJsonObject().get("candidates").getAsJsonArray();
                    for (JsonElement images : imageUrls ){
                        if (images.getAsJsonObject().get("width").getAsInt()==width && images.getAsJsonObject().get("height").getAsInt()==height){
                            photoUrls.add(images.getAsJsonObject().get("url").getAsString());
                            break;
                        }
                    }
                }

                return photoUrls;
            }

        } catch (Exception e) {

        }
        return videoUrls;
    }

    public static List<String> getDownloadUrlForStories(String data) {
        Gson gson = new Gson();
        JsonElement rootElement = gson.fromJson(data, JsonElement.class);

        JsonObject jsonObject = rootElement.getAsJsonObject();
        JsonArray videoVersions = null;
        try {
            videoVersions = jsonObject.get("result")
                    .getAsJsonArray().get(0).getAsJsonObject()
                    .get("video_versions").getAsJsonArray();
        } catch (Exception ignored) {

        }
        JsonArray candidates = jsonObject.get("result")
                .getAsJsonArray().get(0).getAsJsonObject()
                .get("image_versions2").getAsJsonObject()
                .get("candidates").getAsJsonArray();


        if (videoVersions == null) {
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
                InstagramServiceImpl.instaType = Type.POST;
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
        throw new NoSuchElementException("Такое не возможно, или код не правильный");
    }
}
