package temurbeks.experiment.resource;

import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import temurbeks.experiment.entity.TelegramUser;
import temurbeks.experiment.utils.DownloadTask;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.service.InstagramService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;

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
        // Создаем ExecutorService с одним потоком для выполнения задачи
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Создаем задачу для скачивания
        DownloadTask downloadTask = new DownloadTask(instagram, request);

        // Запускаем задачу в фоновом режиме
        executorService.execute(downloadTask);

        // Завершаем работу ExecutorService после выполнения задачи
        executorService.shutdown();

        return "Success";
    }


    @POST
    @Path("/sendMessageToAll")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String sendToAll(@RequestBody StringEntity request, TelegramUser tgUser){
        if (instagram.sendToAll(request, tgUser)){
            return "Success";
        }
        return "Failed";
    }

    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getAll(TelegramUser tgUser){
        if (instagram.getAll(tgUser)){
            return "Success";
        }
        return "Failed";
    }

}
