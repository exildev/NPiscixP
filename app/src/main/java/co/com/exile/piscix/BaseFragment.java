package co.com.exile.piscix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;

public class BaseFragment extends Fragment {
    private String url;

    @Override
    public void onResume() {
        super.onResume();

        if (getURL() == null) {
            startActivityForResult(new Intent(this.getContext(), UrlActivity.class), 1);
        } else {
            url = getURL();
        }

        Log.e("tales5", "url: " + url);
    }

    private String getURL() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UrlPref", Context.MODE_PRIVATE);
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
