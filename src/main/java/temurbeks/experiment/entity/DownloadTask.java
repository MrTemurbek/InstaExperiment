package temurbeks.experiment.entity;

import temurbeks.experiment.service.InstagramService;

import java.io.IOException;

public class DownloadTask implements Runnable {
    private InstagramService instagram;
    private InstagramRequest request;

    public DownloadTask(InstagramService instagram, InstagramRequest request) {
        this.instagram = instagram;
        this.request = request;
    }

    @Override
    public void run() {
        try {
            instagram.getLinkVideo(request);
        } catch (IOException | InterruptedException e) {
            // Обработка ошибок
            e.printStackTrace();
        }
    }
}