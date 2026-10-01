package temurbeks.experiment.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.entity.TelegramUser;
import temurbeks.experiment.service.InstagramService;
import temurbeks.experiment.utils.DownloadTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/api")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstagramExperimentResource {

    @Inject
    InstagramService instagram;

    @POST
    @Path("/download")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String download(@RequestBody InstagramRequest request) {
        // Запускаем задачу в фоновом режиме
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                TelegramUser telegramUser = new TelegramUser(request.getChat(), "CHANNEL", "CHANNEL");
                instagram.getLinkVideo(request, telegramUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.shutdown();
        return "Success";
    }

    @POST
    @Path("/sendMessageToAll")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String sendToAll(@RequestBody StringEntity request, TelegramUser tgUser) {
        if (instagram.sendToAll(request, tgUser)) {
            return "Success";
        }
        return "Failed";
    }

    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getAll(TelegramUser tgUser) {
        if (instagram.getAll(tgUser)) {
            return "Success";
        }
        return "Failed";
    }
}
