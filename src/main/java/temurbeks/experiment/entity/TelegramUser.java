package temurbeks.experiment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUser {
    private String id;
    private String name;
    private String username;
}
