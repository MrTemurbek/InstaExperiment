package temurbeks.experiment.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.enterprise.context.ApplicationScoped;


import com.fasterxml.jackson.databind.ObjectMapper;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.TemporaryResponse;
import temurbeks.experiment.entity.Type;
import temurbeks.experiment.service.InstagramService;
import temurbeks.experiment.service.TelegramService;
import jakarta.inject.Inject;
import temurbeks.experiment.utils.GetDownloadUrlHelper;
import temurbeks.experiment.utils.SendMessageToBot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
public class InstagramServiceImpl implements InstagramService {
//    @Inject
//    @RestClient
//    IgDownloaderRest igDownloaderRest;

    @Inject
    TelegramService telegramService;

    @Override
    public String getLinkVideo(InstagramRequest data) throws JsonProcessingException {
        SendMessageToBot sendMessageToBot =new SendMessageToBot();
        try {
            sendMessageToBot.sendMessage("Скачивание началось ! " +
                    "\n url: " +data.getUrl() , data.getChat());
        }
        catch (Exception ignored){}
        Type instaType = getInstaType(data.getUrl());
        LocalDateTime requestTime = LocalDateTime.now();
        int index = data.getUrl().indexOf("?");
        if (index != -1) {
//            url = url.substring(0, index) + "?__a=1&__d=dis";
        }
        else {
//            url =url+"?__a=1&__d=dis";
        }
        List<String> responseUrl = new ArrayList<>();
        if (instaType.equals(Type.STORIES)){

        }
        else {
            String json = new GetDownloadUrlHelper().getUrl(data.getUrl(), instaType);
            TemporaryResponse temporaryResponse = new ObjectMapper().readValue(json, TemporaryResponse.class);
            responseUrl = extractUrlsFromData(temporaryResponse.getData());
        }
        try {
            telegramService.sendAllToBotFromUrl(responseUrl, data.getUrl(), requestTime, data.getChat(), instaType,sendMessageToBot );
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("ERROR TELEGRAM");
        }
        return "SUCCESS";
    }

    private static List<String> extractUrlsFromData(String data) {
        List<String> urls = new ArrayList<>();
        String startToken = "href=\"";
        String endToken = "\"";
        int startIndex = 0;

        while (startIndex >= 0) {
            startIndex = data.indexOf(startToken, startIndex);
            if (startIndex >= 0) {
                int endIndex = data.indexOf(endToken, startIndex + startToken.length());
                if (endIndex >= 0) {
                    String url = data.substring(startIndex + startToken.length(), endIndex);
                    if (url.contains(".xyz")){
                        urls.add(url);
                    }
                    startIndex = endIndex;
                }
            }
        }

        return urls;
    }
    
    private Type getInstaType(String url){
        if (url.contains(".com/reel")){
            return Type.REELS;
        } else if (url.contains(".com/stories/")) {
            return Type.STORIES;
        }
        else {
            return Type.POST;
        }
    }
}

