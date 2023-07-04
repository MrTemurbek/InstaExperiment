package temurbeks.experiment.utils;

import com.google.gson.Gson;
import okhttp3.*;
import temurbeks.experiment.entity.FinalTGRequest;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;

public class TelegramSenderVideo {
    private final static String BOT_TOKEN = "5969680619:AAF6C7DwXEzHpv61Q8z9I7MaoknbKAJ6ZTs";

    public Integer sendVideo(ArrayList urls, String chat) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .build();
        FinalTGRequest finalTGRequest = new FinalTGRequest(urls, chat);
        Gson gson = new Gson();

        // Преобразование объекта в JSON-строку
        String json = gson.toJson(finalTGRequest);
        // Установка типа контента как application/json
        MediaType mediaType = MediaType.parse("application/json");
        // Создание экземпляра RequestBody с JSON-строкой
        RequestBody requestBody = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMediaGroup")
                .post(requestBody)
                .build();
        Response response = null;
        try  {
            response= client.newCall(request).execute();
            System.out.println("resp :"+ response);
            response.body().close();
            response.close();
            if (response.isSuccessful()) {
                System.out.println("Video sent successfully!");
                client.dispatcher().cancelAll();
                client.connectionPool().evictAll();
                return response.code();

            } else {
                System.out.println("Failed to send video. Response: " + response.body().string());
                response.body().close();
                return response.code();
            }
        } catch (Exception e) {
            System.out.println("resp :"+ response);
            response.body().close();
            System.out.println(e);
            return response.code();
        }
    }
}

