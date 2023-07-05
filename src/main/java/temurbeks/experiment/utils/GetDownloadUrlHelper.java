package temurbeks.experiment.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetDownloadUrlHelper {
    public String getUrl(String url, Type type){
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
                return json;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error occurred while fetching data from JSON";
            }

        }
        else {
            HttpClient httpClient = HttpClientBuilder.create().build();
            String fullUrl = "https://igram.world/api/ig/story" + "?" + "url" + "=" + url;

            HttpGet request = new HttpGet(fullUrl);


            try {
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity1 = response.getEntity();
                String json = EntityUtils.toString(entity1);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error occurred while fetching data from JSON";
            }

        }
    }
}
