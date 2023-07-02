package temurbeks.experiment.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface TelegramService {
    void sendAllToBotFromUrl(List<String> videoUrls, String mainUrl, LocalDateTime time, String chatId) throws IOException, InterruptedException;

}
