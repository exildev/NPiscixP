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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.liuguangqiang.ipicker.IPicker;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

import static java.lang.String.format;


public class ReporteActivity extends AppCompatActivity implements IPicker.OnSelectedListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;

    Field[] types;
    int selectedType = 0;

    Field[] piscinas;
    int selectedPiscina = 0;
    private List<String> images;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        images = new ArrayList<>();

        Intent intent = getIntent();

        if (intent.hasExtra("piscinas")) {
            String json = intent.getStringExtra("piscinas");
            try {
                JSONArray piscinas = new JSONArray(json);
                setPiscinaSpinner(piscinas);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (intent.hasExtra("name")){
            String name = intent.getStringExtra("name");
            TextView n = (TextView) findViewById(R.id.cliente_title);
            n.setText(name);
        }

        IPicker.setOnSelectedListener(this);

        setTypeSpinner();
        validPermissions();
    }

    private void setPiscinaSpinner(JSONArray object_list) {
        piscinas = new Field[object_list.length()];
        for (int i = 0; i < object_list.length(); i++) {
            try {
                JSONObject persona = object_list.getJSONObject(i);
                String nombre = persona.getString("nombre");
                int id = persona.getInt("id");
                Field person = new Field(id, nombre);
                piscinas[i] = person;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<Field> adapter = new ArrayAdapter<>(ReporteActivity.this, android.R.layout.simple_spinner_item, piscinas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.piscina);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPiscina = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setTypeSpinner() {
        showLoading();
        String serviceUrl = getString(R.string.get_reporte_tipo);
        String url = getString(R.string.url, serviceUrl);
        JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    types = new Field[object_list.length()];
                    for (int i = 0; i < object_list.length(); i++) {
                        try {
                            JSONObject persona = object_list.getJSONObject(i);
                            String nombre = persona.getString("nombre");
                            int id = persona.getInt("id");
                            Field person = new Field(id, nombre);
                            types[i] = person;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ArrayAdapter<Field> adapter = new ArrayAdapter<>(ReporteActivity.this, android.R.layout.simple_spinner_item, types);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.tipo);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            selectedType = i;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    hideLoading();
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
        loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
    }

    public void openPicker() {
        IPicker.setLimit(5);
        IPicker.open(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reporte, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_add_a_photo) {
            openPicker();
            return false;
        }
        if (id == R.id.nav_send) {
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
            }
            else {
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
                                ReporteActivity.this.finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void send() {
        final CircularProgressView circle = (CircularProgressView) findViewById(R.id.progress_view);
        circle.setMaxProgress(100);
        circle.setProgress(0);

        String nombre = ((TextView) findViewById(R.id.nombre)).getText().toString();
        String descripcion = ((TextView) findViewById(R.id.descripcion)).getText().toString();
        String tipo_de_reporte = types[selectedType].id + "";
        String piscina = piscinas[selectedPiscina].id + "";
        String latitud = format("%s", getLastBestLocation().getLatitude());
        String longitud = format("%s", getLastBestLocation().getLongitude());

        if(nombre.equals("")){
            Snackbar.make(findViewById(R.id.nombre), "El campo nombre no puede estar vacio", 800).show();
            return;
        }

        if (descripcion.equals("")){
            Snackbar.make(findViewById(R.id.descripcion), "El campo descripción no puede estar vacio", 800).show();
            return;
        }

        if (images.size() < 1){
            Snackbar.make(findViewById(R.id.descripcion), "Debe escojer al menos una imagen", 800).show();
            return;
        }

        showLoading();

        try {
            UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                    .setTitle("Subiendo reporte")
                    .setInProgressMessage("Subiendo reporte a [[UPLOAD_RATE]] ([[PROGRESS]])")
                    .setErrorMessage("Hubo un error al subir el reporte")
                    .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                    .setAutoClearOnSuccess(true);

            String serviceUrl = getString(R.string.reporte_form);
            String url = getString(R.string.url, serviceUrl);
            MultipartUploadRequest upload =
                    new MultipartUploadRequest(getBaseContext(), url)
                            .setNotificationConfig(notificationConfig)
                            .setAutoDeleteFilesAfterSuccessfulUpload(false)
                            .setMaxRetries(1)
                            .addParameter("nombre", nombre)
                            .addParameter("descripcion", descripcion)
                            .addParameter("tipo_de_reporte", tipo_de_reporte)
                            .addParameter("piscina", piscina)
                            .addParameter("latitud", latitud)
                            .addParameter("longitud", longitud)
                            .addParameter("fotoreporte_set-TOTAL_FORMS", String.valueOf(images.size()))
                            .addParameter("fotoreporte_set-INITIAL_FORMS", "0")
                            .addParameter("fotoreporte_set-MIN_NUM_FORMS", "0")
                            .addParameter("fotoreporte_set-MAX_NUM_FORMS", "5");
            for (int i = 0; i < images.size(); i++) {
                String image = images.get(i);
                upload.addFileToUpload(image, "fotoreporte_set-" + i + "-url");
            }

            upload.setDelegate(new UploadStatusDelegate() {
                @Override
                public void onProgress(UploadInfo uploadInfo) {

                }

                @Override
                public void onError(UploadInfo uploadInfo, Exception exception) {
                    hideLoading();
                    Log.e("send", exception.getMessage());
                }

                @Override
                public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                    hideLoading();
                    Intent intent = new Intent(ReporteActivity.this, ListReporteActivity.class);
                    intent.putExtra("send", true);
                    intent.putExtras(getIntent());
                    startActivity(intent);
                    finish();
                    Log.e("send", "code: " + serverResponse.getHttpCode());
                    Log.e("send", serverResponse.getBodyAsString());
                }

                @Override
                public void onCancelled(UploadInfo uploadInfo) {
                }
            }).startUpload();
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    private void showLoading() {
        View modal = findViewById(R.id.modal);
        modal.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        View modal = findViewById(R.id.modal);
        modal.setVisibility(View.GONE);
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
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private void checkGPS(){
        LocationManager L = (LocationManager) getSystemService(LOCATION_SERVICE);

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
                            ReporteActivity.this.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }else {
            initGPS();
        }
    }

    private void validPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }else if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }else {
                checkGPS();
            }
        }else {
            checkGPS();
        }
    }

    @Override
    public void onSelected(List<String> paths) {
        images = paths;
    }
}
