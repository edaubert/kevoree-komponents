package org.kevoree.komponents.helpers;

import java.io.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 11/03/14
 * Time: 16:34
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class Reader {

    public static String copyFileFromStream( InputStream inputStream , String path, String targetName,boolean replace) throws IOException {

        if (inputStream != null) {
            File copy = new File(path + File.separator + targetName);
            copy.mkdirs();
            if(replace)
            {
                if(copy.exists()){
                    if(!copy.delete()){
                        throw new IOException("delete file "+copy.getPath());
                    }
                    if(!copy.createNewFile()){
                        throw new IOException("createNewFile file "+copy.getPath());
                    }
                }
            }
            OutputStream outputStream = new FileOutputStream(copy);
            byte[] bytes = new byte[1024];
            int length = inputStream.read(bytes);

            while (length > -1) {
                outputStream.write(bytes, 0, length);
                length = inputStream.read(bytes);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return copy.getAbsolutePath();
        }
        return null;
    }
}
