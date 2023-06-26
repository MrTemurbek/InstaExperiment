package temurbeks.experiment.service.serviceImpl;


import jakarta.enterprise.context.ApplicationScoped;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import temurbeks.experiment.service.TelegramService;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jakarta.ws.rs.core.UriBuilder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@ApplicationScoped
public class TelegramServiceImpl implements TelegramService {
    public final static Map<String, String> map = new HashMap<String, String>();
    @ConfigProperty(name = "telegram.channel.chat.id")
     String CHAT_ID;
    @ConfigProperty(name = "telegrambot.token")
    String TOKEN;

    @Override
    public void sendVideoToBotFromUrl(String videoUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2)
                .build();

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
                .queryParam("chat_id", CHAT_ID);

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
