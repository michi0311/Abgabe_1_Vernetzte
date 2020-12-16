package Helpers;

import java.util.HashMap;
import java.util.Map;

/****************************
 * Created by Michael Marolt *
 *****************************/

public class MimeTypes {
    public static String mimeTypesHelper(String type) {
        Map<String, String> mimeMap = new HashMap<>();
        mimeMap.put("css","text/css");
        mimeMap.put("htm","text/html");
        mimeMap.put("html","text/html");
        mimeMap.put("xml","text/xml");
        mimeMap.put("txt","text/plain");
        mimeMap.put("asc","text/plain");
        mimeMap.put("gif","image/gif");
        mimeMap.put("jpg","image/jpeg");
        mimeMap.put("jpeg","image/jpeg");
        mimeMap.put("png","image/png");
        mimeMap.put("mp3","audio/mpeg");
        mimeMap.put("mp4","video/mp4");
        mimeMap.put("mov","video/quicktime");
        mimeMap.put("js","application/javascript");
        mimeMap.put("pdf","application/pdf");
        mimeMap.put("zip","application/octet-stream");

        return mimeMap.get(type);
    }
}
