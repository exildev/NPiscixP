package co.com.exile.piscix;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import com.liuguangqiang.ipicker.IPicker;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.exile.piscix.models.Reporte;
import co.com.exile.piscix.utils.ScalingUtilities;

public class ListReporteActivity extends BaseActivity implements IPicker.OnSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    private static final int REQUEST_LOCATION_SETTINGS = 12;

    private List<String> images;

    private InfiniteListView infiniteListView;
    private ArrayList<Reporte> itemList;
    private int page;
    private String search = "";

    private GoogleApiClient mGoogleClient;
    private LocationRequest mLocationRequest;
    private Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_reporte);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        page = 1;
        images = new ArrayList<>();

        IPicker.setOnSelectedListener(this);

        setInfiniteList();
        if (getIntent().hasExtra("send")){
            Snackbar.make(findViewById(R.id.content_list_reporte), "Registro guardado con exito", 1000).show();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_reporte, menu);
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                Log.i("search", "SearchOnQueryTextSubmit: " + query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                search = s;
                infiniteListView.clearList();
                page = 1;
                getReportes();
                return false;
            }
        });
        return true;
    }

    void setInfiniteList() {
        infiniteListView = (InfiniteListView) findViewById(R.id.content_list_reporte);

        itemList = new ArrayList<>();
        final InfiniteListAdapter adapter = new InfiniteListAdapter<Reporte>(this, R.layout.reporte, itemList) {
            @Override
            public void onNewLoadRequired() {
                getReportes();
            }

            @Override
            public void onRefresh() {
                infiniteListView.clearList();
                page = 1;
                getReportes();
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

                ListReporteActivity.ViewHolder holder;

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.reporte, parent, false);
                    holder = new ListReporteActivity.ViewHolder();
                    holder.nombre = (TextView) convertView.findViewById(R.id.nombre);
                    holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
                    holder.cierre = (TextView) convertView.findViewById(R.id.cierre);
                    holder.cliente = (TextView) convertView.findViewById(R.id.cliente);
                    holder.descripcion = (TextView) convertView.findViewById(R.id.descripcion);
                    holder.estado = (TextView) convertView.findViewById(R.id.estado);
                    holder.fecha = (TextView) convertView.findViewById(R.id.fecha);
                    holder.piscina = (TextView) convertView.findViewById(R.id.piscina);
                    holder.tipo = (TextView) convertView.findViewById(R.id.tipo);
                    holder.solution_button = (Button) convertView.findViewById(R.id.solution_button);
                    holder.chat_button = (Button) convertView.findViewById(R.id.chat_button);
                    holder.photos_button = (Button) convertView.findViewById(R.id.photos_button);
                    holder.icon = (CardView) convertView.findViewById(R.id.pedido_icon_card);
                    holder.numero = (TextView) convertView.findViewById(R.id.numero);

                    convertView.setTag(holder);

                } else {
                    holder = (ListReporteActivity.ViewHolder) convertView.getTag();
                }

                final Reporte reporte = itemList.get(position);
                if (reporte != null) {
                    holder.nombre.setText(reporte.getNombre());
                    holder.cierre.setText(reporte.getCierre());
                    holder.cliente.setText(reporte.getCliente());
                    holder.descripcion.setText(reporte.getDescripcion());
                    holder.fecha.setText(reporte.getFecha());
                    holder.piscina.setText(reporte.getPiscina());
                    holder.tipo.setText(reporte.getTipo_de_reporte());
                    holder.subtitle.setText(reporte.getNombre());
                    if (reporte.getNumero().equals("") || reporte.getNumero().equals("null")){
                        holder.numero.setText(R.string.sin_numero);
                    }else {
                        holder.numero.setText(reporte.getNumero());
                    }
                    if (!reporte.isEstado()) {
                        holder.subtitle.setText(R.string.estado_abierto);
                        holder.estado.setText(R.string.estado_abierto);
                        holder.icon.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorReport));
                        holder.chat_button.setVisibility(View.VISIBLE);
                        holder.solution_button.setVisibility(View.VISIBLE);
                    } else {
                        holder.subtitle.setText(R.string.estado_cerrado);
                        holder.estado.setText(R.string.estado_cerrado);
                        holder.chat_button.setVisibility(View.GONE);
                        holder.solution_button.setVisibility(View.GONE);
                        holder.icon.setCardBackgroundColor(Color.parseColor("#b2b2b2"));
                    }

                    holder.solution_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            solucion(position);
                        }
                    });
                    holder.photos_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            initGallery(position);
                        }
                    });

                    holder.chat_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ListReporteActivity.this, ChatActivity.class);
                            intent.putExtra("reporte", reporte.getId());
                            intent.putExtra("reporte_title", reporte.getNombre());
                            intent.putExtra("reporte_fecha", reporte.getFecha());
                            intent.putExtra("reporte_cliente", reporte.getCliente());
                            startActivity(intent);
                        }
                    });
                }

                final View action = convertView.findViewById(R.id.action);
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        action(view);
                    }
                });

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
        getReportes();
    }

    private void initGallery(int position) {
        String serviceUrl = getString(R.string.foto_reporte);
        String url = getUrl(serviceUrl);
        int id = itemList.get(position).getId();

        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    void getReportes() {
        int id = getIntent().getIntExtra("id", -1);
        infiniteListView.startLoading();

        String serviceUrl = getString(R.string.list_reporte, id, page, search);
        String url = getUrl(serviceUrl);
        JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    infiniteListView.stopLoading();
                    JSONArray object_list = response.getJSONArray("object_list");
                    int count = response.getInt("count");
                    if (response.has("next")) {
                        page = response.getInt("next");
                    }
                    for (int i = 0; i < object_list.length(); i++) {
                        JSONObject campo = object_list.getJSONObject(i);
                        int id = campo.getInt("id");
                        String nombre = campo.getString("nombre");
                        String descripcion = campo.getString("descripcion");
                        String tipo_de_reporte = campo.getString("tipo_n");
                        String piscina = campo.getString("piscina__nombre");
                        boolean estado = campo.getBoolean("estado");
                        String fecha = campo.getString("fecha");
                        String cliente = campo.getString("nombreC") + " " + campo.getString("apellidosC");
                        String numero = campo.getString("numero");
                        String cierre = Reporte.CIERRES[campo.getInt("cierre") - 1];
                        infiniteListView.addNewItem(new Reporte(id, nombre, descripcion, tipo_de_reporte, piscina, estado, fecha, cliente, cierre, numero));
                    }
                    if (itemList.size() == count) {
                        infiniteListView.hasMore(false);
                    } else {
                        infiniteListView.hasMore(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                infiniteListView.stopLoading();
                CardView container = (CardView) findViewById(R.id.error_container);
                VolleySingleton.manageError(ListReporteActivity.this, error, container, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("rety", "rety");
                    }
                });
                Log.e("Activities", error.toString());
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(reportesRequest);
    }

    public void action(View view){
        ViewGroup row = (ViewGroup) view.getParent();
        final RelativeLayout container = (RelativeLayout) row.findViewById(R.id.container);
        TextView subtitle = (TextView) row.findViewById(R.id.subtitle);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int close_height = (int) (72 * scale + 0.5f);
        final ImageView imageDrop = (ImageView) row.findViewById(R.id.drop_image);

        if (container.getHeight() == close_height) {
            subtitle.setVisibility(View.GONE);
            expand(container, imageDrop);
        }else {
            subtitle.setVisibility(View.VISIBLE);
            collapse(container, imageDrop);
        }
    }

    private void expand(final View container, final ImageView icon) {
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        final int close_height = (int) (72 * scale + 0.5f);
        container.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = container.getMeasuredHeight() - close_height;

        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_icon);

        icon.startAnimation(rotate);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                container.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (close_height + (int) (targetHeight * interpolatedTime));
                container.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(200);
        container.startAnimation(a);
    }

    private void collapse(final View container, final ImageView icon){
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        final int actualHeight = container.getMeasuredHeight();
        int targetHeight = (int) (72 * scale + 0.5f);
        ValueAnimator va = ValueAnimator.ofInt(actualHeight, targetHeight);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_icon_back);

        icon.startAnimation(rotate);

        va.setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                container.getLayoutParams().height = value;
                container.getLayoutParams().width = value;
                container.requestLayout();
            }
        });
        va.start();
    }

    private void solucion(final int position) {
        new MaterialDialog.Builder(this)
                .title("Solución")
                .customView(R.layout.solucion, true)
                .positiveText("Guardar")
                .negativeText("Cerrar")
                .negativeColor(ContextCompat.getColor(this, R.color.colorReport))
                .neutralText("Fotos")
                .neutralColor(ContextCompat.getColor(this, R.color.colorReportAccent))
                .autoDismiss(false)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        openPicker();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        images = new ArrayList<>();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        images = new ArrayList<>();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        send(dialog, position);
                    }
                })
                .show();
    }

    public void openPicker() {
        IPicker.setLimit(5);
        IPicker.open(this);
    }

    private void send(final MaterialDialog dialog, int position) {

        dialog.dismiss();
        View view = dialog.getCustomView();

        assert view != null;
        String nombre = ((TextView) view.findViewById(R.id.nombre)).getText().toString();
        String descripcion = ((TextView) view.findViewById(R.id.descripcion)).getText().toString();
        String reporte = String.valueOf(itemList.get(position).getId());
        final String latitud = String.valueOf(myLocation.getLatitude());
        final String longitud = String.valueOf(myLocation.getLongitude());

        if (nombre.equals("")) {
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.nombre_container);
            til.setError("Debe ingresar un nombre");
            return;
        } else {
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.nombre_container);
            til.setError("");
        }

        if (descripcion.equals("")) {
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.descripcion_container);
            til.setError("Debe ingresar una descripción");
            return;
        } else {
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.descripcion_container);
            til.setError("");
        }

        if (images.size() < 1) {
            send(nombre, descripcion, reporte, latitud, longitud);
            return;
        }

        final MaterialDialog loading = new MaterialDialog.Builder(this)
                .title("Subiendo reporte")
                .content("Por favor espere")
                .progress(true, 0)
                .show();

        try {
            UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                    .setTitle("Subiendo solucion")
                    .setInProgressMessage("Subiendo solucion a [[UPLOAD_RATE]] ([[PROGRESS]])")
                    .setErrorMessage("Hubo un error al subir la solucion")
                    .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                    .setAutoClearOnSuccess(true);

            String serviceUrl = getString(R.string.solucion_form);
            String url = getUrl(serviceUrl);
            MultipartUploadRequest upload =
                    new MultipartUploadRequest(this, url)
                            .setNotificationConfig(notificationConfig)
                            .setAutoDeleteFilesAfterSuccessfulUpload(false)
                            .setMaxRetries(1)
                            .setUtf8Charset()
                            .addParameter("nombre", nombre)
                            .addParameter("descripcion", descripcion)
                            .addParameter("reporte", reporte)
                            .addParameter("latitud", latitud)
                            .addParameter("longitud", longitud)
                            .addParameter("fotosolucion_set-TOTAL_FORMS", String.valueOf(images.size()))
                            .addParameter("fotosolucion_set-INITIAL_FORMS", "0")
                            .addParameter("fotosolucion_set-MIN_NUM_FORMS", "0")
                            .addParameter("fotosolucion_set-MAX_NUM_FORMS", "5");
            for (int i = 0; i < images.size(); i++) {
                String image = images.get(i);
                image = ScalingUtilities.decodeFile(image, 1024, 1024);
                upload.addFileToUpload(image, "fotosolucion_set-" + i + "-url");
            }

            upload.setDelegate(new UploadStatusDelegate() {
                @Override
                public void onProgress(UploadInfo uploadInfo) {

                }

                @Override
                public void onError(UploadInfo uploadInfo, Exception exception) {
                    loading.dismiss();
                    Log.e("send", exception.getMessage());
                }

                @Override
                public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                    loading.dismiss();
                    infiniteListView.clearList();
                    page = 1;
                    getReportes();
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

    public void send(final String nombre, final String descripcion, final String reporte, final String latitud, final String longitud) {
        final MaterialDialog loading = new MaterialDialog.Builder(this)
                .title("Guardando solucion")
                .content("Por favor espere")
                .progress(true, 0)
                .show();

        String serviceUrl = getString(R.string.solucion_form);
        String url = getUrl(serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        infiniteListView.clearList();
                        page = 1;
                        getReportes();
                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.e("solucion", new String(error.networkResponse.data));
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nombre", nombre);
                params.put("descripcion", descripcion);
                params.put("latitud", latitud);
                params.put("longitud", longitud);
                params.put("reporte", reporte);
                params.put("fotosolucion_set-TOTAL_FORMS", "0");
                params.put("fotosolucion_set-INITIAL_FORMS", "0");
                params.put("fotosolucion_set-MIN_NUM_FORMS", "0");
                params.put("fotosolucion_set-MAX_NUM_FORMS", "5");
                return params;
            }
        };
        loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
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
                            status.startResolutionForResult(ListReporteActivity.this, REQUEST_LOCATION_SETTINGS);
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
    public void onSelected(List<String> paths) {
        images = paths;
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

    static class ViewHolder {
        TextView nombre;
        TextView subtitle;
        TextView cliente;
        TextView fecha;
        TextView estado;
        TextView piscina;
        TextView tipo;
        TextView cierre;
        TextView descripcion;
        TextView numero;
        CardView icon;
        Button chat_button;
        Button solution_button;
        Button photos_button;
    }
}
