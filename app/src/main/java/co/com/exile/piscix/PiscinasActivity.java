package co.com.exile.piscix;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.com.exile.piscix.models.PiscinaAsignacion;

public class PiscinasActivity extends AppCompatActivity {

    private int piscinero;
    private InfiniteListView infiniteListView;
    private ArrayList<PiscinaAsignacion> itemList;
    private int page = 1;
    private String search = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piscinas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        piscinero = getIntent().getIntExtra("piscinero", -1);

        setInfiniteList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_reporte, menu);
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
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
        infiniteListView = (InfiniteListView) findViewById(R.id.content_piscinas);

        itemList = new ArrayList<>();
        InfiniteListAdapter adapter = new InfiniteListAdapter<PiscinaAsignacion>(this, R.layout.piscina, itemList) {
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
                    convertView = getLayoutInflater().inflate(R.layout.piscina, parent, false);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.cliente = (TextView) convertView.findViewById(R.id.cliente);
                    holder.medidas = (TextView) convertView.findViewById(R.id.medidas);
                    holder.asignacion = (Switch) convertView.findViewById(R.id.asignacion);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final PiscinaAsignacion piscina = itemList.get(position);
                if (piscina != null) {
                    holder.title.setText(getString(R.string.piscina_title, piscina.getNombre(), piscina.getTipo()));
                    holder.cliente.setText(piscina.getCliente());
                    String medidas = getString(R.string.piscina_medidas, piscina.getAncho(), piscina.getLargo(), piscina.getProfundidad());
                    holder.medidas.setText(medidas);
                    holder.asignacion.setOnCheckedChangeListener(null);
                    holder.asignacion.setChecked(piscina.isAsignacion());

                    holder.asignacion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b != piscina.isAsignacion()) {
                                send(piscina, b, compoundButton);
                            }
                        }
                    });
                }

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
        getReportes();
    }

    void getReportes() {
        infiniteListView.startLoading();
        String serviceUrl = getString(R.string.list_piscinas, piscinero, page, search);
        String url = getString(R.string.url, serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
                        String tipo = campo.getString("tipo__nombre");
                        double largo = campo.getDouble("largo");
                        double ancho = campo.getDouble("ancho");
                        double profundidad = campo.getDouble("profundidad");
                        boolean estado = campo.getBoolean("estado");
                        String cliente = campo.getString("casa__cliente__first_name") + " " + campo.getString("casa__cliente__last_name");
                        boolean asignacion = campo.getBoolean("asignacion");

                        infiniteListView.addNewItem(new PiscinaAsignacion(id, nombre, tipo, ancho, largo, profundidad, estado, cliente, asignacion));
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

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    static class ViewHolder {
        TextView title;
        TextView cliente;
        TextView medidas;
        Switch asignacion;
    }

    void send(final PiscinaAsignacion piscina, final boolean estado, final CompoundButton compoundButton) {
        final MaterialDialog loading = new MaterialDialog.Builder(this)
                .title(R.string.loadin_modal_title)
                .content(R.string.loadin_modal_content)
                .progress(true, 0)
                .show();
        String serviceUrl = getString(R.string.asigancion_form);
        String url = getString(R.string.url, serviceUrl);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        piscina.setAsignacion(estado);
                        Snackbar.make(findViewById(R.id.content_piscinas), R.string.success_response, 800).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("login", error.toString());
                        Log.e("error", new String(error.networkResponse.data));
                        loading.dismiss();
                        compoundButton.setChecked(!estado);
                        Snackbar.make(findViewById(R.id.content_piscinas), "Hubo un error al realizar la operacion", 800).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("piscinero", String.valueOf(piscinero));
                params.put("piscina", String.valueOf(piscina.getId()));
                if (estado) {
                    params.put("asigna", "True");
                } else {
                    params.put("asigna", "False");
                }
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
