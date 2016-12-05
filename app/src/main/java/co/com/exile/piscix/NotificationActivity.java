package co.com.exile.piscix;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.piscix.notix.Notix;
import co.com.exile.piscix.notix.NotixFactory;
import co.com.exile.piscix.notix.onNotixListener;

public class NotificationActivity extends AppCompatActivity implements onNotixListener {

    private InfiniteListView infiniteListView;
    Notix notix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getIntent().getBooleanExtra("notification", false)) {
                    startActivity(new Intent(NotificationActivity.this, HomeActivity.class));
                }
                finish();
            }
        });

        notix = NotixFactory.buildNotix(this);
        notix.setNotixListener(this);

        setInfiniteList();
    }

    synchronized void setInfiniteList() {
        infiniteListView = (InfiniteListView) findViewById(R.id.content_notification);

        final InfiniteListAdapter adapter = new InfiniteListAdapter<JSONObject>(this, R.layout.notification, NotixFactory.notifications) {
            @Override
            public void onNewLoadRequired() {
            }

            @Override
            public void onRefresh() {
                infiniteListView.clearList();
                notix.getMessages();

            }

            @Override
            public void onItemClick(int i) {
                JSONObject notification = NotixFactory.notifications.get(i);
                Log.i("notif", notification.toString());
                try {
                    JSONObject data = notification.getJSONObject("data");
                    String tipo = data.getString("tipo");
                    switch (tipo) {
                        case "Actividad":
                            Log.i("notif", "Actividad");
                            break;
                        case "Respuesta":
                            Log.i("notif", "Respuesta");
                            visitRespuesta(data);
                            break;
                        case "Solucion":
                            Log.i("notif", "Solución de Reporte");
                            visitSolucion();
                            break;
                        case "Asignacion":
                            Log.i("notif", "Asignación");
                            visitAsignacion();
                            break;
                        case "Reporte informativo":
                            Log.i("notif", "Reporte informativo");
                            visitInformativo();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onItemLongClick(int i) {
            }

            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.notification, parent, false);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.content = (TextView) convertView.findViewById(R.id.content);
                    holder.icon_card = (CardView) convertView.findViewById(R.id.icon_card);
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final JSONObject reporte = NotixFactory.notifications.get(position);
                if (reporte != null) {
                    try {
                        String html = reporte.getString("html");
                        JSONObject data = reporte.getJSONObject("data");
                        String tipo = data.getString("tipo");
                        String title = "";
                        int icon_res = -1;
                        switch (tipo) {
                            case "Actividad":
                                title = "Actividad";
                                icon_res = R.drawable.ic_event_24dp;
                                break;
                            case "Respuesta":
                                title = "Respuesta";
                                icon_res = R.drawable.ic_forum_24dp;
                                break;
                            case "Recordatorio":
                                title = "Recordatorio";
                                icon_res = R.drawable.ic_alarm_24dp;
                                break;
                            case "Solucion":
                                title = "Solución de Reporte";
                                icon_res = R.drawable.ic_buid_24dp;
                                break;
                            case "Asignacion":
                                title = "Asignación";
                                icon_res = R.drawable.ic_pool_24dp;
                                break;
                            case "Reporte informativo":
                                title = "Reporte informativo";
                                icon_res = R.drawable.ic_info_24dp;
                                break;
                        }

                        holder.content.setText(html);
                        holder.title.setText(title);
                        holder.icon.setImageResource(icon_res);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
    }

    private void visitAsignacion() {
        Intent intent = new Intent(this, RutaActivity.class);
        startActivity(intent);
        finish();
    }

    private void visitSolucion() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("action", "Solucion");
        startActivity(intent);
        finish();
    }

    private void visitInformativo() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("action", "informativo");
        startActivity(intent);
        finish();
    }

    private void visitRespuesta(JSONObject notification) {
        try {
            int reporte_id = notification.getInt("reporte_id");
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("reporte", reporte_id);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("notification", false)) {
            startActivity(new Intent(NotificationActivity.this, HomeActivity.class));
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notix.setNotixListener(this);
        setInfiniteList();
    }

    @Override
    public void onNotix(final JSONObject data) {
        Log.i("notix", data.toString());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infiniteListView.addNewItem(data);
                infiniteListView.stopLoading();
            }
        });
    }

    @Override
    public void onVisited(JSONObject data) {

    }

    static class ViewHolder {
        TextView title;
        TextView content;
        CardView icon_card;
        ImageView icon;
    }
}
