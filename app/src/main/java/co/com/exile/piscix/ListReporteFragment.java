package co.com.exile.piscix;


import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
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

import static java.lang.String.format;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListReporteFragment extends Fragment implements IPicker.OnSelectedListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    LocationManager locationManager;

    private static final int REQUEST_CODE_PICKER = 2;
    private List<String> images;

    private InfiniteListView infiniteListView;
    private ArrayList<Reporte> itemList;
    private int page;
    private String search = "";
    private SearchView searchView;

    public ListReporteFragment() {
        page = 1;
        images = new ArrayList<>();
    }

    public static ListReporteFragment listReporteFragmentInstance(SearchView searchView) {
        // Required empty public constructor
        ListReporteFragment f = new ListReporteFragment();
        f.searchView = searchView;
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_list_reporte, container, false);
        setInfiniteList(fragment);
        setSearchView();
        validPermissions();
        IPicker.setOnSelectedListener(this);
        return fragment;
    }

    void setInfiniteList(View fragment) {
        infiniteListView = (InfiniteListView) fragment.findViewById(R.id.content_list_reporte);

        itemList = new ArrayList<>();
        InfiniteListAdapter adapter = new InfiniteListAdapter<Reporte>(this.getActivity(), R.layout.reporte, itemList) {
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
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.reporte, parent, false);
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
                        holder.numero.setText("Sin numero");
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
                            solucion(view, position);
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
                            Intent intent = new Intent(ListReporteFragment.this.getActivity(), ChatActivity.class);
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

    private void initGallery(int position){
        int id = itemList.get(position).getId();
        String serviceUrl = getString(R.string.foto_reporte);
        String url = getString(R.string.url, serviceUrl);

        Intent intent = new Intent(this.getActivity(), GalleryActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    void getReportes() {
        infiniteListView.startLoading();
        String serviceUrl = getString(R.string.list_reporte_no_cliente, page, search);
        String url = getString(R.string.url, serviceUrl);
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
                Log.e("Activities", error.toString());
            }
        });

        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(reportesRequest);
    }

    public void action(View view) {
        ViewGroup row = (ViewGroup) view.getParent();
        final RelativeLayout container = (RelativeLayout) row.findViewById(R.id.container);
        TextView subtitle = (TextView) row.findViewById(R.id.subtitle);
        TextView title = (TextView) row.findViewById(R.id.nombre);
        final float scale = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
        int close_height = (int) (72 * scale + 0.5f);
        final ImageView imageDrop = (ImageView) row.findViewById(R.id.drop_image);

        if (container.getHeight() == close_height) {
            subtitle.setVisibility(View.GONE);
            title.setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
            expand(container, imageDrop);
        } else {
            title.setTextColor(Color.parseColor("#000000"));
            subtitle.setVisibility(View.VISIBLE);
            collapse(container, imageDrop);
        }
    }

    private void expand(final View container, final ImageView icon) {
        float scale = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
        final int close_height = (int) (72 * scale + 0.5f);
        container.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = container.getMeasuredHeight() - close_height;

        Animation rotate = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon);

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

    private void collapse(final View container, final ImageView icon) {
        final float scale = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
        final int actualHeight = container.getMeasuredHeight();
        int targetHeight = (int) (72 * scale + 0.5f);
        ValueAnimator va = ValueAnimator.ofInt(actualHeight, targetHeight);
        Animation rotate = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_icon_back);

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

    void setSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                Log.i("search", "SearchOnQueryTextSubmit: " + query);
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                VolleySingleton.getInstance(ListReporteFragment.this.getContext()).cancelAll();
                search = s;
                infiniteListView.clearList();
                page = 1;
                getReportes();
                return false;
            }
        });
    }

    private void solucion(final View view, final int position) {
        new MaterialDialog.Builder(this.getContext())
                .title("Solución")
                .customView(R.layout.solucion, true)
                .positiveText("Guardar")
                .negativeText("Cerrar")
                .negativeColor(ContextCompat.getColor(this.getContext(), R.color.colorReport))
                .neutralText("Fotos")
                .neutralColor(ContextCompat.getColor(this.getContext(), R.color.colorReportAccent))
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION || requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                validPermissions();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                builder.setMessage("Para que esta aplicación funcione correctamente usted debe dar permisos de acceso al GPS ¿Desea hacerlo ahora?")
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                validPermissions();
                            }
                        })
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ListReporteFragment.this.getActivity().finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    public void openPicker() {
        IPicker.setLimit(5);
        IPicker.open(this.getContext());
    }

    private void send(final MaterialDialog dialog, int position) {

        dialog.dismiss();
        View view = dialog.getCustomView();

        String nombre = ((TextView) view.findViewById(R.id.nombre)).getText().toString();
        String descripcion = ((TextView) view.findViewById(R.id.descripcion)).getText().toString();
        String reporte = String.valueOf(itemList.get(position).getId());
        String latitud = format("%s", getLastBestLocation().getLatitude());
        String longitud = format("%s", getLastBestLocation().getLongitude());

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

        final MaterialDialog loading = new MaterialDialog.Builder(this.getContext())
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
            String url = getString(R.string.url, serviceUrl);
            MultipartUploadRequest upload =
                    new MultipartUploadRequest(this.getContext(), url)
                            .setNotificationConfig(notificationConfig)
                            .setAutoDeleteFilesAfterSuccessfulUpload(false)
                            .setMaxRetries(1)
                            .addParameter("nombre", nombre)
                            .addParameter("descripcion", descripcion)
                            .addParameter("reporte", reporte)
                            .addParameter("latitud", latitud)
                            .addParameter("longitud", longitud)
                            .addParameter("fotosolucion_set-TOTAL_FORMS", format("%d", images.size()))
                            .addParameter("fotosolucion_set-INITIAL_FORMS", "0")
                            .addParameter("fotosolucion_set-MIN_NUM_FORMS", "0")
                            .addParameter("fotosolucion_set-MAX_NUM_FORMS", "5");
            for (int i = 0; i < images.size(); i++) {
                String image = images.get(i);
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

    public void send(final String nombre, final String descripcion, final String reporte, final String latitud, final String longitud){
        final MaterialDialog loading = new MaterialDialog.Builder(this.getContext())
                .title("Guardando solucion")
                .content("Por favor espere")
                .progress(true, 0)
                .show();

        String serviceUrl = getString(R.string.solucion_form);
        String url = getString(R.string.url, serviceUrl);
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
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(loginRequest);
    }

    private void validPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }else if (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }else {
                checkGPS();
            }
        }else {
            checkGPS();
        }
    }

    private void checkGPS(){
        LocationManager L = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!L.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                            ListReporteFragment.this.getActivity().finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }else {
            initGPS();
        }
    }

    private void initGPS() {
        Toast.makeText(this.getContext(), "pidiendo el gps ", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            validPermissions();
            return;
        }
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onSelected(List<String> paths) {
        images = paths;
    }
}
