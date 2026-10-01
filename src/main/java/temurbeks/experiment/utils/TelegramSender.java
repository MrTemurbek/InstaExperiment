package temurbeks.experiment.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import temurbeks.experiment.entity.FinalTGRequest;
import temurbeks.experiment.entity.TelegramRequest;
import temurbeks.experiment.telegram.TgLocal;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Отвечает за отправку медиа в Telegram через Bot API.
 * Эту часть не трогаем — здесь логика доставки, а не скачивания.
 */
public class TelegramSender {

    public final static String BOT_TOKEN = TgLocal.BOT_TOKEN;

    public Integer sendMedia(ArrayList<TelegramRequest> urls, String chat) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(Duration.ofSeconds(90))
                .build();
        FinalTGRequest finalTGRequest = new FinalTGRequest(urls, chat);

        // Преобразование объекта в JSON-строку
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
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
        try {
            response = client.newCall(request).execute();
            response.close();
            if (response.isSuccessful()) {
                System.out.println("Operation completed successfully!");
                client.dispatcher().cancelAll();
                client.connectionPool().evictAll();
            } else {
                System.out.println("Failed to send media. Response: " + response.body().string());
                response.close();
            }
            return response.code();
        } catch (Exception e) {
            response.close();
            client.dispatcher().cancelAll();
            client.connectionPool().evictAll();
            if (response.code() == 400) {
                if (sendBigVideo(urls.get(0).getMedia(), chat)) {
                    return 200;
                }
            }
            e.printStackTrace();
            return response.code();
        }
    }

    public static Boolean sendBigVideo(String url, String chat) throws IOException, InterruptedException {
        new SendMessageToBot().sendMessage("Походу файл большой, попытаемся скачать и отправить вам, это займёт время, пожалуйста ждите 🤞", chat);
        String uuid = UUID.randomUUID().toString();
        String videoFilePath = "video/" + uuid + ".mp4";

        File dir = new File("video");
        if (!dir.exists()) dir.mkdirs();

        // Скачиваем файл по прямой ссылке
        if (downloadVideoFile(url, videoFilePath)) {
            new SendMessageToBot().sendMessage("Скачали ! Отправляем 👌", chat);
        } else {
            new SendMessageToBot().sendMessage("Не получилось скачать ☹️", chat);
            return false;
        }

        File videoFile = new File(videoFilePath);
        double fileSizeInMB = (double) videoFile.length() / (1024 * 1024);

        if (fileSizeInMB > 49.5d) {
            new SendMessageToBot().sendMessage("Размер файла оказался большим, отправка займёт время, но мы отправим 😁", chat);
            return new TgLocal().sendVideoLocal(videoFilePath, chat);
        } else {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chat)
                    .addFormDataPart("video", videoFile.getName(),
                            RequestBody.create(MediaType.parse("video/mp4"), videoFile))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendVideo")
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                response.close();
                return response.isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Скачивает видео по URL в локальный файл.
     */
    private static boolean downloadVideoFile(String url, String destPath) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(Duration.ofSeconds(120))
                    .build();
            Request req = new Request.Builder().url(url).build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) return false;
                byte[] bytes = resp.body().bytes();
                java.nio.file.Files.write(java.nio.file.Paths.get(destPath), bytes);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
