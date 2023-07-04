package temurbeks.experiment.insta;

import temurbeks.experiment.utils.TelegramSenderVideo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReelsDownloader {
    public Boolean reels(ArrayList urls, String chatId) throws IOException, InterruptedException {
        TelegramSenderVideo telegramSenderVideo = new TelegramSenderVideo();
        Boolean result = false;
        try {
            if (urls.size() > 5) {

                List<String> firstPart = urls.subList(0, Math.min(urls.size(), 5));
                List<String> secondPart = urls.subList(Math.min(urls.size(), 5), urls.size());
                Integer r1 = telegramSenderVideo.sendVideo(new ArrayList(firstPart), chatId);
                Thread.sleep(6 * 1000);
                Integer r2 = telegramSenderVideo.sendVideo(new ArrayList(secondPart), chatId);
                if (r1.equals(200) & r2.equals(200)) {
                    result= true;
                    return true;
                }
            } else {
                if (telegramSenderVideo.sendVideo(urls, chatId).equals(200)){
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
