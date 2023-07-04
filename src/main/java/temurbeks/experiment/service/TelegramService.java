package temurbeks.experiment.service;

import temurbeks.experiment.entity.Type;
import temurbeks.experiment.utils.SendMessageToBot;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface TelegramService {
    void sendAllToBotFromUrl(List<String> videoUrls, String mainUrl, LocalDateTime time, String chatId, Type type, SendMessageToBot sendMessageToBot) throws IOException, InterruptedException;

}
