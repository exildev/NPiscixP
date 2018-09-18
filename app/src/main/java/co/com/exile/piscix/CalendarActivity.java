package co.com.exile.piscix;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import co.com.exile.piscix.models.Actividad;

public class CalendarActivity extends AppCompatActivity {
    private InfiniteListView infiniteListView;
    private ArrayList<Actividad> itemList;

    private Calendar start;
    private Calendar end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        start = Calendar.getInstance();
        end = Calendar.getInstance();

        setInfiniteList();
        setButtons();
    }

    void setButtons() {
        Button startB = (Button) findViewById(R.id.start);
        Button endB = (Button) findViewById(R.id.end);
        startB.setText(start.get(Calendar.DAY_OF_MONTH) + "/" + (start.get(Calendar.MONTH) + 1) + "/" + start.get(Calendar.YEAR));
        endB.setText(end.get(Calendar.DAY_OF_MONTH) + "/" + (end.get(Calendar.MONTH) + 1) + "/" + end.get(Calendar.YEAR));
    }

    public void setDate(View view) {
        final int id = view.getId();
        Calendar date;
        if (id == R.id.start) {
            date = start;
        } else {
            date = end;
        }
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        if (id == R.id.start || start.after(c)) {
                            start.set(year, monthOfYear, dayOfMonth);
                        }

                        if (id == R.id.end || end.before(c)) {
                            end.set(year, monthOfYear, dayOfMonth);
                        }

                        setButtons();
                        infiniteListView.clearList();
                        loadActivities();
                    }
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    void setInfiniteList() {
        infiniteListView = (InfiniteListView) findViewById(R.id.content_calendar);

        itemList = new ArrayList<>();
        final InfiniteListAdapter adapter = new InfiniteListAdapter<Actividad>(this, R.layout.actividad, itemList) {
            @Override
            public void onNewLoadRequired() {
                loadActivities();
            }

            @Override
            public void onRefresh() {
                infiniteListView.clearList();
                loadActivities();
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
                    convertView = getLayoutInflater().inflate(R.layout.actividad, parent, false);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.fecha = (TextView) convertView.findViewById(R.id.date);

                    holder.icon = (CardView) convertView.findViewById(R.id.icon_card);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final Actividad actividad = itemList.get(position);
                if (actividad != null) {
                    holder.title.setText(actividad.getTitle());
                    holder.fecha.setText(actividad.getDate());
                    if (actividad.getColor().equals("gray")) {
                        holder.icon.setCardBackgroundColor(ContextCompat.getColor(CalendarActivity.this, R.color.grey));
                    } else {
                        try {
                            holder.icon.setCardBackgroundColor(Color.parseColor(actividad.getColor()));
                        } catch (IllegalArgumentException e) {
                            Log.e("tales", "error", e);
                        }
                    }
                }
                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
        loadActivities();
    }

    private void loadActivities() {
        infiniteListView.startLoading();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String s = formater.format(start.getTime());
        String e = formater.format(end.getTime());
        String serviceUrl = getString(R.string.calendar_notificaciones, s, e);
        String url = getString(R.string.url, serviceUrl);
        Log.e("tales", url);
        JsonArrayRequest loginRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                infiniteListView.stopLoading();
                Log.e("tales", response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject activity = response.getJSONObject(i);
                        String title = activity.getString("title");
                        String date = activity.getString("start");
                        String color = activity.getString("color");
                        String type = activity.getString("type");
                        infiniteListView.addNewItem(new Actividad(title, color, date));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                infiniteListView.stopLoading();
                CardView container = (CardView) findViewById(R.id.error_container);
                VolleySingleton.manageError(CalendarActivity.this, error, container, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("rety", "rety");
                    }
                });
                Log.e("Activities", error.toString());
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
    }

    static class ViewHolder {
        TextView title;
        TextView fecha;
        CardView icon;
    }
}
