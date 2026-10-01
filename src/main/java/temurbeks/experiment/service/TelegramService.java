package temurbeks.experiment.service;

import temurbeks.experiment.entity.InstagramMediaItem;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface TelegramService {

    void sendAllToBotFromUrl(List<InstagramMediaItem> items, String mainUrl, LocalDateTime time, String chatId) throws IOException, InterruptedException;
}
