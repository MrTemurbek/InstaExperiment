package temurbeks.experiment.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.service.InstagramService;
import temurbeks.experiment.service.TelegramService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/api")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstagramExperimentResource {

    @Inject
    InstagramService instagram;

    @Inject
    TelegramService telegramService;

    @POST
    @Path("/download")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String download(InstagramRequest request) {
        instagram.getLinkVideo(request.getUrl());
        return "Success";
    }

    @POST
    @Path("/telegram")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String sendMessage(InstagramRequest request) throws IOException, InterruptedException {
        telegramService.sendVideoToBotFromUrl(request.getUrl());
        return "Success";
    }
}
