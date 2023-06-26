package temurbeks.experiment.service.serviceImpl;

import jakarta.enterprise.context.ApplicationScoped;


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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import temurbeks.experiment.client.IgDownloaderRest;
import temurbeks.experiment.entity.TemporaryResponse;
import temurbeks.experiment.service.InstagramService;
import temurbeks.experiment.service.TelegramService;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
public class InstagramServiceImpl implements InstagramService {
    @Inject
    @RestClient
    IgDownloaderRest igDownloaderRest;

    @Inject
    TelegramService telegramService;

    @Override
    public String getLinkVideo(String url) {
        int index = url.indexOf("?");
        TemporaryResponse temporaryResponse = new TemporaryResponse();
        if (index != -1) {
            url = url.substring(0, index) + "?__a=1&__d=dis";
        }
        else {
            url =url+"?__a=1&__d=dis";
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
                TemporaryResponse responseData = new ObjectMapper().readValue(json, TemporaryResponse.class);
                temporaryResponse= responseData;
            } catch (IOException e) {
                e.printStackTrace();
                return "Error occurred while fetching data from JSONPlaceholder";
            }
        }
        String responseUrl = extractUrlFromData(temporaryResponse.getData());
        try {
            telegramService.sendVideoToBotFromUrl(responseUrl);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("ERROR TELEGRAM");
        }
        return "SUCCESS";
    }

    private static String extractUrlFromData(String data) {
        String startToken = "href=\"";
        String endToken = "\"";
        int startIndex = data.indexOf(startToken);
        int endIndex = data.indexOf(endToken, startIndex + startToken.length());

        return data.substring(startIndex + startToken.length(), endIndex);
    }
}
