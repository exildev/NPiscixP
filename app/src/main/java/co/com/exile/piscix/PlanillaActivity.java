package co.com.exile.piscix;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.Map;

public class PlanillaActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;

    LocationManager locationManager;

    private int piscina;
    private int planilla;
    private double lat;
    private double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planilla);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        hideLoading();

        piscina = getIntent().getIntExtra("piscina", -1);

        if (getIntent().hasExtra("planilla")) {
            planilla = getIntent().getIntExtra("planilla", -1);
            getData();
        } else {
            planilla = -1;
        }

        validPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.planilla, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_send) {
            send();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION || requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                validPermissions();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Para que esta aplicación funcione correctamente usted debe dar permisos de acceso al GPS ¿Desea hacerlo ahora?")
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                validPermissions();
                            }
                        })
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                PlanillaActivity.this.finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    public void send() {
        showLoading();
        final CheckBox cepillado = (CheckBox) findViewById(R.id.cepillado);
        final CheckBox aspirado = (CheckBox) findViewById(R.id.aspirado);
        final CheckBox retrolavado = (CheckBox) findViewById(R.id.retrolavado);
        final CheckBox aumento_agua = (CheckBox) findViewById(R.id.aumento_agua);
        final CheckBox aplicacion_cloro = (CheckBox) findViewById(R.id.aplicacion_cloro);
        final CheckBox aplicacion_sulfato_al = (CheckBox) findViewById(R.id.aplicacion_sulfato_al);
        final CheckBox clasificador_alg = (CheckBox) findViewById(R.id.clasificador_agl);
        final CheckBox espera = (CheckBox) findViewById(R.id.espera);
        final RadioButton disminucion_ph = (RadioButton) findViewById(R.id.disminucion_ph);
        final RadioButton aumento_ph = (RadioButton) findViewById(R.id.aumento_ph);
        final TextInputEditText nivel_ph = (TextInputEditText) findViewById(R.id.nivel_ph);
        final TextInputEditText nivel_cloro = (TextInputEditText) findViewById(R.id.nivel_cloro);
        final TextInputEditText observaciones = (TextInputEditText) findViewById(R.id.observaciones);
        final String latitud = planilla == -1 ? String.valueOf(getLastBestLocation().getLatitude()) : String.valueOf(lat);
        final String longitud = planilla == -1 ? String.valueOf(getLastBestLocation().getLongitude()) : String.valueOf(lng);

        String url = "http://104.236.33.228:8050/actividades/planilladiaria/form/";
        if (planilla != -1) {
            url = "http://104.236.33.228:8050/actividades/planilladiaria/edit/form/" + planilla + "/";
        }
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent data = new Intent();
                        data.putExtra("response", response);
                        data.putExtra("status", 200);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err = new String(error.networkResponse.data);
                        Intent data = new Intent();
                        data.putExtra("response", err);
                        data.putExtra("status", error.networkResponse.statusCode);
                        setResult(RESULT_OK, data);
                        for (String r : err.split("\n")) {
                            Log.e("solucion", r);
                        }
                        finish();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (cepillado.isChecked()) {
                    params.put("cepillado", "on");
                }
                if (aspirado.isChecked()) {
                    params.put("aspirado", "on");
                }
                if (retrolavado.isChecked()) {
                    params.put("retrolavado", "on");
                }
                if (aumento_agua.isChecked()) {
                    params.put("aumento_agua", "on");
                }
                if (aplicacion_cloro.isChecked()) {
                    params.put("aplicacion_cloro", "on");
                }
                if (aplicacion_sulfato_al.isChecked()) {
                    params.put("aplicacion_sulfato_al", "on");
                }
                if (disminucion_ph.isChecked()) {
                    params.put("disminucion_ph", "on");
                }
                if (aumento_ph.isChecked()) {
                    params.put("aumento_ph", "on");
                }
                if (clasificador_alg.isChecked()) {
                    params.put("clasificador_alg", "on");
                }
                if (espera.isChecked()) {
                    params.put("espera", "on");
                }
                params.put("nivel_cloro", nivel_cloro.getText().toString());
                params.put("nivel_ph", nivel_ph.getText().toString());
                params.put("observaciones", observaciones.getText().toString());
                params.put("piscina", String.valueOf(piscina));
                params.put("latitud", latitud);
                params.put("longitud", longitud);
                return params;
            }
        };
        loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
    }

    void getData() {
        showLoading();
        String url = "http://104.236.33.228:8050/actividades/planilladiaria/list/?pk=" + planilla;
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    hideLoading();
                    JSONArray object_list = response.getJSONArray("object_list");

                    if (object_list.length() > 0) {
                        JSONObject campo = object_list.getJSONObject(0);

                        CheckBox cepillado = (CheckBox) findViewById(R.id.cepillado);
                        CheckBox aspirado = (CheckBox) findViewById(R.id.aspirado);
                        CheckBox retrolavado = (CheckBox) findViewById(R.id.retrolavado);
                        CheckBox aumento_agua = (CheckBox) findViewById(R.id.aumento_agua);
                        CheckBox aplicacion_cloro = (CheckBox) findViewById(R.id.aplicacion_cloro);
                        CheckBox aplicacion_sulfato_al = (CheckBox) findViewById(R.id.aplicacion_sulfato_al);
                        CheckBox clasificador_alg = (CheckBox) findViewById(R.id.clasificador_agl);
                        CheckBox espera = (CheckBox) findViewById(R.id.espera);
                        RadioButton disminucion_ph = (RadioButton) findViewById(R.id.disminucion_ph);
                        RadioButton aumento_ph = (RadioButton) findViewById(R.id.aumento_ph);
                        TextInputEditText nivel_ph = (TextInputEditText) findViewById(R.id.nivel_ph);
                        TextInputEditText nivel_cloro = (TextInputEditText) findViewById(R.id.nivel_cloro);
                        TextInputEditText observaciones = (TextInputEditText) findViewById(R.id.observaciones);

                        cepillado.setChecked(campo.getBoolean("cepillado"));
                        aspirado.setChecked(campo.getBoolean("aspirado"));
                        retrolavado.setChecked(campo.getBoolean("retrolavado"));
                        aumento_agua.setChecked(campo.getBoolean("aumento_agua"));
                        aplicacion_cloro.setChecked(campo.getBoolean("aplicacion_cloro"));
                        aplicacion_sulfato_al.setChecked(campo.getBoolean("aplicacion_sulfato_al"));
                        clasificador_alg.setChecked(campo.getBoolean("clasificador_alg"));
                        espera.setChecked(campo.getBoolean("espera"));
                        disminucion_ph.setChecked(campo.getBoolean("disminucion_ph"));
                        aumento_ph.setChecked(campo.getBoolean("aumento_ph"));
                        nivel_ph.setText(String.valueOf(campo.getDouble("nivel_ph")));
                        nivel_cloro.setText(String.valueOf(campo.getDouble("nivel_cloro")));
                        observaciones.setText(campo.getString("observaciones"));

                        lat = campo.getDouble("latitud");
                        lng = campo.getDouble("longitud");
                        Log.i("json", campo.toString());
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

        VolleySingleton.getInstance(this).addToRequestQueue(reportesRequest);
    }

    private void showLoading() {
        View modal = findViewById(R.id.modal);
        modal.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        View modal = findViewById(R.id.modal);
        modal.setVisibility(View.GONE);
    }

    private void validPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                checkGPS();
            }
        } else {
            checkGPS();
        }
    }

    private void checkGPS() {
        LocationManager L = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!L.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Para que esta aplicación funcione correctamente debe estar atcivado el GPS\n¿Desea activarlo ahora?")
                    .setCancelable(false)
                    .setPositiveButton("Activar GPS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent I = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(I);
                        }
                    })
                    .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            initGPS();
        }
    }

    private void initGPS() {
        Toast.makeText(this, "pidiendo el gps ", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            validPermissions();
            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    @Nullable
    private Location getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            validPermissions();
            return null;
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }
}
