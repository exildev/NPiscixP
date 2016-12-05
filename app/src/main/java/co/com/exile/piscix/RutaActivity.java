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
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.softw4re.views.InfiniteListAdapter;
import com.softw4re.views.InfiniteListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.com.exile.piscix.models.Planilla;
import co.com.exile.piscix.notix.Notix;
import co.com.exile.piscix.notix.NotixFactory;
import co.com.exile.piscix.notix.onNotixListener;

public class RutaActivity extends AppCompatActivity implements onNotixListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Notix notix;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        notix = NotixFactory.buildNotix(this);
        notix.setNotixListener(this);
        visitMessages();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
        private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
        LocationManager locationManager;

        private static final int PLANILLA_RESULT = 1;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private InfiniteListView infiniteListView;
        private ArrayList<Planilla> itemList;
        private int page = 1;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_ruta, container, false);
            setInfiniteList(rootView);
            validPermissions();
            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            Log.i("result", requestCode + "");
            if (requestCode == PLANILLA_RESULT && resultCode == RESULT_OK) {
                int status = data.getIntExtra("status", -1);
                String response = data.getStringExtra("response");
                if (status == 200) {
                    Snackbar.make(getActivity().findViewById(R.id.container), "Planilla registrada con exito", 800).show();
                    page = 1;
                    infiniteListView.clearList();
                    getReportes();
                }
                Log.i("status", status + "");
                Log.i("response", response);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        void setInfiniteList(View fragment) {
            infiniteListView = (InfiniteListView) fragment.findViewById(R.id.content_list_planilla);

            itemList = new ArrayList<>();
            InfiniteListAdapter adapter = new InfiniteListAdapter<Planilla>(this.getActivity(), R.layout.ruta, itemList) {
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
                        int section = getArguments().getInt(ARG_SECTION_NUMBER);
                        if (section == 1) {
                            convertView = getActivity().getLayoutInflater().inflate(R.layout.ruta, parent, false);
                        } else {
                            convertView = getActivity().getLayoutInflater().inflate(R.layout.ruta_pendientes, parent, false);
                        }
                        holder = new ViewHolder();
                        holder.title = (TextView) convertView.findViewById(R.id.title);
                        holder.cliente = (TextView) convertView.findViewById(R.id.cliente);
                        holder.medidas = (TextView) convertView.findViewById(R.id.medidas);
                        holder.info_btn = (CardView) convertView.findViewById(R.id.info_btn);
                        holder.action_image = (ImageView) convertView.findViewById(R.id.action_image);

                        convertView.setTag(holder);

                    } else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    final Planilla planilla = itemList.get(position);
                    if (planilla != null) {
                        holder.title.setText("Piscina " + planilla.getNombreP() + ", tipo " + planilla.getTipo());
                        holder.cliente.setText(planilla.getNombreCF() + " " + planilla.getNombreCL());
                        holder.medidas.setText(planilla.getProfundidad() + "m alto, " + planilla.getAncho() + "m ancho, " + planilla.getLargo() + "m largo");

                        if (planilla.getPlanilla() != null && (planilla.getSalida() != null && planilla.getSalida()) && (planilla.getEspera() == null || !planilla.getEspera())) {
                            holder.action_image.setImageResource(R.drawable.ic_done_all_24dp);
                        } else if (planilla.getPlanilla() != null && (planilla.getSalida() == null || !planilla.getSalida()) && (planilla.getEspera() == null || !planilla.getEspera())) {
                            holder.action_image.setImageResource(R.drawable.ic_done_24dp);
                            holder.info_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    saveSalida(planilla);
                                }
                            });
                        } else if (planilla.getPlanilla() != null && (planilla.getSalida() == null || !planilla.getSalida()) && (planilla.getEspera() != null && planilla.getEspera())) {
                            holder.action_image.setImageResource(R.drawable.ic_edit_24dp);
                            holder.info_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i("piscina", planilla.getPiscina() + "");
                                    Log.i("planilla", planilla.getPlanilla() + "");
                                    Intent intent = new Intent(getActivity(), PlanillaActivity.class);
                                    intent.putExtra("piscina", planilla.getPiscina());
                                    intent.putExtra("planilla", planilla.getPlanilla());
                                    PlaceholderFragment.this.startActivityForResult(intent, PLANILLA_RESULT);
                                }
                            });
                        } else if (planilla.getPlanilla() == null) {
                            holder.action_image.setImageResource(R.drawable.ic_content_paste_24dp);
                            holder.info_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i("piscina", planilla.getPiscina() + "");
                                    Intent intent = new Intent(getActivity(), PlanillaActivity.class);
                                    intent.putExtra("piscina", planilla.getPiscina());
                                    PlaceholderFragment.this.startActivityForResult(intent, PLANILLA_RESULT);
                                }
                            });
                        }

                    }

                    return convertView;
                }
            };

            infiniteListView.setAdapter(adapter);
            getReportes();
        }

        void getReportes() {
            infiniteListView.startLoading();
            String serviceUrl = getString(R.string.planillas_pendientes, page);
            int section = getArguments().getInt(ARG_SECTION_NUMBER);
            if (section == 1) {
                serviceUrl = getString(R.string.asignaciones, page);
            }
            String url = getString(R.string.url, serviceUrl);
            JsonObjectRequest reportesRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        infiniteListView.stopLoading();
                        JSONArray object_list = response.getJSONArray("object_list");
                        if (response.has("next")) {
                            page = response.getInt("next");
                        }
                        for (int i = 0; i < object_list.length(); i++) {
                            JSONObject campo = object_list.getJSONObject(i);
                            double ancho = campo.getDouble("ancho");
                            Boolean espera = null;
                            if (campo.has("espera")) {
                                espera = campo.get("espera").equals(null) ? null : campo.getBoolean("espera");
                            }
                            double largo = campo.getDouble("largo");
                            String nombreCF = campo.getString("nombreCF");
                            String nombreCL = campo.getString("nombreCL");
                            String nombreP = campo.getString("nombreP");
                            int piscina = campo.getInt("piscina");
                            int piscinero_id = campo.getInt("piscinero_id");
                            Integer planilla = campo.get("planilla").equals(null) ? null : campo.getInt("planilla");
                            double profundidad = campo.getDouble("profundidad");
                            Boolean salida = campo.get("salida").equals(null) ? null : campo.getBoolean("salida");

                            String tipo = campo.getString("tipo");
                            Integer orden = null;
                            if (campo.has("orden")) {
                                orden = campo.get("orden").equals(null) ? null : campo.getInt("orden");
                            }
                            Integer id = null;
                            if (campo.has("id")) {
                                id = campo.get("id").equals(null) ? null : campo.getInt("id");
                            }
                            Double latitud = null;
                            if (campo.has("latitud")) {
                                latitud = campo.get("latitud").equals(null) ? null : campo.getDouble("latitud");
                            }
                            Double longitud = null;
                            if (campo.has("longitud")) {
                                longitud = campo.get("longitud").equals(null) ? null : campo.getDouble("longitud");
                            }
                            infiniteListView.addNewItem(new Planilla(ancho, espera, largo, nombreCF, nombreCL, nombreP, piscina, piscinero_id, planilla, profundidad, salida, tipo, orden, id, latitud, longitud));
                        }
                        Log.i("count", response.getInt("num_rows") + "");
                        if (response.has("count")) {
                            int count = response.getInt("count");

                            if (itemList.size() == count) {
                                infiniteListView.hasMore(false);
                            } else {
                                infiniteListView.hasMore(true);
                            }
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

        void saveSalida(final Planilla planilla) {
            new MaterialDialog.Builder(this.getContext())
                    .title("Solución")
                    .content("¿Esta seguro que quiere darle salida a esta planilla?")
                    .positiveText("Guardar")
                    .negativeText("Cerrar")
                    .negativeColor(ContextCompat.getColor(this.getContext(), R.color.grey))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            salida(planilla);
                        }
                    })
                    .show();
        }

        void salida(int planillaId, final MaterialDialog loading) {
            String serviceUrl = getString(R.string.salida_planilla, planillaId);
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
                            Snackbar.make(infiniteListView, "Salida registrada con exito", 800).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loading.dismiss();
                            Log.e("solucion", new String(error.networkResponse.data));
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("salida", "on");
                    return params;
                }
            };
            loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this.getContext()).addToRequestQueue(loginRequest);
        }

        void salida(final Planilla planilla) {
            final String nombre = getString(R.string.salida_planilla_nombre, planilla.getNombreP());
            final String descripcion = getString(R.string.salida_planilla_desc, planilla.getNombreP(), planilla.getNombreCF(), planilla.getNombreCL());
            final String latitud = String.valueOf(getLastBestLocation().getLatitude());
            final String longitud = String.valueOf(getLastBestLocation().getLongitude());

            final MaterialDialog loading = new MaterialDialog.Builder(this.getContext())
                    .title(R.string.loadin_modal_title)
                    .content(R.string.loadin_modal_content)
                    .progress(true, 0)
                    .show();

            String serviceUrl = getString(R.string.reporte_informativo_form);
            String url = getString(R.string.url, serviceUrl);
            StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            salida(planilla.getPlanilla(), loading);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loading.dismiss();
                            Log.e("solucion", new String(error.networkResponse.data));
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("nombre", nombre);
                    params.put("descripcion", descripcion);
                    params.put("latitud", latitud);
                    params.put("longitud", longitud);
                    return params;
                }
            };
            loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(this.getContext()).addToRequestQueue(loginRequest);
        }

        private void validPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                } else if (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                } else {
                    checkGPS();
                }
            } else {
                checkGPS();
            }
        }

        private void checkGPS() {
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
                                PlaceholderFragment.this.getActivity().finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                initGPS();
            }
        }

        private void initGPS() {
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Ruta";
                case 1:
                    return "Pendientes";
            }
            return null;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView cliente;
        TextView medidas;
        CardView info_btn;
        ImageView action_image;
    }

    private void visitMessages() {
        ArrayList<String> messages = new ArrayList<>();

        for (JSONObject notification : NotixFactory.notifications) {
            try {
                JSONObject data = notification.getJSONObject("data");
                String tipo = data.getString("tipo");
                if (tipo.equals("Asignacion")) {
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
    public void onNotix(JSONObject data) {
        NotixFactory.buildNotification(this, data);
    }

    @Override
    public void onVisited(JSONObject data) {
        Log.i("notifications", NotixFactory.notifications.size() + "");
        try {
            JSONArray messages_id = data.getJSONArray("messages_id");
            for (int i = 0; i < messages_id.length(); i++) {
                String id = messages_id.getString(i);
                for (JSONObject notification : NotixFactory.notifications) {
                    String _id = notification.getString("_id");
                    if (id.equals(_id)) {
                        NotixFactory.notifications.remove(notification);
                        break;
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("notifications", NotixFactory.notifications.size() + "");
    }
}
