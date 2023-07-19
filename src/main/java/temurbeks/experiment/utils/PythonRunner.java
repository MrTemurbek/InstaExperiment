package temurbeks.experiment.utils;

import java.io.*;

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
            process.waitFor();

            // Используем полученный вывод в Java
            response = output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }}
