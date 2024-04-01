package temurbeks.experiment.telegram;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import temurbeks.experiment.entity.QuizEntity;
import temurbeks.experiment.entity.TelegramUser;
import temurbeks.experiment.service.InstagramService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static temurbeks.experiment.utils.TemplateExtractor.extractValues;

@ApplicationScoped
public class TelegramBotHandler extends TelegramLongPollingBot {

    public TelegramBotHandler(InstagramService instagram) {
        this.instagram = instagram;
    }

    private String USERNAME = "Pomoshnik_uz_robot";
    private String TOKEN = "1497637733:AAGs5QgCmrMf_Qqxh5KuDcKAge2TLtjlKD8";

    @Inject
    InstagramService instagram;
    private final Map<Long, Long> lastProcessedTimestamps = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Starting Bot!");
        Message message = update.getMessage();
        Long userId = message.getChatId();
        String text = message.getText();
        String name;
        if (StringUtils.isEmpty(message.getChat().getLastName())) {
            name = message.getChat().getFirstName();
        } else {
            name = message.getChat().getFirstName() + " " + message.getChat().getLastName();
        }
        String username;
        if (StringUtils.isEmpty(message.getChat().getUserName())) {
            username = "NULL";
        } else {
            username = message.getChat().getUserName();
        }
        TelegramUser tgUser = new TelegramUser(userId.toString(), name, username);

        if (text.contains("/start")) {
            sender(message, "Привет, этот бот поможет создать опросник \n" +
                    "Hello, this bot can help you with creation quizzes \n \n" +
                    "Author/Автор: @Mr_Temurbek");
        } else if (text.toLowerCase().contains("question")) {
            QuizEntity quizEntity = extractValues(text);
            pollCreator(message, quizEntity.getQuestion(), quizEntity.getOptions(), quizEntity.getCorrectOption(), quizEntity.getExplanation());
        } else {
            sender(message, "Не правильный запрос на бот!");
        }
    }

    @Override
    public String getBotUsername() {
        return USERNAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }


    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    public void runBot(@Observes StartupEvent event) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBotHandler telegramBotHandler = new TelegramBotHandler(instagram);
            telegramBotHandler.setTOKEN(TOKEN);
            telegramBotHandler.setUSERNAME(USERNAME);
            telegramBotsApi.registerBot(telegramBotHandler);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sender(Message message, String text) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void pollCreator(Message message, String question, List<String> options, Integer correctOption, String explanation) {

        Boolean isAnonymous = true;
        String pollType = "quiz";
        Boolean allowMulltipleVotes = false;
        Boolean isClosed = false;
        Boolean disableNotifications = false;
        Integer openPeriod = null;
        Integer closeDate = null;

        if (StringUtils.isEmpty(explanation) || explanation.contains("null")) {
            explanation = null;
        }

        SendPoll sendPoll = new SendPoll(message.getChatId().toString(), 0, question, options, isAnonymous, pollType,
                allowMulltipleVotes, correctOption,
                isClosed, disableNotifications, 0,
                null, openPeriod, closeDate, explanation,
                null, null, true, true);

        try {
            execute(sendPoll);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
