package temurbeks.experiment.service;

import java.io.IOException;

public interface TelegramService {
    void sendVideoToBotFromUrl(String videoUrl) throws IOException, InterruptedException;

}
