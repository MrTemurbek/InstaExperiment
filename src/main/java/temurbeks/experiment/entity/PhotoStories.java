package temurbeks.experiment.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoStories {
    private int width;
    private int height;
    private String url;
    private UrlSignature url_signature;
}
