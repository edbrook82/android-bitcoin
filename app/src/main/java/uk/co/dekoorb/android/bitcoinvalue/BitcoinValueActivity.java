package uk.co.dekoorb.android.bitcoinvalue;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class BitcoinValueActivity extends AppCompatActivity
        implements FetchJsonFromUrlTask.JsonDataReceivedListener {

    public static final String BPI_API = "https://api.coindesk.com/v1/bpi/currentprice.json";
    public static final String COINDESK_URL = "https://www.coindesk.com/price/";

    public static final String JSON_BPI_KEY = "bpi";
    public static final String JSON_GBP_KEY = "GBP";
    public static final String JSON_RATE_KEY = "rate_float";

    private FetchJsonFromUrlTask mGetBpiTask;
    private TextView mBitcoinPrice;
    private ProgressBar mPbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitcoin_value);

        mBitcoinPrice = (TextView) findViewById(R.id.tv_bitcoin_price);
        mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        TextView coinDeskText = (TextView) findViewById(R.id.tv_copyright);
        coinDeskText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(COINDESK_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        Button updateButton = (Button) findViewById(R.id.btn_update);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBPI();
            }
        });
    }

    private void updateBPI() {
        if (!isOkToRequest()) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mGetBpiTask == null || mGetBpiTask.getStatus().compareTo(AsyncTask.Status.FINISHED) == 0) {
            mGetBpiTask = new FetchJsonFromUrlTask(this);
            try {
                mBitcoinPrice.setVisibility(View.GONE);
                mPbLoading.setVisibility(View.VISIBLE);
                mGetBpiTask.execute(new URL(BPI_API));
            } catch (MalformedURLException e) {
                //
            }
        }
    }

    private boolean isOkToRequest() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private String getRateFromJson(JSONObject jsonObject, String currency) throws JSONException {
        JSONObject jsonBpis = jsonObject.getJSONObject(JSON_BPI_KEY);
        JSONObject jsonGbp = jsonBpis.getJSONObject(currency);
        float rate_float = Float.parseFloat(jsonGbp.getString(JSON_RATE_KEY));
        return NumberFormat.getCurrencyInstance(Locale.UK).format(rate_float);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateBPI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGetBpiTask != null) {
            mGetBpiTask.cancel(true);
        }
    }

    @Override
    public void onJsonData(JSONObject jsonObject) {
        if (jsonObject == null) {
            notifyUpdateFailed();
            return;
        }
        try {
            String rate = getRateFromJson(jsonObject, JSON_GBP_KEY);
            mBitcoinPrice.setText(rate);
            mPbLoading.setVisibility(View.GONE);
            mBitcoinPrice.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            notifyUpdateFailed();
        }
    }

    private void notifyUpdateFailed() {
        mBitcoinPrice.setText(R.string.bpi_default_value);
        mBitcoinPrice.setVisibility(View.VISIBLE);
        mPbLoading.setVisibility(View.GONE);
        String message = getString(R.string.update_failed);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
