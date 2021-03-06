package co.com.exile.piscix;


import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.com.exile.piscix.models.Solucion;
import co.com.exile.piscix.notix.Notix;
import co.com.exile.piscix.notix.NotixFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class SolucionesFragment extends BaseFragment implements OnSearchListener {

    private InfiniteListView infiniteListView;
    private String search = "";
    private ArrayList<Solucion> itemList;
    private int page;
    private Notix notix;

    private ArrayList<Integer> newItems;


    public SolucionesFragment() {
        page = 1;
        // Required empty public constructor
        notix = NotixFactory.buildNotix(this.getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_soluciones, container, false);
        visitMessages();
        setInfiniteList(fragment);
        return fragment;
    }

    public static SolucionesFragment SolucionesFragmentInstance() {
        // Required empty public constructor
        return new SolucionesFragment();
    }

    void setInfiniteList(View fragment) {
        infiniteListView = (InfiniteListView) fragment.findViewById(R.id.content_list_solucion);

        itemList = new ArrayList<>();
        InfiniteListAdapter adapter = new InfiniteListAdapter<Solucion>(this.getActivity(), R.layout.soluciones, itemList) {
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
                Toast.makeText(getContext(), "long click", Toast.LENGTH_LONG).show();
            }

            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.soluciones, parent, false);
                    holder = new ViewHolder();
                    holder.nombre = (TextView) convertView.findViewById(R.id.nombre);
                    holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
                    holder.reporte = (TextView) convertView.findViewById(R.id.reporte);
                    holder.cliente = (TextView) convertView.findViewById(R.id.cliente);
                    holder.descripcion = (TextView) convertView.findViewById(R.id.descripcion);
                    holder.fecha = (TextView) convertView.findViewById(R.id.fecha);
                    holder.photos_button = (Button) convertView.findViewById(R.id.photos_button);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Solucion solucion = itemList.get(position);
                if (solucion != null) {
                    holder.nombre.setText(solucion.getNombre());
                    holder.subtitle.setText(solucion.getCliente() + "\n" + solucion.getFecha());
                    holder.reporte.setText(solucion.getReporte());
                    holder.cliente.setText(solucion.getCliente());
                    holder.descripcion.setText(solucion.getDescripcion());
                    holder.fecha.setText(solucion.getFecha());

                    holder.photos_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            initGallery(position);
                        }
                    });

                    if (newItems.indexOf(solucion.getId()) > -1) {
                        holder.nombre.setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorAccent));
                        newItems.remove(newItems.indexOf(solucion.getId()));
                    } else {
                        holder.nombre.setTextColor(Color.parseColor("#000000"));
                    }
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

    void getReportes() {
        infiniteListView.startLoading();

        String serviceUrl = getString(R.string.list_mantenimiento, page, search);
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
                        int clienteId = campo.getInt("cliente_id");
                        String cliente = campo.getString("nombreC") + " " + campo.getString("apellidosC");
                        String descripcion = campo.getString("descripcion");
                        String fecha = campo.getString("fecha");
                        String nombre = campo.getString("nombre");
                        String reporte = campo.getString("reporte__nombre");
                        String user = campo.getString("user");

                        infiniteListView.addNewItem(new Solucion(id, clienteId, cliente, descripcion, fecha, nombre, reporte, user));
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
                View main = getView();
                assert main != null;
                CardView container = (CardView) main.findViewById(R.id.error_container);
                VolleySingleton.manageError(getContext(), error, container, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("rety", "rety");
                    }
                });
                Log.e("Activities", error.toString());
            }
        });

        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(reportesRequest);
    }

    private void initGallery(int position){
        String serviceUrl = getString(R.string.foto_mantenimiento);
        String url = getUrl(serviceUrl);
        int id = itemList.get(position).getId();
        Intent intent = new Intent(this.getActivity(), GalleryActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    static class ViewHolder {
        TextView nombre;
        TextView subtitle;
        TextView cliente;
        TextView fecha;
        TextView reporte;
        TextView descripcion;
        TextView user;
        Button photos_button;
    }

    private void visitMessages() {
        ArrayList<String> messages = new ArrayList<>();
        newItems = new ArrayList<>();

        for (JSONObject notification : NotixFactory.notifications) {
            try {
                JSONObject data = notification.getJSONObject("data");
                String tipo = data.getString("tipo");
                if (tipo.equals("Solucion")) {
                    int solucion_id = data.getInt("solucion_id");
                    newItems.add(solucion_id);
                    String id = notification.getString("_id");
                    messages.add(id);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        notix.visitMessages(messages);
    }

    @Override
    public void onSearch(String search) {
        this.search = search;
        infiniteListView.clearList();
        page = 1;
        getReportes();
    }
}
