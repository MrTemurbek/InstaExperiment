package temurbeks.experiment.service.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import temurbeks.experiment.entity.TelegramRequest;
import temurbeks.experiment.entity.Type;
import temurbeks.experiment.insta.ReelsDownloader;
import temurbeks.experiment.service.TelegramService;
import java.io.IOException;
import temurbeks.experiment.utils.DeleteAllInFolder;
import temurbeks.experiment.utils.SendMessageToBot;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TelegramServiceImpl implements TelegramService {
    @Override
    public void sendAllToBotFromUrl(List<String> videoUrls, String mainUrl, LocalDateTime time, String chatId, Type type, SendMessageToBot sendMessageToBot) {
try {
    Boolean result = null;
    if (type.equals(Type.STORIES)){}
    else {
        ArrayList<TelegramRequest> requests = new ArrayList<>();
        for (String videoUrl: videoUrls) {
            requests.add(new TelegramRequest("video", videoUrl));
        }
            result=  new ReelsDownloader().reels(requests, chatId);
    }
    try {

        if (result.equals(Boolean.TRUE)){
            LocalDateTime timeDone = LocalDateTime.now();
            sendMessageToBot.sendMessage("DOWNLOADED IN: " + difference(time, timeDone)+" seconds !", chatId);

        }
        else {
            sendMessageToBot.sendMessage("Unsuccessful :(", chatId);
        }
    }
    catch (Exception e){
        e.printStackTrace();
        System.out.println("Vozmojno zdes ?");
    }

    new DeleteAllInFolder().deleteInFolder();
}
catch (Exception e){
    System.out.println("here "+e);
}
    }

    public String difference(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        Duration duration = Duration.between(dateTime1, dateTime2);
        return Long.toString(duration.getSeconds());
    }

}
