package co.com.exile.piscix;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapRutaActivity extends BaseActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private int page = 1;
    private int piscinero;
    ArrayList<LatLng> waypoints;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark, R.color.colorPrimary, R.color.tealPrimary, R.color.colorAccent, R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_ruta);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        waypoints = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        piscinero = getIntent().getIntExtra("piscinero", -1);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng cartagena = new LatLng(10.400173, -75.5784663);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cartagena, 10));
        loadItems();
    }

    private void loadItems() {
        String serviceUrl = getString(R.string.list_asignaciones_piscinero, piscinero, page);
        String url = getUrl(serviceUrl);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray object_list = response.getJSONArray("object_list");
                    for (int i = 0; i < object_list.length(); i++) {
                        JSONObject campo = object_list.getJSONObject(i);
                        if (!campo.get("latitud").equals(null) && !campo.get("longitud").equals(null)) {
                            double lat = campo.getDouble("latitud");
                            double lng = campo.getDouble("longitud");
                            LatLng casa = new LatLng(lat, lng);
                            waypoints.add(casa);
                            String cliente = campo.getString("nombreCF") + " " + campo.getString("nombreCL");
                            mMap.addMarker(new MarkerOptions().position(casa).title(cliente));
                        }
                    }
                    if (response.has("next")) {
                        page = response.getInt("next");
                        loadItems();
                    } else if (waypoints.size() > 1) {
                        setRoute();
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

    private void setRoute() {
        Log.i("waypoints", waypoints.size() + "");
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(waypoints)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
        LatLng start = waypoints.get(0);
        LatLng end = waypoints.get(waypoints.size() - 1);

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (LatLng point : waypoints) {
            bounds.include(point);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.build().getCenter(), 11));

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(ContextCompat.getColor(this, COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        mMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_circle_24dp));
        mMap.addMarker(options);
    }

    @Override
    public void onRoutingCancelled() {

    }
}
