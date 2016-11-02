package co.com.exile.piscix;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.com.exile.piscix.models.Mensaje;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView infiniteListView;
    private ArrayList<Mensaje> itemList;

    private int reporte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reporte = getIntent().getIntExtra("reporte", -1);

        setInfiniteList();
    }

    void setInfiniteList() {
        itemList = new ArrayList<>();
        infiniteListView = (RecyclerView) findViewById(R.id.content_chat);
        infiniteListView.setHasFixedSize(true);
        infiniteListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, this);
        infiniteListView.swapAdapter(chatAdapter, false);
        getReportes();
    }

    void getReportes() {
        //infiniteListView.startLoading();
        String url = "http://104.236.33.228:8050/reportes/respuesta/list/?reporte=" + reporte;
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //infiniteListView.stopLoading();
                    JSONArray object_list = response.getJSONArray("object_list");
                    for (int i = 0; i < object_list.length(); i++) {
                        JSONObject campo = object_list.getJSONObject(i);
                        String fecha = campo.getString("fecha");
                        String mensaje = campo.getString("mensaje");
                        String user = campo.getString("user");
                        boolean tu = campo.getBoolean("tu");
                        itemList.add(new Mensaje(fecha, mensaje, tu, user));
                    }
                    RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, ChatActivity.this);
                    infiniteListView.swapAdapter(chatAdapter, false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Activities", error.toString());
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(reportesRequest);
    }

    static class ViewHolder {
        TextView mensaje;
        TextView user;
        CardView bubble;
    }

}
