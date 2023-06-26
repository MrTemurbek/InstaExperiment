package temurbeks.experiment.service;

import jakarta.enterprise.context.ApplicationScoped;
import temurbeks.experiment.service.serviceImpl.InstagramServiceImpl;

@ApplicationScoped
public class InstagramService implements InstagramServiceImpl {

    @Override
    public String getLinkVideo(String url) {
        return null;
    }
}
