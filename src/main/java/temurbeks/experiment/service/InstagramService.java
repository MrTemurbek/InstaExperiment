package temurbeks.experiment.service;

import temurbeks.experiment.entity.InstagramRequest;

import java.io.IOException;

public interface InstagramService {
    String getLinkVideo(InstagramRequest request) throws IOException, InterruptedException;
}
