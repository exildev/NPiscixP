package co.com.exile.piscix;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import co.com.exile.piscix.notix.AlarmListener;
import co.com.exile.piscix.notix.Notix;
import co.com.exile.piscix.notix.NotixFactory;

public class AlarmActivity extends AppCompatActivity implements AlarmListener {

    private InfiniteListView<JSONObject> infiniteListView;
    private ArrayList<JSONObject> alarms;
    private Notix notix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        notix = NotixFactory.buildNotix(this);
        notix.setAlarmListener(this);
        alarms = new ArrayList<>();
        setInfiniteList();
    }

    public void add(View view) {
        new MaterialDialog.Builder(this)
                .title("Nuevo recordatorio")
                .customView(R.layout.alarm_form, true)
                .positiveText("Guardar")
                .negativeText("Cerrar")
                .negativeColor(ContextCompat.getColor(this, R.color.colorReport))
                .neutralColor(ContextCompat.getColor(this, R.color.colorReportAccent))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        setHour(dialog);
                    }
                })
                .show();
    }

    private void setHour(MaterialDialog dialog) {
        View view = dialog.getCustomView();
        final String nombre = ((TextView) view.findViewById(R.id.nombre)).getText().toString();

        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog picker = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        Calendar now = Calendar.getInstance();
                        Calendar hora = Calendar.getInstance();

                        if (now.get(Calendar.HOUR_OF_DAY) > hourOfDay || (now.get(Calendar.HOUR_OF_DAY) == hourOfDay && now.get(Calendar.MINUTE) > minute)) {
                            hora.add(Calendar.DATE, 1);
                        }

                        hora.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        hora.set(Calendar.MINUTE, minute);
                        long dif = hora.getTimeInMillis() - now.getTimeInMillis();
                        SimpleDateFormat formater = new SimpleDateFormat("hh:mm aa", Locale.US);
                        String s = formater.format(hora.getTime());
                        notix.addAlarm(nombre, s, dif + "");
                        infiniteListView.startLoading();
                        notix.getAlarms();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        picker.show(getFragmentManager(), "Timepickerdialog");
    }

    void setInfiniteList() {
        infiniteListView = (InfiniteListView) findViewById(R.id.content_alarm);

        final InfiniteListAdapter<JSONObject> adapter = new InfiniteListAdapter<JSONObject>(this, R.layout.alarm, alarms) {
            @Override
            public void onNewLoadRequired() {
            }

            @Override
            public void onRefresh() {
                infiniteListView.clearList();
                notix.getAlarms();

            }

            @Override
            public void onItemClick(int i) {
            }

            @Override
            public void onItemLongClick(int i) {
            }

            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.alarm, parent, false);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.content = (TextView) convertView.findViewById(R.id.content);
                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final JSONObject reporte = alarms.get(position);
                if (reporte != null) {
                    try {
                        String message = reporte.getString("message");
                        String hora = reporte.getString("hora");

                        holder.content.setText(hora);
                        holder.title.setText(message);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
        infiniteListView.startLoading();
        notix.getAlarms();
    }

    @Override
    public void onAlarm(JSONObject alarm) {
        try {
            String html = alarm.getString("html");
            NotixFactory.buildAlarm(this, html);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infiniteListView.startLoading();
                    notix.getAlarms();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShowAlarm(final JSONArray alarms) {
        Log.i("alarm", alarms.toString());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlarmActivity.this.infiniteListView.clearList();
                for (int i = 0; i < alarms.length(); i++) {
                    try {
                        JSONObject alarm = alarms.getJSONObject(i);
                        AlarmActivity.this.infiniteListView.addNewItem(alarm);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                infiniteListView.stopLoading();
            }
        });
    }

    static class ViewHolder {
        TextView title;
        TextView content;
    }
}
