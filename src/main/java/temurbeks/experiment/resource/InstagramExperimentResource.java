package temurbeks.experiment.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/download")
public class InstagramExperimentResource {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String download() {

        return "Success";
    }
}
