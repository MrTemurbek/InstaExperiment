package temurbeks.experiment.service;

import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;

import java.io.IOException;

public interface InstagramService {
    String getLinkVideo(InstagramRequest request) throws IOException, InterruptedException;
    Boolean sendToAll(StringEntity message);
}
