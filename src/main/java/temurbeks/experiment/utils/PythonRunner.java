package temurbeks.experiment.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import temurbeks.experiment.entity.Type;
import temurbeks.experiment.exception.MyException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PythonRunner {
    public String runner(String instaUrl) {
        String url ="https://saveig.app/api/ajaxSearch";
        String response="";
        try {
            // Создаем объект ProcessBuilder с командой Python и аргументами
            ProcessBuilder pb = new ProcessBuilder("python3", "python/request.py", url, instaUrl);

            // Запускаем процесс
            Process process = pb.start();

            // Получаем вывод программы Python
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Дожидаемся окончания выполнения процесса
            int exitCode = process.waitFor();

            // Используем полученный вывод в Java
            response = output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }}
