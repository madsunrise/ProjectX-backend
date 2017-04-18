package com.projectx.utils;

import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Created by ivan on 18.04.17.
 */
public class PhotoService {

    // Сохранить полученные фото в файлы и вернуть массив с именами файлов
    public static synchronized List<String> savePhotosToFiles(List<String> photos) throws IOException {
        List<String> fileNames = new ArrayList<>();
        for (String base64: photos) {
            String decoded = UriUtils.decode(base64, "UTF-8");
            byte[] bytes = Base64.getDecoder().decode(decoded);
            String fileName = System.currentTimeMillis() + ".jpeg";

            String path = "photos/v1";
            File photoDir = new File(path);

            if (!photoDir.exists()) {
                if (!photoDir.mkdirs()) {
                    throw new IOException("Can not create /photos dir!");
                }
            }

            File file = new File(photoDir, fileName);

            if (!file.createNewFile()) {
                throw new IOException("Can not create a file!");
            };
            OutputStream stream = new FileOutputStream(file);
            stream.write(bytes);
            stream.close();
            fileNames.add(fileName);
        }
        return fileNames;
    }
}
