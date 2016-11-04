package co.com.exile.piscix;

import android.os.Bundle;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.com.exile.piscix.models.Mensaje;
import co.com.exile.piscix.models.Message;

public class ChatActivity extends AppCompatActivity {

    public static final String SOCKET_USERNAME = "user1";
    public static final String SOCKET_PASSWORD = "123456";

    private RecyclerView infiniteListView;
    private ArrayList<Mensaje> itemList;

    private Socket mSocket;
    private ArrayList<Message> messages;
    private String django_id;
    private String username;
    private String type;

    private int reporte;

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

        initSocket();

        setReporte();

        setInfiniteList();
    }

    void initSocket() {
        messages = new ArrayList<>();
        try {
            mSocket = IO.socket("http://104.236.33.228:1196");
            mSocket.on("identify", onIdentify);
            mSocket.on("success-login", onSuccesLogin);
            mSocket.on("error-login", onErrorLogin);
            mSocket.on("notix", onNotix);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String url = "http://104.236.33.228:8050/usuarios/is/login/";
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    django_id = response.getString("session");
                    username = response.getString("username");
                    type = response.getString("type");

                    try {
                        JSONObject msg = new JSONObject();
                        msg.put("webuser", username);
                        msg.put("type", type);
                        emitMessage("messages", msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

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
        reportesRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(reportesRequest);
    }

    @Override
    protected void onDestroy() {
        mSocket.disconnect()
                .close();
        super.onDestroy();
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
        String url = "http://104.236.33.228:8050/reportes/respuesta/form/";
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

    static class ViewHolder {
        TextView mensaje;
        TextView user;
        CardView bubble;
    }

    private void login() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("django_id", django_id);
            msg.put("usertype", "WEB");
            msg.put("password", SOCKET_PASSWORD);
            msg.put("username", SOCKET_USERNAME);
            mSocket.emit("login", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
            return;
        }
        for (Message message : messages) {
            try {
                JSONObject msg = message.getMessage();
                msg.put("django_id", django_id);
                msg.put("usertype", "WEB");
                mSocket.emit(message.getEmit(), msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        messages = new ArrayList<>();
    }

    private void emitMessage(String emit, JSONObject message) {
        messages.add(new Message(emit, message));
        try {
            JSONObject msg = new JSONObject();
            msg.put("django_id", django_id);
            msg.put("usertype", "WEB");
            mSocket.emit("identify", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onIdentify = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!message.has("ID")) {
                        login();
                    } else {
                        sendMessages();
                    }
                }
            });
        }
    };

    private Emitter.Listener onNotix = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject message = (JSONObject) args[0];
                JSONObject data = message.getJSONObject("data");
                if (data.has("data")) {
                    data = data.getJSONObject("data");
                    String tipo = data.getString("tipo");
                    Log.i("notix", tipo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onSuccesLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendMessages();
                }
            });
        }
    };

    private Emitter.Listener onErrorLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChatActivity.this, "Hubo un error en el servidor", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

}
