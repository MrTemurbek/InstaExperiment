package temurbeks.experiment.utils;

import com.google.gson.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import temurbeks.experiment.entity.Type;
import temurbeks.experiment.exception.MyException;

import java.io.IOException;


public class GetDownloadUrlHelper {
    public String getUrl(String url, Type type, String chat) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        if (type.equals(Type.REELS) || type.equals(Type.POST)) {
            String json;
            try {
                json = new PythonRunner().runner(url);
                JsonParser jsonParser = new JsonParser();
                JsonElement rootElement = null;
                try {
                    rootElement = jsonParser.parse(json);
                } catch (Exception e) {
                    System.out.println("Error json ->" + json);
                    new SendMessageToBot().sendMessage("Проблема с Saveinsta ☹️, свяжитесь с @Mr_Temurbek", chat);
                }

                JsonObject jsonObject = rootElement.getAsJsonObject();
                try {
                    String mess = jsonObject.get("mess").getAsString();
                    if (mess.contains("Video is private")) {
                        throw new MyException();
                    }
                } catch (NullPointerException ignored) {

                }
                return json;
            } catch (MyException e) {
                new SendMessageToBot().sendMessage(" ❌❌❌ Обработка не удалась ❌❌❌\n 🔒 Закрытый аккаунт 🔒", chat);
                throw new RuntimeException("zakritiy akkaunt");
            }
        } else {
            String fullUrl = "https://igram.world/api/ig/story" + "?" + "url" + "=" + url;

            HttpGet request = new HttpGet(fullUrl);


            try {
                HttpResponse response = httpClient.execute(request);
                if (response.getStatusLine().getStatusCode() == 500) {
                    throw new MyException();
                }
                HttpEntity entity1 = response.getEntity();
                String json = EntityUtils.toString(entity1);
                System.out.println("json response from Igram.World \n->" + json);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error occurred while fetching data from JSON";
            } catch (MyException e) {
                new SendMessageToBot().sendMessage("  ❌❌❌ Обработка не удалась ❌❌❌ \n 🔒 Видимо закрытый аккаунт 🔒 \n или \uD83E\uDEAB Сервер не работает \uD83E\uDEAB", chat);
                throw new RuntimeException("zakritiy akkaunt");
            }

        }
    }
}
