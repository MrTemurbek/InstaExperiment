package temurbeks.experiment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizEntity {
    private String question;
    private List<String> options;
    private Integer correctOption;
    private String explanation;
}
