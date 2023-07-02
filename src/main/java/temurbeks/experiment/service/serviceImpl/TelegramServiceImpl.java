package temurbeks.experiment.service.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import temurbeks.experiment.insta.ReelsDownloader;
import temurbeks.experiment.service.TelegramService;
import java.io.IOException;
import temurbeks.experiment.utils.DeleteAllInFolder;
import temurbeks.experiment.utils.SendMessageToBot;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class TelegramServiceImpl implements TelegramService {
    @Override
    public void sendAllToBotFromUrl(List<String> videoUrls, String mainUrl, LocalDateTime time, String chatId) throws IOException, InterruptedException {
        SendMessageToBot sendMessageToBot = new SendMessageToBot();
        for (String videoUrl: videoUrls) {
            new ReelsDownloader().reels(videoUrl, chatId);
        }
        LocalDateTime timeDone = LocalDateTime.now();
        sendMessageToBot.sendMessage("URL :  "+mainUrl
                +" \n REQUEST : "+formatter(time)
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
