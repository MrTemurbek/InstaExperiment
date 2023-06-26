package temurbeks.experiment.service.serviceImpl;

import java.io.IOException;

public interface TelegramServiceImpl {
    void sendVideoToBotFromUrl(String videoUrl) throws IOException, InterruptedException;

}
