package temurbeks.experiment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstagramMediaItem {
    private String mediaUrl;
    private String localPath;
    private String caption;
    private String title;
    private String performer;
    private int duration;
    private double fileSizeMB;
    private boolean isVideo;

    public InstagramMediaItem(String mediaUrl, String caption, String title, String performer, int duration, boolean isVideo) {
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.title = title;
        this.performer = performer;
        this.duration = duration;
        this.isVideo = isVideo;
    }
}
