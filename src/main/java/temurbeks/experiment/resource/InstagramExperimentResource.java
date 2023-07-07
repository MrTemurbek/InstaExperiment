package temurbeks.experiment.resource;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.service.InstagramService;
import temurbeks.experiment.service.TelegramService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import temurbeks.experiment.utils.SendMessageToBot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Path("/api")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstagramExperimentResource {

    @Inject
    InstagramService instagram;

    private static final String FILE_PATH = "chat_id.txt";
    private static List<String> chatIdList;

    public void onStart(@Observes StartupEvent event) {
        chatIdList = readStaticListFromFile();
        // Делайте что-то с вашим статическим списком после чтения из файла
    }

    public void addToStaticList(String id) {
        chatIdList.add(id);
        saveStaticListToFile();
    }

    @POST
    @Path("/download")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String download(@RequestBody InstagramRequest request) throws IOException, InterruptedException {
        if (!chatIdList.contains(request.getChat())){
            try {
                addToStaticList(request.getChat());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        instagram.getLinkVideo(request);
        return "Success";
    }


    @POST
    @Path("/sendMessageToAll")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String sendToAll(@RequestBody StringEntity request) throws IOException, InterruptedException {
        for (String chatID: chatIdList) {
            new SendMessageToBot().sendMessage(request.getMessage(), chatID);
        }
        return "Success";
    }








    private List<String> readStaticListFromFile() {
        List<String> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            // Обработка ошибки чтения файла
            e.printStackTrace();
        }

        return list;
    }

    private void saveStaticListToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String id : chatIdList) {
                writer.write(id);
                writer.newLine();
            }
        } catch (IOException e) {
            // Обработка ошибки записи в файл
            e.printStackTrace();
        }
    }
}
