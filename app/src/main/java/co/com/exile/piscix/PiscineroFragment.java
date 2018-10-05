package co.com.exile.piscix;


import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import co.com.exile.piscix.models.Piscinero;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PiscineroFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PiscineroFragment extends BaseFragment {
    private InfiniteListView infiniteListView;
    private ArrayList<Piscinero> itemList;
    private int page = 1;
    private String search = "";
    private SearchView searchView;


    public PiscineroFragment() {
        // Required empty public constructor
    }


    public static PiscineroFragment newInstance(SearchView searchView) {
        PiscineroFragment fragment = new PiscineroFragment();
        fragment.searchView = searchView;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_piscinero, container, false);
        setInfiniteList(fragment);
        setSearchView();
        return fragment;
    }

    void setInfiniteList(View fragment) {
        infiniteListView = (InfiniteListView) fragment.findViewById(R.id.content_list_piscinero);

        itemList = new ArrayList<>();
        InfiniteListAdapter adapter = new InfiniteListAdapter<Piscinero>(this.getActivity(), R.layout.piscinero, itemList) {
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

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.piscinero, parent, false);
                    holder = new ViewHolder();
                    holder.nombre = (TextView) convertView.findViewById(R.id.nombre);
                    holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
                    holder.numero = (TextView) convertView.findViewById(R.id.numero);
                    holder.email = (TextView) convertView.findViewById(R.id.email);
                    holder.cumple = (TextView) convertView.findViewById(R.id.cumple);
                    holder.direccion = (TextView) convertView.findViewById(R.id.direccion);
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.green_button = (Button) convertView.findViewById(R.id.green_button);
                    holder.teal_button = (Button) convertView.findViewById(R.id.teal_button);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final Piscinero piscinero = itemList.get(position);
                if (piscinero != null) {
                    holder.nombre.setText(piscinero.getFirst_name() + " " + piscinero.getLast_name());
                    holder.subtitle.setText(piscinero.getDireccion());
                    holder.numero.setText(piscinero.getTelefono());
                    holder.email.setText(piscinero.getEmail());
                    holder.cumple.setText(piscinero.getFecha_nacimiento());
                    holder.direccion.setText(piscinero.getDireccion());
                    holder.teal_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(PiscineroFragment.this.getActivity(), PiscinasActivity.class);
                            intent.putExtra("piscinero", piscinero.getId());
                            startActivity(intent);
                        }
                    });
                    holder.green_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(PiscineroFragment.this.getActivity(), RutaPActivity.class);
                            intent.putExtra("piscinero", piscinero.getId());
                            startActivity(intent);
                        }
                    });


                    View phone = convertView.findViewById(R.id.button_numero);

                    phone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String uri = "tel:" + piscinero.getTelefono().trim();
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse(uri));
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

    void getReportes() {
        infiniteListView.startLoading();
        String serviceUrl = getString(R.string.list_piscineros, page, search);
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
                        String direccion = campo.getString("direccion");
                        String email = campo.getString("email");
                        String fecha_nacimiento = campo.getString("fecha_nacimiento");
                        String first_name = campo.getString("first_name");
                        int id = campo.getInt("id");
                        String imagen = campo.getString("imagen");
                        String last_name = campo.getString("last_name");
                        String telefono = campo.getString("telefono");
                        infiniteListView.addNewItem(new Piscinero(direccion, email, fecha_nacimiento, first_name, id, imagen, last_name, telefono));
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
                VolleySingleton.getInstance(PiscineroFragment.this.getContext()).cancelAll();
                search = s;
                infiniteListView.clearList();
                page = 1;
                getReportes();
                return false;
            }
        });
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

    static class ViewHolder {
        TextView nombre;
        TextView subtitle;
        TextView numero;
        TextView email;
        TextView cumple;
        TextView direccion;
        ImageView icon;
        Button green_button;
        Button teal_button;
    }

}
