package temurbeks.experiment.utils;

import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.TelegramUser;
import temurbeks.experiment.service.InstagramService;

public class DownloadTask implements Runnable {
    private InstagramService instagram;
    private InstagramRequest request;

    public DownloadTask(InstagramService instagram, InstagramRequest request) {
        this.instagram = instagram;
        this.request = request;
    }

    @Override
    public void run() {
        TelegramUser telegramUser = new TelegramUser(request.getChat(), "CHANNEL", "CHANNEL");
        instagram.getLinkVideo(request, telegramUser);
    }
}