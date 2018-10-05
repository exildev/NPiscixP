package co.com.exile.piscix;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.com.exile.piscix.models.Cliente;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClienteFragment extends BaseFragment implements OnSearchListener {
    private int page;
    private String search = "";
    private ArrayList<Cliente> itemList;
    private InfiniteListView infiniteListView;

    public ClienteFragment() {
        // Required empty public constructor
        page = 1;
    }

    public static ClienteFragment ClienteFragmentInstance() {
        // Required empty public constructor
        return new ClienteFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_cliente, container, false);
        setInfiniteList(fragment);
        setFab(fragment);
        return fragment;
    }

    void setInfiniteList(View fragment) {
        infiniteListView = (InfiniteListView) fragment.findViewById(R.id.infiniteListView);

        itemList = new ArrayList<>();
        InfiniteListAdapter adapter = new InfiniteListAdapter<Cliente>(this.getActivity(), R.layout.cliente, itemList) {

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
                int id = itemList.get(i).getId();
                launchCliente(id);
            }

            @Override
            public void onItemLongClick(int i) {
                Toast.makeText(getContext(), "long click", Toast.LENGTH_LONG).show();
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.cliente, parent, false);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.cliente);
                    holder.subtitle = (TextView) convertView.findViewById(R.id.cliente_subtitle);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Cliente cliente = itemList.get(position);
                if (cliente != null) {
                    holder.title.setText(cliente.getFirst_name() + " " + cliente.getLast_name());
                    if (cliente.isTipo() == Cliente.ES_PROPIETARIO) {
                        holder.subtitle.setText(R.string.tipo_propietario);
                    } else {
                        holder.subtitle.setText(R.string.arrendatario);
                    }
                }

                return convertView;
            }
        };

        infiniteListView.setAdapter(adapter);
        getClientes();
    }

    void setFab(View fragment) {
        FloatingActionButton fab = (FloatingActionButton) fragment.findViewById(R.id.qr_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
                    return;
                }
                initScan();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                try {
                    int cliente = Integer.parseInt(result.getContents());
                    launchCliente(cliente);
                } catch (NumberFormatException e) {
                    View view = getView();
                    assert view != null;
                    Snackbar.make(view, "Debe escanear un codigo QR valido", 1200).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void getClientes() {
        infiniteListView.startLoading();
        String serviceUrl = getString(R.string.get_clientes, page, search);
        String url = getUrl(serviceUrl);
        JsonObjectRequest clientesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
                        String first_name = campo.getString("first_name");
                        int id = campo.getInt("id");
                        String imagen = campo.getString("imagen");
                        String last_name = campo.getString("last_name");
                        boolean tipo = campo.getBoolean("tipo");
                        infiniteListView.addNewItem(new Cliente(first_name, id, imagen, last_name, tipo));
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
        VolleySingleton.getInstance(this.getActivity()).addToRequestQueue(clientesRequest);
    }

    void initScan() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    void launchCliente(int id) {
        Intent intent = new Intent(this.getContext(), ProfileActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    @Override
    public void onSearch(String search) {
        this.search = search;
        infiniteListView.clearList();
        page = 1;
        getClientes();
    }

    static class ViewHolder {
        TextView title;
        TextView subtitle;
    }

    private final int PERMISSIONS_REQUEST_CAMERA = 1;
}
