package co.com.exile.piscix.notix;


import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import co.com.exile.piscix.VolleySingleton;
import co.com.exile.piscix.models.Message;


public class Notix {

    private static final String SOCKET_USERNAME = "user1";
    private static final String SOCKET_PASSWORD = "123456";

    private Socket mSocket;
    private ArrayList<Message> messages;
    private String django_id;
    private String username;
    private String type;
    private static Notix instance;
    private onNotixListener notixListener;

    private Notix() {
        initSocket();
    }

    public static Notix getInstance() {
        if (instance == null) {
            instance = new Notix();
        }

        return instance;
    }

    public void setNotixListener(onNotixListener notixListener) {
        this.notixListener = notixListener;
    }

    public boolean isConnected() {
        return mSocket.connected();
    }

    private void initSocket() {
        Log.i("hola", "hola");
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
    }

    void setUser(Context context) {
        String url = "http://104.236.33.228:8050/usuarios/is/login/";
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("is_login", response.toString());
                try {
                    django_id = response.getString("session");
                    username = response.getString("username");
                    type = response.getString("type");
                    getMessages();
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
        VolleySingleton.getInstance(context).addToRequestQueue(reportesRequest);
    }

    boolean hasUser() {
        return !(username == null || type == null || django_id == null);
    }

    public void getMessages() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("webuser", username);
            msg.put("type", type);
            emitMessage("messages", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            Log.i("notix", "identify " + mSocket.connected());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onIdentify = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject message = (JSONObject) args[0];
            if (!message.has("ID")) {
                login();
            } else {
                sendMessages();
            }
        }
    };

    private Emitter.Listener onNotix = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("notix", "Triggered");
            try {
                JSONObject message = (JSONObject) args[0];
                JSONObject data = message.getJSONObject("data");
                if (data.has("data")) {
                    if (notixListener != null) {
                        notixListener.onNotix(data);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onSuccesLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            sendMessages();
        }
    };

    private Emitter.Listener onErrorLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("Error", "Hubo un error en el servidor");
        }
    };
}
