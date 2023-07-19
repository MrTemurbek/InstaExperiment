package temurbeks.experiment.entity;

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
