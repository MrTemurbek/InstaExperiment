package temurbeks.experiment.entity;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InstagramRequest {
    private String url;
    private String chat;

    public InstagramRequest(String url, String chat) {
        this.url = url;
        this.chat = chat;
    }

    public String getChat() {
        return chat;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
