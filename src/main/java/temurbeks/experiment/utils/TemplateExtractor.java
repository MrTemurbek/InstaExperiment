package temurbeks.experiment.utils;

import temurbeks.experiment.entity.QuizEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateExtractor {
    public static QuizEntity extractValues(String template) {
        List<String> extractedValues = new ArrayList<>();
        Pattern pattern = Pattern.compile("Question:\\s*(.*?)Options:\\s*(.*?)Correct Option:\\s*(.*?)Explanation:\\s*(.*)",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(template);
        if (matcher.find()) {
            extractedValues.add(matcher.group(1).trim()); // Question
            String optionsText = matcher.group(2).trim();
            String[] options = optionsText.split("\\n");
            for (String option : options) {
                extractedValues.add(option.trim()); // Options
            }
            extractedValues.add(matcher.group(3).trim()); // Correct Option
            extractedValues.add(matcher.group(4).trim()); // Explanation
        }
        QuizEntity quizEntity = new QuizEntity();
        quizEntity.setQuestion(extractedValues.get(0));
        List<String> options = new ArrayList<>();
        for (int i = 1; i < extractedValues.size(); i++) {
            if (options.contains(extractedValues.get(i))){
                quizEntity.setCorrectOption(options.indexOf(extractedValues.get(i)));
                break;
            }
            options.add(extractedValues.get(i));
        }
        quizEntity.setOptions(options);
        quizEntity.setExplanation(extractedValues.get(extractedValues.size() - 1));
        return quizEntity;
    }
}

