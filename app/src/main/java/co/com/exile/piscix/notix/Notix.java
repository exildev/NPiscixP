package co.com.exile.piscix.notix;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

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

import co.com.exile.piscix.R;
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
    private AlarmListener alarmListener;

    private Notix(Context context) {
        getNotixURL(context);
    }

    public static Notix getInstance(Context context) {
        if (instance == null) {
            instance = new Notix(context);
        }

        return instance;
    }

    private void getNotixURL(final Context context) {
        String serviceUrl = context.getString(R.string.notix);
        String url = getURL(context, serviceUrl);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                initSocket(response, context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Activities", error.toString());
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private String getURL(final Context context, String serviceUrl) {
        return Uri.parse(getURL(context))
                .buildUpon()
                .appendEncodedPath(serviceUrl)
                .build()
                .toString();
    }

    private String getURL(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("UrlPref", Context.MODE_PRIVATE);
        return sharedPref.getString("url", null);
    }

    public void setNotixListener(onNotixListener notixListener) {
        this.notixListener = notixListener;
    }

    public void setAlarmListener(AlarmListener alarmListener) {
        this.alarmListener = alarmListener;
    }

    public boolean isConnected() {
        return mSocket.connected();
    }

    private void initSocket(String url, Context context) {
        messages = new ArrayList<>();
        try {
            Log.e("tales5", url);
            mSocket = IO.socket("http://" + url);
            mSocket.on("identify", onIdentify);
            mSocket.on("success-login", onSuccesLogin);
            mSocket.on("error-login", onErrorLogin);
            mSocket.on("notix", onNotix);
            mSocket.on("visited", onVisited);
            mSocket.on("alarm", onAlarm);
            mSocket.on("list-alarms", onShowAlarm);
            mSocket.connect();

            if (!hasUser()) {
                setUser(context);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    void setUser(Context context) {
        String serviceUrl = context.getString(R.string.islogin);
        String url = getURL(context, serviceUrl);
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

    private boolean hasUser() {
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

    public void addAlarm(String message, String hora, String time) {
        try {
            JSONObject alarm = new JSONObject();
            alarm.put("message", message);
            alarm.put("hora", hora);
            alarm.put("time", time);
            emitMessage("alarm", alarm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void visitMessages(ArrayList<String> messages) {
        JSONArray messages_id = new JSONArray(messages);
        Log.i("visit", messages_id.toString());
        try {
            JSONObject msg = new JSONObject();
            msg.put("webuser", username);
            msg.put("type", type);
            msg.put("messages_id", messages_id);
            emitMessage("visited", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("django_id", django_id);
            msg.put("usertype", "WEB");
            msg.put("webuser", username);
            msg.put("password", SOCKET_PASSWORD);
            msg.put("username", SOCKET_USERNAME);
            mSocket.emit("login", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessages() {
        Log.i("sendMessages", messages.size() + "");
        if (messages == null) {
            messages = new ArrayList<>();
            return;
        }
        for (Message message : messages) {
            Log.i("sendMessage", message.toString());
            try {
                JSONObject msg = message.getMessage();
                msg.put("django_id", django_id);
                msg.put("usertype", "WEB");
                msg.put("webuser", username);
                mSocket.emit(message.getEmit(), msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        messages = new ArrayList<>();
    }

    public void getAlarms() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("webuser", username);
            msg.put("usertype", type);
            emitMessage("show-alarm", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            Log.i("onidentify", message.toString());
            Log.i("onidentify", messages.size() + "");
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
            try {
                JSONObject message = (JSONObject) args[0];
                String id = message.getString("_id");
                JSONObject data = message.getJSONObject("data");
                data.put("_id", id);
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

    private Emitter.Listener onVisited = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("visited", "tiggered");
            try {
                JSONObject message = (JSONObject) args[0];
                notixListener.onVisited(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onShowAlarm = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("alarms", "triggered");
            final JSONArray alarms = (JSONArray) args[0];
            alarmListener.onShowAlarm(alarms);
        }
    };

    private Emitter.Listener onAlarm = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("alarm", "triggered");
            final JSONObject alarm = (JSONObject) args[0];
            alarmListener.onAlarm(alarm);
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
