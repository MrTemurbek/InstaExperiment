package temurbeks.experiment.entity;

public class InstagramRequest {
    private String url;

    public InstagramRequest(String url) {
        this.url = url;
    }

    public InstagramRequest() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
