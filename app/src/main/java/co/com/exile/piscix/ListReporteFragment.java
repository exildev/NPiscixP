package co.com.exile.piscix;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;
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

import static java.lang.String.format;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListReporteFragment extends Fragment {

    private static final int REQUEST_CODE_PICKER = 2;
    private ArrayList<Image> images;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_list_reporte, container, false);
        setInfiniteList(fragment);
        setSearchView();
        return fragment;
    }

    void setInfiniteList(View fragment) {
        infiniteListView = (InfiniteListView) fragment.findViewById(R.id.content_list_reporte);

        itemList = new ArrayList<>();
        InfiniteListAdapter adapter = new InfiniteListAdapter<Reporte>(this.getActivity(), R.layout.reporte, itemList) {
            @Override
            public void onNewLoadRequired() {
                getClientes();
            }

            @Override
            public void onRefresh() {
                infiniteListView.clearList();
                page = 1;
                getClientes();
            }

            @Override
            public void onItemClick(int i) {
            }

            @Override
            public void onItemLongClick(int i) {
                Toast.makeText(getContext(), "long click", Toast.LENGTH_LONG).show();
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

                    convertView.setTag(holder);

                } else {
                    holder = (ListReporteActivity.ViewHolder) convertView.getTag();
                }

                Reporte reporte = itemList.get(position);
                if (reporte != null) {
                    holder.nombre.setText(reporte.getNombre());
                    holder.cierre.setText(reporte.getCierre());
                    holder.cliente.setText(reporte.getCliente());
                    holder.descripcion.setText(reporte.getDescripcion());
                    holder.fecha.setText(reporte.getFecha());
                    holder.piscina.setText(reporte.getPiscina());
                    holder.tipo.setText(reporte.getTipo_de_reporte());
                    holder.subtitle.setText(reporte.getNombre());
                    if (reporte.isEstado()) {
                        holder.subtitle.setText(R.string.estado_abierto);
                        holder.estado.setText(R.string.estado_abierto);
                    } else {
                        holder.subtitle.setText(R.string.estado_cerrado);
                        holder.estado.setText(R.string.estado_cerrado);
                        holder.chat_button.setVisibility(View.GONE);
                        holder.solution_button.setVisibility(View.GONE);
                    }

                    holder.solution_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            solucion(view, position);
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
        getClientes();
    }



    void getClientes() {
        infiniteListView.startLoading();
        String url = "http://104.236.33.228:8050/reportes/reporte/list/?page=" + page + "&search=" + search;
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
                        String cierre = Reporte.CIERRES[campo.getInt("cierre") - 1];
                        infiniteListView.addNewItem(new Reporte(id, nombre, descripcion, tipo_de_reporte, piscina, estado, fecha, cliente, cierre));
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

    public void action(View view){
        ViewGroup row = (ViewGroup) view.getParent();
        final RelativeLayout container = (RelativeLayout) row.findViewById(R.id.container);
        TextView subtitle = (TextView) row.findViewById(R.id.subtitle);
        TextView title = (TextView) row.findViewById(R.id.nombre);
        CardView icon = (CardView) row.findViewById(R.id.pedido_icon_card);
        final float scale = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
        int close_height = (int) (72 * scale + 0.5f);
        final ImageView imageDrop = (ImageView) row.findViewById(R.id.drop_image);

        if (container.getHeight() == close_height) {
            subtitle.setVisibility(View.GONE);
            title.setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
            icon.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
            expand(container, imageDrop);
        }else {
            title.setTextColor(Color.parseColor("#000000"));
            subtitle.setVisibility(View.VISIBLE);
            icon.setCardBackgroundColor(Color.parseColor("#b2b2b2"));
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

    private void collapse(final View container, final ImageView icon){
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

    void setSearchView(){
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
                VolleySingleton.getInstance(ListReporteFragment.this.getContext()).cancelAll();
                search = s;
                infiniteListView.clearList();
                page = 1;
                getClientes();
                return false;
            }
        });
    }

    private void solucion(View view, int position) {
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
                        send(dialog.getCustomView());
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
            Toast.makeText(this.getContext(), "Hola", Toast.LENGTH_SHORT).show();
        }
    }

    public void openPicker() {
        Intent intent = new Intent(this.getActivity(), ImagePickerActivity.class);

        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_FOLDER_MODE, true);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_MODE, ImagePickerActivity.MODE_MULTIPLE);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_LIMIT, 5);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_SHOW_CAMERA, true);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES, images);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_FOLDER_TITLE, "Carpetas");
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_IMAGE_TITLE, "Toque para seleccionar");
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_IMAGE_DIRECTORY, "Camera");

        startActivityForResult(intent, REQUEST_CODE_PICKER);
    }


    private void send(View view) {

        String nombre = ((TextView) view.findViewById(R.id.nombre)).getText().toString();
        String descripcion = ((TextView) view.findViewById(R.id.descripcion)).getText().toString();

        if(nombre.equals("")){
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.nombre_container);
            til.setErrorEnabled(true);
            til.setError("Debe ingresar un nombre");
            return;
        }

        if (descripcion.equals("")){
            TextInputLayout til = (TextInputLayout) view.findViewById(R.id.descripcion_container);
            til.setErrorEnabled(true);
            til.setError("Debe ingresar una descripción");
            return;
        }

        if (images.size() < 1){
            Snackbar.make(this.getActivity().findViewById(R.id.descripcion), "Debe escojer al menos una imagen", 800).show();
            return;
        }

        /*try {
            UploadNotificationConfig notificationConfig = new UploadNotificationConfig()
                    .setTitle("Subiendo reporte")
                    .setInProgressMessage("Subiendo reporte a [[UPLOAD_RATE]] ([[PROGRESS]])")
                    .setErrorMessage("Hubo un error al subir el reporte")
                    .setCompletedMessage("Subida completada exitosamente en [[ELAPSED_TIME]]")
                    .setAutoClearOnSuccess(true);

            MultipartUploadRequest upload =
                    new MultipartUploadRequest(this.getContext(), "http://104.236.33.228:8050/reportes/reporte/form/")
                            .setNotificationConfig(notificationConfig)
                            .setAutoDeleteFilesAfterSuccessfulUpload(false)
                            .setMaxRetries(1)
                            .addParameter("nombre", nombre)
                            .addParameter("descripcion", descripcion)
                            .addParameter("fotoreporte_set-TOTAL_FORMS", format("%d", images.size()))
                            .addParameter("fotoreporte_set-INITIAL_FORMS", "0")
                            .addParameter("fotoreporte_set-MIN_NUM_FORMS", "0")
                            .addParameter("fotoreporte_set-MAX_NUM_FORMS", "5");
            for (int i = 0; i < images.size(); i++) {
                Image image = images.get(i);
                upload.addFileToUpload(image.getPath(), "fotoreporte_set-" + i + "-url");
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
        }*/
    }
}
