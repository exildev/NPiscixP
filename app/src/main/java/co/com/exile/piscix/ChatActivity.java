package co.com.exile.piscix;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.com.exile.piscix.models.Mensaje;

public class ChatActivity extends AppCompatActivity {

    private InfiniteListView infiniteListView;
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
        infiniteListView = (InfiniteListView) findViewById(R.id.content_chat);

        itemList = new ArrayList<>();
        InfiniteListAdapter adapter = new InfiniteListAdapter<Mensaje>(this, R.layout.chat_bubble, itemList) {
            @Override
            public void onNewLoadRequired() {
                getReportes();
            }

            @Override
            public void onRefresh() {
                infiniteListView.clearList();
                getReportes();
            }

            @Override
            public void onItemClick(int i) {
            }

            @Override
            public void onItemLongClick(int i) {
                Toast.makeText(getContext(), "long click", Toast.LENGTH_LONG).show();
            }

            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.chat_bubble, parent, false);
                    holder = new ViewHolder();
                    holder.user = (TextView) convertView.findViewById(R.id.user);
                    holder.mensaje = (TextView) convertView.findViewById(R.id.message);
                    holder.bubble = (CardView) convertView.findViewById(R.id.bubble);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final Mensaje mensaje = itemList.get(position);
                if (mensaje != null) {
                    float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                    final int max_margin = (int) (72 * scale + 0.5f);
                    final int min_margin = (int) (16 * scale + 0.5f);
                    int top_margin = (int) (16 * scale + 0.5f);
                    int bottom_margin = 0;
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    if (position > 0 && itemList.get(position - 1).getUser().equals(mensaje.getUser())) {
                        top_margin = (int) (2 * scale + 0.5f);
                    }

                    if ((position + 1) >= itemList.size()) {
                        bottom_margin = (int) (16 * scale + 0.5f);
                    }

                    holder.mensaje.setText(mensaje.getMensaje());
                    if (!mensaje.isTu()) {
                        holder.mensaje.setTextColor(ContextCompat.getColor(this.getContext(), R.color.white));
                        holder.user.setText(mensaje.getUser());
                        if (position > 0 && itemList.get(position - 1).getUser().equals(mensaje.getUser())) {
                            holder.user.setVisibility(View.GONE);
                        } else {
                            holder.user.setVisibility(View.VISIBLE);
                        }
                        holder.bubble.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
                        layoutParams.setMargins(min_margin, top_margin, max_margin, bottom_margin);
                    } else {
                        holder.user.setVisibility(View.GONE);
                        holder.bubble.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.white));
                        holder.mensaje.setTextColor(ContextCompat.getColor(this.getContext(), R.color.grey));
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        layoutParams.setMargins(max_margin, top_margin, min_margin, bottom_margin);
                    }
                    holder.bubble.setLayoutParams(layoutParams);
                }

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
        getReportes();
    }

    void getReportes() {
        infiniteListView.startLoading();
        String url = "http://104.236.33.228:8050/reportes/respuesta/list/?reporte=" + reporte;
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    infiniteListView.stopLoading();
                    JSONArray object_list = response.getJSONArray("object_list");
                    for (int i = 0; i < object_list.length(); i++) {
                        JSONObject campo = object_list.getJSONObject(i);
                        String fecha = campo.getString("fecha");
                        String mensaje = campo.getString("mensaje");
                        String user = campo.getString("user");
                        boolean tu = campo.getBoolean("tu");
                        infiniteListView.addNewItem(new Mensaje(fecha, mensaje, tu, user));
                    }
                    infiniteListView.hasMore(false);
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
