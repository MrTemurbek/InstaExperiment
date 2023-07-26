package temurbeks.experiment.telegram;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import temurbeks.experiment.entity.InstagramRequest;
import temurbeks.experiment.entity.StringEntity;
import temurbeks.experiment.service.InstagramService;
import java.io.IOException;

@ApplicationScoped
public class TelegramBotHandler extends TelegramLongPollingBot {

    public TelegramBotHandler(InstagramService instagram) {
        this.instagram = instagram;
    }

    private String USERNAME = "instagram_down_robot";
    private  String TOKEN = "5969680619:AAF6C7DwXEzHpv61Q8z9I7MaoknbKAJ6ZTs";

    @Inject
    InstagramService instagram;


    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Starting Bot!");
        Message message = update.getMessage();
        Long userId = message.getChatId();
        String text  = message.getText();
        if (text.contains("instagram.com/")) {
            try {
                instagram.getLinkVideo(new InstagramRequest(text, userId.toString()));
            } catch (IOException | InterruptedException e ) {
                sender(message, "☹️ Обработка не удалась ☹️, свяжитесь с @Mr_Temurbek");
            }
        }
        else if (text.contains("/start")){
            sender(message, "Привет, этот бот поможет скачать видео с Инстаграма \n" +
                    "Hello, this bot can help you with downloading Instagram video \n \n+" +
                    "Author/Автор: @Mr_Temurbek");
        }
        else if (text.startsWith("TOALL")){
            text = text.substring(5);
            instagram.sendToAll(new StringEntity(text));
        }
        else {
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
            System.out.println(e.getMessage());
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

}
