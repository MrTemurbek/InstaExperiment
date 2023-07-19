package temurbeks.experiment.service.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import temurbeks.experiment.entity.TelegramRequest;
import temurbeks.experiment.entity.Type;
import temurbeks.experiment.service.TelegramService;
import temurbeks.experiment.utils.DeleteAllInFolder;
import temurbeks.experiment.utils.SendMessageToBot;
import temurbeks.experiment.utils.TelegramSender;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TelegramServiceImpl implements TelegramService {
    @Override
    public void sendAllToBotFromUrl(List<String> urls, String mainUrl, LocalDateTime time, String chatId, Type type) throws IOException, InterruptedException {
        try {
            boolean result;
            ArrayList<TelegramRequest> requests = new ArrayList<>();
            TelegramSender telegramSender = new TelegramSender();
            if (type.equals(Type.POST)) {
                for (String postUrls : urls) {
                    requests.add(new TelegramRequest("photo", postUrls));
                }
            } else {
                for (String videoUrl : urls) {
                    requests.add(new TelegramRequest("video", videoUrl));
                }
            }
            result = telegramSender.sendMedia(requests, chatId).equals(200);
            SendMessageToBot sendMessageToBot = new SendMessageToBot();
            LocalDateTime timeDone = LocalDateTime.now();
            if (result){
                if (type.equals(Type.POST)){
                    sendMessageToBot.sendMessage("Фотография обработано за " + difference(time, timeDone) + " секунды ⏳", chatId);

                }
                else {
                    sendMessageToBot.sendMessage("Видео обработано за " + difference(time, timeDone) + " секунды ⏳", chatId);

                }
            } else {
                sendMessageToBot.sendMessage("Не получилось скачать☹️, свяжитесь с @Mr_Temurbek, обработано за :"+ difference(time, timeDone), chatId);
            }
            new DeleteAllInFolder().deleteInFolder();
        } catch (Exception e) {
            LocalDateTime timeDone = LocalDateTime.now();
            new SendMessageToBot().sendMessage("Не получилось скачать☹️, свяжитесь с @Mr_Temurbek, обработано за :"+ difference(time, timeDone), chatId);
            e.printStackTrace();
        }
    }

    public String difference(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        Duration duration = Duration.between(dateTime1, dateTime2);
        return Long.toString(duration.getSeconds());
    }

}
