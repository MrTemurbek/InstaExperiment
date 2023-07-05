package temurbeks.experiment.insta;

import temurbeks.experiment.utils.TelegramSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstagramPhotoAndVideoDownloader {
    public Boolean download(ArrayList urls, String chatId){
        TelegramSender telegramSender = new TelegramSender();
        Boolean result = false;

        try {
            if (urls.size() > 5) {

                List<String> firstPart = urls.subList(0, Math.min(urls.size(), 5));
                List<String> secondPart = urls.subList(Math.min(urls.size(), 5), urls.size());
                Integer r1 = telegramSender.sendMedia(new ArrayList(firstPart), chatId);
                Thread.sleep(6 * 1000);
                Integer r2 = telegramSender.sendMedia(new ArrayList(secondPart), chatId);
                if (r1.equals(200) & r2.equals(200)) {
                    result= true;
                    return true;
                }
            } else {
                if (telegramSender.sendMedia(urls, chatId).equals(200)){
                    result= true;
                    return true;
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
            if (result.equals(true)){
                return true;
            }
            return false;
        }
        return result;
    }
}
