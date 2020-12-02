package cn.wsjiu.easychange.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadImageCallable extends AbstractCallable {
    private final String METHOD = "GET";

    public LoadImageCallable(String url) {
        super();
        this.url = url;
    }

    @Override
    public Bitmap call() throws Exception {
        URL url = new URL(this.url);
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        httpURLConnection.setRequestMethod(METHOD);
        InputStream in = httpURLConnection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        return bitmap;
    }
}
