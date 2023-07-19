package temurbeks.experiment.resource;

import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.service.InstagramService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;

import java.io.*;


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
    public String download(@RequestBody InstagramRequest request) throws IOException, InterruptedException {
        instagram.getLinkVideo(request);
        return "Success";
    }


    @POST
    @Path("/sendMessageToAll")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String sendToAll(@RequestBody StringEntity request){
        if (instagram.sendToAll(request)){
            return "Success";
        }
        return "Failed";
    }

}
