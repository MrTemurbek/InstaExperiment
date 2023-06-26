package temurbeks.experiment.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import temurbeks.experiment.entity.TemporaryResponse;


@Path("/api")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public interface IgDownloaderRest {


    @POST
    @Path("/endpoint")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    TemporaryResponse sendFormData(Form formData);
}
