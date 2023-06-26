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

    public TemporaryResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
