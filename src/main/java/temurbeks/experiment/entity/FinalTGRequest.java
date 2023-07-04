package temurbeks.experiment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalTGRequest {
    private ArrayList<TelegramRequest> media;
    private String chat_id;
}
