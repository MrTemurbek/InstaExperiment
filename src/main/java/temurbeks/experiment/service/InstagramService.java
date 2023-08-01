package temurbeks.experiment.service;

import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.entity.TelegramUser;

import java.io.IOException;

public interface InstagramService {
    String getLinkVideo(InstagramRequest request, TelegramUser tgUser) throws IOException, InterruptedException;
    Boolean sendToAll(StringEntity message, TelegramUser tgUser);
    Boolean getAll(TelegramUser tgUser);
}
