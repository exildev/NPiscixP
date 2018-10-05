package co.com.exile.piscix;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.fiskur.simpleviewpager.ImageURLLoader;
import eu.fiskur.simpleviewpager.SimpleViewPager;

public class GalleryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getPhotos();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void getPhotos() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        int id = getIntent().getIntExtra("id", -1);
        String url = getIntent().getStringExtra("url") + id;
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                findViewById(R.id.loading).setVisibility(View.GONE);
                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    if (object_list.length() == 0){
                        noPhotos();
                        return;
                    }
                    String[] urlArray = new String[object_list.length()];
                    for (int i = 0; i < object_list.length(); i++) {
                        JSONObject campo = object_list.getJSONObject(i);
                        String url = campo.getString("url");
                        urlArray[i] = url;
                    }
                    setImageGallery(urlArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                findViewById(R.id.loading).setVisibility(View.GONE);
                final CardView container = (CardView) findViewById(R.id.error_container);
                container.setVisibility(View.VISIBLE);
                VolleySingleton.manageError(GalleryActivity.this, error, container, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        container.setVisibility(View.GONE);
                        getPhotos();
                    }
                });
                Log.e("Activities", error.toString());
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(reportesRequest);
    }

    void setImageGallery(String[] urlArray){
        String serviceUrl = getString(R.string.media);
        final String urlBase = getUrl(serviceUrl);
        SimpleViewPager simpleViewPager = (SimpleViewPager) findViewById(R.id.simple_view_pager);
        simpleViewPager.setVisibility(View.VISIBLE);
        simpleViewPager.setImageUrls(urlArray, new ImageURLLoader() {
            @Override
            public void loadImage(ImageView view, String url) {
                Log.i("url", url);
                Picasso.with(GalleryActivity.this)
                        .load(urlBase + url)
                        .placeholder(R.drawable.ic_photo)
                        .into(view);
            }
        });

        int indicatorColor = Color.parseColor("#ffffff");
        int selectedIndicatorColor = ContextCompat.getColor(this, R.color.colorPrimary);
        simpleViewPager.showIndicator(indicatorColor, selectedIndicatorColor);
    }
    void noPhotos(){
        findViewById(R.id.no_photos).setVisibility(View.VISIBLE);
    }
}
