package temurbeks.experiment.insta;

import temurbeks.experiment.utils.DownloaderVideo;
import temurbeks.experiment.utils.TelegramSenderVideo;

import java.io.IOException;
import java.util.UUID;

public class ReelsDownloader {
    public void reels(String url, String chatId){
        String uuid = UUID.randomUUID().toString();
        DownloaderVideo videoDownloader = new DownloaderVideo();
        String savePath = "video/"+uuid+".mp4";
        try {
            videoDownloader.downloadVideo(url, savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TelegramSenderVideo telegramSenderVideo = new TelegramSenderVideo();
        try {
            telegramSenderVideo.sendVideo(savePath, chatId);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
