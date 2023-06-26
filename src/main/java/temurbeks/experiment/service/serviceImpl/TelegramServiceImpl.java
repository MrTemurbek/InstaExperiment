package temurbeks.experiment.service.serviceImpl;


import jakarta.enterprise.context.ApplicationScoped;
import temurbeks.experiment.service.TelegramService;

import java.io.IOException;

import temurbeks.experiment.utils.DeleteAllInFolder;
import temurbeks.experiment.utils.DownloaderVideo;
import temurbeks.experiment.utils.SendMessageToBot;
import temurbeks.experiment.utils.TelegramSenderVideo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@ApplicationScoped
public class TelegramServiceImpl implements TelegramService {
    public final static Map<String, String> map = new HashMap<String, String>();
    @Override
    public void sendVideoToBotFromUrl(String videoUrl, String mainUrl, LocalDateTime time, String chatId) throws IOException, InterruptedException {
        DownloaderVideo videoDownloader = new DownloaderVideo();
        String savePath = "video/sample.mp4";
        LocalDateTime timeDownload = LocalDateTime.now();
        try {

            videoDownloader.downloadVideo(videoUrl, savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendMessageToBot sendMessageToBot = new SendMessageToBot();

        TelegramSenderVideo telegramSenderVideo = new TelegramSenderVideo();
        LocalDateTime sendTime = LocalDateTime.now();
        try {
            telegramSenderVideo.sendVideo(savePath);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        LocalDateTime timeDone = LocalDateTime.now();
        sendMessageToBot.sendMessage("URL :  "+mainUrl
                +" \n REQUEST : "+formatter(time)
                +"\n DOWNLOAD : "+difference(timeDownload, sendTime)
                +" sec\n SENT : "+difference(sendTime, timeDone)
                +" sec \n DOWNLOADED IN:" + difference(time, timeDone)+" seconds !", chatId);
        new DeleteAllInFolder().deleteInFolder();
    }

    public static String formatter(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh.mm.ss");
        return dateTime.format(formatter);
}

    public String difference(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        Duration duration = Duration.between(dateTime1, dateTime2);
        return Long.toString(duration.getSeconds());
    }

}
