package temurbeks.experiment.utils;

import java.io.File;

public class DeleteAllInFolder {
    final static String folderPath = "video/"; // Замените на путь к вашей папке

    public void deleteInFolder() {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                        System.out.println("Удален файл: " + file.getName());
                    }
                }
            }
        } else {
            System.out.println("Папка не существует или это не директория.");
        }
    }
}


