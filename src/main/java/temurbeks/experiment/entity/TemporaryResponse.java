package temurbeks.experiment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TemporaryResponse {
    private String status;
    private String  p;
    private String data;

    public TemporaryResponse(String status, String p, String data) {
        this.status = status;
        this.p = p;
        this.data = data;
    }
    
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
