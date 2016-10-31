package co.com.exile.piscix;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import co.com.exile.piscix.models.Planilla;

public class RutaActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ruta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
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
                        Log.i("espera", planilla.getEspera() + "");
                        holder.title.setText("Piscina " + planilla.getNombreP() + ", tipo " + planilla.getTipo());
                        holder.cliente.setText(planilla.getNombreCF() + " " + planilla.getNombreCL());
                        holder.medidas.setText(planilla.getProfundidad() + "m alto, " + planilla.getAncho() + "m ancho, " + planilla.getLargo() + "m largo");

                        if (planilla.getPlanilla() != null && (planilla.getSalida() != null && planilla.getSalida()) && (planilla.getEspera() == null || !planilla.getEspera())) {
                            holder.action_image.setImageResource(R.drawable.ic_done_all_24dp);
                        } else if (planilla.getPlanilla() != null && (planilla.getSalida() == null || !planilla.getSalida()) && (planilla.getEspera() == null || !planilla.getEspera())) {
                            holder.action_image.setImageResource(R.drawable.ic_done_24dp);
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
            String url = "http://104.236.33.228:8050/actividades/planilladiaria/pendiente/list/?page=" + page;
            int section = getArguments().getInt(ARG_SECTION_NUMBER);
            if (section == 1) {
                url = "http://104.236.33.228:8050/usuarios/service/list/asignaciones/?asigna=True&page=" + page;
            }
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
}
