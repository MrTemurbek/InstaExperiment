package temurbeks.experiment.utils;

import okhttp3.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.IOException;

public class TelegramSenderVideo {
    private final static String CHAT_ID = "-1001529421660";
    private final static String BOT_TOKEN= "5969680619:AAF6C7DwXEzHpv61Q8z9I7MaoknbKAJ6ZTs";

    public void sendVideo(String videoFilePath) throws IOException {
        OkHttpClient client = new OkHttpClient();

        File videoFile = new File(videoFilePath);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", CHAT_ID)
                .addFormDataPart("video", videoFile.getName(),
                        RequestBody.create(MediaType.parse("video/mp4"), videoFile))
                .build();

        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendVideo")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Video sent successfully!");
            } else {
                System.out.println("Failed to send video. Response: " + response.body().string());
            }
        }
}
}
