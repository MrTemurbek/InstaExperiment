package temurbeks.experiment.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import temurbeks.experiment.entity.TemporaryResponse;
import temurbeks.experiment.entity.Type;
import temurbeks.experiment.exception.MyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetDownloadUrlHelper {
    public String getUrl(String url, Type type, String chat) throws IOException, InterruptedException {
        if (type.equals(Type.REELS) || type.equals(Type.POST)){
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost("https://igdownloader.app/api/ajaxSearch");

            // Создаем список пар "имя"-"значение" для формы данных
            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("q", url));
            formParams.add(new BasicNameValuePair("t", "media"));

            try {
                // Создаем объект UrlEncodedFormEntity и устанавливаем его в качестве сущности запроса
                HttpEntity entity = new UrlEncodedFormEntity(formParams);
                request.setEntity(entity);

                HttpResponse response = httpClient.execute(request);
                HttpEntity entity1 = response.getEntity();
                String json = EntityUtils.toString(entity1);
                Gson gson = new Gson();
                JsonParser jsonParser = new JsonParser();
                JsonElement rootElement = jsonParser.parse(json);

                JsonObject jsonObject = rootElement.getAsJsonObject();
                try {
                    String mess = jsonObject.get("mess").getAsString();
                    if (mess.contains("Video is private")){
                        throw new MyException();
                    }
                }catch (NullPointerException ignored){

                }
                return json;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error occurred while fetching data from JSON";
            }catch (MyException e) {
                SendMessageToBot sendMessageToBot = new SendMessageToBot();
                sendMessageToBot.sendMessage(" ❌❌❌ Обработка не удалась ❌❌❌\n 🔒 Закрытый аккаунт 🔒", chat);
                throw new RuntimeException("zakritiy akkaunt");
            }

        }
        else {
            HttpClient httpClient = HttpClientBuilder.create().build();
            String fullUrl = "https://igram.world/api/ig/story" + "?" + "url" + "=" + url;

            HttpGet request = new HttpGet(fullUrl);


            try {
                HttpResponse response = httpClient.execute(request);
                if (response.getStatusLine().getStatusCode()==500){
                    throw new MyException();
                }
                HttpEntity entity1 = response.getEntity();
                String json = EntityUtils.toString(entity1);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error occurred while fetching data from JSON";
            }catch (MyException e){
                SendMessageToBot sendMessageToBot = new SendMessageToBot();
                sendMessageToBot.sendMessage("  ❌❌❌ Обработка не удалась ❌❌❌ \n 🔒 Видимо закрытый аккаунт 🔒 \n или \uD83E\uDEAB Сервер не работает \uD83E\uDEAB", chat);
                throw new RuntimeException("zakritiy akkaunt");
            }

        }
    }
}
