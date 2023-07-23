package temurbeks.experiment.telegram;

import okhttp3.*;

import java.io.File;
import java.time.Duration;

import static temurbeks.experiment.utils.TelegramSender.BOT_TOKEN;

public class TgLocal {
    public boolean sendVideoLocal(String videoFilePath, String chat){
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
                    .addFormDataPart("video", videoFile.getName() ,
                            RequestBody.create(MediaType.parse("video/mp4"), videoFile))
                    .build();
            Request request = new Request.Builder()
                    .url("http://127.0.0.1:8081/bot"+BOT_TOKEN+"/sendVideo")
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            client.dispatcher().cancelAll();
            response.close();

            return response.isSuccessful();
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
