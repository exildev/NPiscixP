package co.com.exile.piscix;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONObject;

import java.util.Calendar;

import co.com.exile.piscix.notix.AlarmListener;
import co.com.exile.piscix.notix.Notix;
import co.com.exile.piscix.notix.NotixFactory;

public class AlarmActivity extends AppCompatActivity implements AlarmListener {

    private Notix notix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notix = NotixFactory.buildNotix(this);
        notix.setAlarmListener(this);
    }

    public void add(View view) {
        new MaterialDialog.Builder(this)
                .title("Reporte informativo")
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
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                        Calendar now = Calendar.getInstance();
                        Calendar hora = Calendar.getInstance();

                        if (now.get(Calendar.HOUR_OF_DAY) > hourOfDay || (now.get(Calendar.HOUR_OF_DAY) == hourOfDay && now.get(Calendar.MINUTE) > minute)) {
                            hora.add(Calendar.DATE, 1);
                        }

                        hora.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        hora.set(Calendar.MINUTE, minute);
                        long dif = hora.getTimeInMillis() - now.getTimeInMillis();
                        Log.i("hora", (hora.get(Calendar.DAY_OF_MONTH) + "/" + (hora.get(Calendar.MONTH) + 1) + "/" + hora.get(Calendar.YEAR) + " " + hora.get(Calendar.HOUR_OF_DAY) + ":" + hora.get(Calendar.MINUTE)));
                        Log.i("now", (now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH) + 1) + "/" + now.get(Calendar.YEAR) + " " + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE)));
                        Log.i("dif", "" + dif);
                        notix.addAlarm(nombre, hora.get(Calendar.HOUR_OF_DAY) + ":" + hora.get(Calendar.MINUTE), dif + "");
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        picker.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onAlarm(JSONObject alarm) {
        Log.i("alarm", alarm.toString());
    }
}
