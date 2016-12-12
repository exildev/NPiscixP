package co.com.exile.piscix;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;


public class VolleySingleton {
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    private CookieManager cookieManager;

    private VolleySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    void cancelAll(){
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                Log.d("DEBUG","request running: "+request.getUrl());
                return true;
            }
        });
    }

    String getCookie(String name, URI url){
        List<HttpCookie> cookies = cookieManager.getCookieStore().get(url);
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals(name)){
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void manageError(Context context, VolleyError error, ViewGroup container, View.OnClickListener retryListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View errorView = inflater.inflate(R.layout.no_network, container, false);
        container.addView(errorView);
        Button retry = (Button) errorView.findViewById(R.id.retry);
        if (error.networkResponse == null) {
            Log.i("retry", "hola " + retry.isClickable());
            retry.setOnClickListener(retryListener);
        } else {
            TextView errorTV = (TextView) errorView.findViewById(R.id.error_text);
            errorTV.setText(context.getString(R.string.server_error, error.networkResponse.statusCode));
            retry.setVisibility(View.GONE);
            Log.i("network", "" + error.networkResponse.statusCode);
        }
    }
}
