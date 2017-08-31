package top.trumeet.mipushframework.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Trumeet on 2017/8/31.
 * Network utils
 */

public class Network implements INetwork {
    public static INetwork getDefault () {
        return new Network();
    }

    @Override
    public String execute (String baseUrl) throws IOException {
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection)
                url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDefaultUseCaches(false);
        connection.connect();
        InputStream stream = connection.getInputStream();
        byte[] bytes = null;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        byte[] buffer = new byte[1024 * 8];
        int length;
        try {
            while ((length = bufferedInputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, length);
            }
            bufferedOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connection.disconnect();
        return new String(bytes, "UTF-8");
    }
}
