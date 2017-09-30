package uk.co.dekoorb.android.bitcoinvalue;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by edbrook on 30/09/2017.
 */

public class FetchJsonFromUrlTask extends AsyncTask<URL, Void, JSONObject> {

    private static final int MAX_CONNECT_TIMEOUT = 3000;
    private static final int MAX_READ_TIMEOUT = 3000;
    private static final int MAX_RESPONSE_SIZE = 8192;

    private JsonDataReceivedListener mListener;

    public interface JsonDataReceivedListener {
        void onJsonData(JSONObject jsonObject);
    }

    public FetchJsonFromUrlTask(JsonDataReceivedListener listener) {
        mListener = listener;
    }

    @Override
    protected JSONObject doInBackground(URL... urls) {
        if (urls.length == 0) {
            return null;
        }
        URL apiUrl = urls[0];
        JSONObject jsonObject = null;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) apiUrl.openConnection();
            connection.setConnectTimeout(MAX_CONNECT_TIMEOUT);
            connection.setReadTimeout(MAX_READ_TIMEOUT);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = readResponse(connection, MAX_RESPONSE_SIZE);
                jsonObject = new JSONObject(jsonString);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        mListener.onJsonData(jsonObject);
    }

    @NonNull
    private String readResponse(URLConnection connection, int maxReadSize) throws IOException {
        int readSize;
        char[] rawBuffer = new char[maxReadSize];
        StringBuilder sb = new StringBuilder();
        InputStream stream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        while ((readSize = reader.read(rawBuffer)) != -1 && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            sb.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return sb.toString();
    }
}
