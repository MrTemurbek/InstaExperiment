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
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.entity.TelegramUser;
import temurbeks.experiment.service.InstagramService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        long currentTime = System.currentTimeMillis();

        if (text.contains("instagram.com/")) {
            // Check if the chatId was processed before and calculate the time difference
            long lastProcessedTimestamp = lastProcessedTimestamps.getOrDefault(userId, 0L);
            long timeDifference = currentTime - lastProcessedTimestamp;

            // Update the last processed timestamp for this chatId
            lastProcessedTimestamps.put(userId, System.currentTimeMillis());

            // Process the request with a new thread
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            String finalText = text;
            executorService.execute(() -> {
                try {
                    instagram.getLinkVideo(new InstagramRequest(finalText, userId.toString()), tgUser);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            executorService.shutdown();

            // If there is a time difference, wait before processing other requests
            if (timeDifference > 0) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (text.contains("/start")) {
            sender(message, "Привет, этот бот поможет скачать видео с Инстаграма \n" +
                    "Hello, this bot can help you with downloading Instagram video \n \n" +
                    "Author/Автор: @Mr_Temurbek");
        } else if (text.startsWith("TO_ALL")) {
            text = text.substring(6);
            instagram.sendToAll(new StringEntity(text), tgUser);
        } else if (text.startsWith("GET_ALL")) {
            instagram.getAll(tgUser);
        } else if (text.contains("QUIZZES")) {
            pollCreator(message, "Text");
        } else {
            sender(message, "Не правильный запрос на бот, \n отправьте ссылку на бот!");
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

    public void pollCreator(Message message, String quiz) {
        String question = "Is it ok ?";
        String pollId = UUID.randomUUID().toString();
        Integer optionId1 = 1;
        Integer optionId2 = 2;
        Integer optionId3 = 3;
        List<String> options = List.of("one", "two", "three");
        Boolean isAnonymous = true;
        PollAnswer answer = new PollAnswer(pollId, new User(), List.of(optionId1, optionId2, optionId3));
        Integer correctOption = 1;
        String pollType = "quiz";
        Boolean allowMulltipleVotes = false;
        Boolean isClosed = false;
        Boolean disableNotifications = false;
        Integer replyToMessageId = 0;
        Integer openPeriod = null;
        Integer closeDate = null;
        String explanation = "Working";

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
