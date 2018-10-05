package co.com.exile.piscix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class BaseActivity extends AppCompatActivity {

    private String url;

    @Override
    protected void onResume() {
        super.onResume();

        if (getURL() == null) {
            startActivityForResult(new Intent(this, UrlActivity.class), 1);
        } else {
            url = getURL();
        }

        Log.e("tales5", "url: " + url);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        if (getURL() == null) {
            startActivityForResult(new Intent(this, UrlActivity.class), 1);
        } else {
            url = getURL();
        }

        Log.e("tales5", "url: " + url);
    }

    private String getURL() {
        SharedPreferences sharedPref = getSharedPreferences("UrlPref", Context.MODE_PRIVATE);
        return sharedPref.getString("url", null);
    }

    protected String getUrl(String serviceUrl) {
        return Uri.parse(getURL())
                .buildUpon()
                .appendEncodedPath(serviceUrl)
                .build()
                .toString();
    }
}
