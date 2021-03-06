package co.com.exile.piscix;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlanillaActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    private static final int REQUEST_LOCATION_SETTINGS = 12;

    private int piscina;
    private int planilla;
    private double lat;
    private double lng;

    private GoogleApiClient mGoogleClient;
    private LocationRequest mLocationRequest;
    private Location myLocation;

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

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleClient.connect();
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
        final String latitud = planilla == -1 ? String.valueOf(myLocation.getLatitude()) : String.valueOf(lat);
        final String longitud = planilla == -1 ? String.valueOf(myLocation.getLongitude()) : String.valueOf(lng);

        String serviceUrl = getString(R.string.planilla_form);

        if (planilla != -1) {
            serviceUrl = getString(R.string.planilla_edit, planilla);
        }
        String url = getUrl(serviceUrl);
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
                        if (error.networkResponse == null) {
                            hideLoading();
                            final CardView container = (CardView) findViewById(R.id.error_container);
                            container.setVisibility(View.VISIBLE);
                            VolleySingleton.manageError(PlanillaActivity.this, error, container, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    container.setVisibility(View.GONE);
                                    getData();
                                    Log.i("rety", "rety");
                                }
                            });
                        } else {
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
        String serviceUrl = getString(R.string.planilla_info, planilla);
        String url = getUrl(serviceUrl);
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    hideLoading();
                    JSONArray object_list = response.getJSONArray("object_list");

                    if (object_list.length() > 0) {
                        JSONObject campo = object_list.getJSONObject(0);
                        Log.i("json", campo.toString());
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
                        if (campo.has("nivel_ph") && !campo.get("nivel_ph").equals(null)) {
                            nivel_ph.setText(String.valueOf(campo.getDouble("nivel_ph")));
                        }
                        if (campo.has("nivel_cloro") && !campo.get("nivel_cloro").equals(null)) {
                            nivel_cloro.setText(String.valueOf(campo.getDouble("nivel_cloro")));
                        }
                        if (campo.has("observaciones") && !campo.get("observaciones").equals(nivel_cloro)) {
                            observaciones.setText(campo.getString("observaciones"));
                        }
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
                hideLoading();
                final CardView container = (CardView) findViewById(R.id.error_container);
                container.setVisibility(View.VISIBLE);
                VolleySingleton.manageError(PlanillaActivity.this, error, container, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        container.setVisibility(View.GONE);
                        getData();
                        Log.i("rety", "rety");
                    }
                });
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
                createLocationRequest();
            }
        } else {
            createLocationRequest();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                        builder.build());


        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.i("settings", "si tal");
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(PlanillaActivity.this, REQUEST_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.i("settings", "no tal");
                        break;
                }
            }
        });
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            validPermissions();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleClient, this);
    }

    @Override
    public void onDestroy() {
        if (mGoogleClient.isConnected()) {
            stopLocationUpdates();
        }
        super.onDestroy();
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
                builder.setMessage(R.string.gps_permissions_message)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                validPermissions();
                            }
                        })
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            Log.i("GPS", "" + (resultCode == Activity.RESULT_OK));
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.activate_gps_message)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                createLocationRequest();
                            }
                        })
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
