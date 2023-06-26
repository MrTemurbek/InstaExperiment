package temurbeks.experiment.exception;



import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * @author : Inam Kadirov
 * @created : 23/05/21, Sunday
 **/

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        e.printStackTrace();
        Response response = Response.status(200).type("application/json")
                .entity(new RuntimeException("GLOBALKA")).build();
        return response;

    }
}
