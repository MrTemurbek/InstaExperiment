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
            int index = url.indexOf("?");
            if (index != -1) {
                if( url.contains("/reels/")){
                    url = url.replaceFirst("/reels/", "/reel/");

                }
                url= url.substring(0, index) + "?__a=1&__d=dis";
            } else {
                url= url+"?__a=1&__d=dis";
            }
            HttpGet request = new HttpGet(url);
            System.out.println("url -> "+ url);
            try {
                HttpResponse response = httpClient.execute(request);

                System.out.println("RESPONSE -> " + response);
                HttpEntity entity1 = response.getEntity();
                String json = EntityUtils.toString(entity1);
                JsonParser jsonParser =new Gson().fromJson(json, JsonParser.class);
                JsonElement rootElement = null;
                try {
                    rootElement = jsonParser.parse(json);
                } catch (Exception e) {
                    new SendMessageToBot().sendMessage("Error with Servers ☹️, contact to @Mr_Temurbek \n \n" +
                            " Проблема с Сервером ☹️, свяжитесь с @Mr_Temurbek", chat);
                }

                JsonObject jsonObject = rootElement.getAsJsonObject();
                try {
                    Boolean mess = jsonObject.get("require_login").getAsBoolean();
                    if (mess) {
                        throw new MyException();
                    }
                } catch (NullPointerException ignored) {

                }
                return json;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error occurred while fetching data from JSON";
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
                new SendMessageToBot().sendMessage("  ❌❌❌ Обработка не удалась ❌❌❌ \n 🔒 Закрытый аккаунт 🔒", chat);
                throw new RuntimeException("zakritiy akkaunt");
            }

        }
    }
}
