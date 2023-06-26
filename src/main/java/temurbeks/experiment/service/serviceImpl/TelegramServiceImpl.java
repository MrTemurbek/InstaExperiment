package temurbeks.experiment.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import temurbeks.experiment.service.serviceImpl.TelegramServiceImpl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@ApplicationScoped
public class TelegramService implements TelegramServiceImpl {
    public final static Map<String, String> map = new HashMap<String, String>();
    private static final String CHAT_ID = "-1001529421660";
    private static final String TOKEN = "5554490736:AAEIf3SCkORKKYg9wnAaVypWHVLj2Ttd_vQ";

    @Override
    public void sendVideoToBotFromUrl(String videoUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2)
                .build();

        String videoCaption = "Описание видео";

        // Создание временного файла для сохранения видео
        File tempFile = File.createTempFile("video", ".mp4");

        // Загрузка видео с URL и сохранение во временный файл
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request1 = new Request.Builder()
                .url(videoUrl)
                .build();
        try (Response response = okHttpClient.newCall(request1).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    try (InputStream inputStream = new BufferedInputStream(responseBody.byteStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                        fileOutputStream.flush();
                    }
                }
            } else {
                throw new IOException("Failed to download video. HTTP status code: " + response.code());
            }
        }

        // Отправка видео в Telegram бот
        UriBuilder builder = UriBuilder
                .fromUri("https://api.telegram.org")
                .path("/{token}/sendVideo")
                .queryParam("chat_id", CHAT_ID)
                .queryParam("caption", videoCaption);

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofFile(tempFile.toPath());
        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .uri(builder.build("bot" + TOKEN))
                .timeout(Duration.ofSeconds(5))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("SUCCESS!");

        // Удаление временного файла
        tempFile.delete();
    }
}
