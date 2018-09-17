package co.com.exile.piscix;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.com.exile.piscix.models.Mensaje;
import co.com.exile.piscix.notix.Notix;
import co.com.exile.piscix.notix.NotixFactory;
import co.com.exile.piscix.notix.onNotixListener;

public class ChatActivity extends AppCompatActivity implements onNotixListener {

    private RecyclerView infiniteListView;
    private ArrayList<Mensaje> itemList;


    private int reporte;
    private Notix notix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
                appBarLayout.setExpanded(true);
            }
        });

        final View text = findViewById(R.id.message_text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, ChatActivity.this);
                        infiniteListView.swapAdapter(chatAdapter, false);
                        infiniteListView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }, 400);
            }
        });

        text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, ChatActivity.this);
                        infiniteListView.swapAdapter(chatAdapter, false);
                        infiniteListView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }, 400);
            }
        });


        notix = NotixFactory.buildNotix(this);
        notix.setNotixListener(this);

        setReporte();

        setInfiniteList();

        visitMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notix.setNotixListener(this);
    }

    public void send(View view) {
        TextView messageTV = (TextView) findViewById(R.id.message_text);
        String message = messageTV.getText().toString();
        messageTV.setText("");
        if (message.equals("")) {
            return;
        }
        Log.i("message", message);
        Mensaje m = new Mensaje("", message, true, "", false);
        itemList.add(m);
        RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, ChatActivity.this);
        infiniteListView.swapAdapter(chatAdapter, false);
        infiniteListView.scrollToPosition(chatAdapter.getItemCount() - 1);
        send(m);
    }

    void send(final Mensaje mensaje) {
        String serviceUrl = getString(R.string.respuesta_form);
        String url = getString(R.string.url, serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mensaje.setStatus(true);
                        RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, ChatActivity.this);
                        infiniteListView.swapAdapter(chatAdapter, false);
                        infiniteListView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("login", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("mensaje", mensaje.getMensaje());
                params.put("reporte", String.valueOf(reporte));
                return params;
            }
        };
        loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
    }

    void setReporte() {
        reporte = getIntent().getIntExtra("reporte", -1);
        String reporte_title = getIntent().getStringExtra("reporte_title");
        String reporte_fecha = getIntent().getStringExtra("reporte_fecha");
        String reporte_cliente = getIntent().getStringExtra("reporte_cliente");

        TextView title = (TextView) findViewById(R.id.reporte_title);
        TextView fecha = (TextView) findViewById(R.id.reporte_fecha);
        TextView cliente = (TextView) findViewById(R.id.reporte_cliente);

        title.setText(reporte_title);
        fecha.setText(reporte_fecha);
        cliente.setText(reporte_cliente);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, R.color.colorTransparent));
        collapsingToolbarLayout.setTitle(reporte_title);

    }

    void setInfiniteList() {
        itemList = new ArrayList<>();
        infiniteListView = (RecyclerView) findViewById(R.id.content_chat);
        infiniteListView.setHasFixedSize(true);
        infiniteListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, this);
        infiniteListView.swapAdapter(chatAdapter, false);
        getMensajes();
    }

    void getMensajes() {
        String serviceUrl = getString(R.string.respuesta_list, reporte);
        String url = getString(R.string.url, serviceUrl);
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    for (int i = 0; i < object_list.length(); i++) {
                        JSONObject campo = object_list.getJSONObject(i);
                        String fecha = campo.getString("fecha");
                        String mensaje = campo.getString("mensaje");
                        String user = campo.getString("user");
                        boolean tu = campo.getBoolean("tu");
                        itemList.add(new Mensaje(fecha, mensaje, tu, user, true));
                    }
                    RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, ChatActivity.this);
                    infiniteListView.swapAdapter(chatAdapter, false);
                    infiniteListView.scrollToPosition(chatAdapter.getItemCount() - 1);
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

    private void visitMessages() {
        ArrayList<String> messages = new ArrayList<>();
        for (JSONObject notification : NotixFactory.notifications) {
            try {
                JSONObject data = notification.getJSONObject("data");
                String tipo = data.getString("tipo");
                if (tipo.equals("Respuesta")) {
                    int reporte_id = data.getInt("reporte_id");
                    if (reporte_id == reporte) {
                        String id = notification.getString("_id");
                        messages.add(id);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        notix.visitMessages(messages);
    }

    @Override
    public void onNotix(JSONObject data) {
        try {
            JSONObject d = data.getJSONObject("data");
            String tipo = d.getString("tipo");
            if (tipo.equals("Respuesta")) {
                int reporte_id = d.getInt("reporte_id");
                if (reporte_id == reporte) {
                    ArrayList<String> messages = new ArrayList<>();
                    String id = data.getString("_id");
                    messages.add(id);
                    notix.visitMessages(messages);
                    final String mensaje = d.getString("mensaje");
                    final String user = d.getString("usuario");
                    final String fecha = d.getString("fecha");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            itemList.add(new Mensaje(fecha, mensaje, false, user, true));
                            RecyclerView.Adapter chatAdapter = new ChatAdapter(itemList, ChatActivity.this);
                            infiniteListView.swapAdapter(chatAdapter, false);
                            infiniteListView.scrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                    });
                    return;
                }
            }
            NotixFactory.buildNotification(this, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVisited(JSONObject data) {
        Log.i("notifications", NotixFactory.notifications.size() + "");
        try {
            JSONArray messages_id = data.getJSONArray("messages_id");
            for (int i = 0; i < messages_id.length(); i++) {
                String id = messages_id.getString(i);
                for (JSONObject notification : NotixFactory.notifications) {
                    String _id = notification.getString("_id");
                    if (id.equals(_id)) {
                        NotixFactory.notifications.remove(notification);
                        break;
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("notifications", NotixFactory.notifications.size() + "");
    }

    static class ViewHolder {
        TextView mensaje;
        TextView user;
        CardView bubble;
    }
}
