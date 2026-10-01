package temurbeks.experiment.telegram;

import okhttp3.*;
import java.io.File;
import java.time.Duration;

/**
 * Вспомогательный класс для отправки больших видео-файлов через локальный Bot API сервер.
 * Используется TelegramSender когда файл больше 49.5 МБ.
 */
public class TgLocal {

    // Токен бота (должен совпадать с TelegramSender.BOT_TOKEN)
    public static final String BOT_TOKEN = "5969680619:AAF6C7DwXEzHpv61Q8z9I7MaoknbKAJ6ZTs";

    /**
     * Отправляет видео через локальный Telegram Bot API сервер (127.0.0.1:8081).
     * Нужен для файлов > 50 МБ, которые нельзя залить через стандартный API.
     *
     * @param videoFilePath путь к локальному файлу
     * @param chat          chat_id получателя
     * @return true если отправка успешна
     */
    public boolean sendVideoLocal(String videoFilePath, String chat) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(Duration.ofSeconds(75))
                    .build();

            File videoFile = new File(videoFilePath);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chat)
                    .addFormDataPart("width", "1080")
                    .addFormDataPart("height", "1920")
                    .addFormDataPart("video", videoFile.getName(),
                            RequestBody.create(MediaType.parse("video/mp4"), videoFile))
                    .build();

            Request request = new Request.Builder()
                    .url("http://127.0.0.1:8081/bot" + BOT_TOKEN + "/sendVideo")
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            client.dispatcher().cancelAll();
            response.close();
            return response.isSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
