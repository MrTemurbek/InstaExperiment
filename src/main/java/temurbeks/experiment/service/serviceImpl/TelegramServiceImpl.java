package temurbeks.experiment.service.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;
import temurbeks.experiment.entity.InstagramMediaItem;
import temurbeks.experiment.entity.TelegramRequest;
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
    public void sendAllToBotFromUrl(List<InstagramMediaItem> items, String mainUrl, LocalDateTime time, String chatId) throws IOException, InterruptedException {
        try {
            boolean result;
            if (items == null || items.isEmpty()) {
                LocalDateTime timeDone = LocalDateTime.now();
                new SendMessageToBot().sendMessage("Ничего не найдено по ссылке ☹️, обработано за : " + difference(time, timeDone), chatId);
                return;
            }

            ArrayList<TelegramRequest> requests = new ArrayList<>();
            for (InstagramMediaItem item : items) {
                String mediaType = item.isVideo() ? "video" : "photo";
                requests.add(new TelegramRequest(mediaType, item.getMediaUrl()));
            }

            TelegramSender telegramSender = new TelegramSender();
            result = telegramSender.sendMedia(requests, chatId) == 200;

            SendMessageToBot sendMessageToBot = new SendMessageToBot();
            LocalDateTime timeDone = LocalDateTime.now();

            if (result) {
                sendMessageToBot.sendMessage("Медиа обработано за " + difference(time, timeDone) + " секунды ⏳", chatId);
            } else {
                sendMessageToBot.sendMessage("Не получилось отправить медиа ☹️, свяжитесь с @Mr_Temurbek, обработано за : " + difference(time, timeDone), chatId);
            }
            new DeleteAllInFolder().deleteInFolder();
        } catch (Exception e) {
            LocalDateTime timeDone = LocalDateTime.now();
            new SendMessageToBot().sendMessage("Не получилось скачать ☹️, свяжитесь с @Mr_Temurbek, обработано за : " + difference(time, timeDone), chatId);
            e.printStackTrace();
        }
    }

    public String difference(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        Duration duration = Duration.between(dateTime1, dateTime2);
        return Long.toString(duration.getSeconds());
    }
}
