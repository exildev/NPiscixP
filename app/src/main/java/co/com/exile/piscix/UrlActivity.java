package co.com.exile.piscix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;

public class UrlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button goBtn = (Button) findViewById(R.id.goBtn);
        final TextInputEditText urlTV = (TextInputEditText) findViewById(R.id.urlTV);
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlTV.getText().toString();
                if (!url.isEmpty() && URLUtil.isValidUrl(url)) {
                    saveURL(url);
                } else  {
                    Snackbar.make(v, "Debe ingresarse un URL valida", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        urlTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String url = urlTV.getText().toString();
                if (!url.isEmpty()) {
                    saveURL(url);
                    return true;
                }
                return false;
            }
        });
    }

    private void saveURL(String url) {
        SharedPreferences sharedPref = getSharedPreferences("UrlPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("url", url);
        editor.apply();
        finish();
    }
}
