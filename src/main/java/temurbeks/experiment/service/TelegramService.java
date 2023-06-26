package temurbeks.experiment.service;

import java.io.IOException;
import java.time.LocalDateTime;

public interface TelegramService {
    void sendVideoToBotFromUrl(String videoUrl, String mainUrl, LocalDateTime time, String chatId) throws IOException, InterruptedException;

}
