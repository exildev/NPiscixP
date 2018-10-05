package co.com.exile.piscix;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapCasaActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleClient;
    private LocationRequest mLocationRequest;
    private Marker myPosition;

    private PlaceAutoCompleteAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_casa);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleClient.connect();

        setAutocomplete();
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
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.i("settings", "si tal");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapCasaActivity.this,
                                    12);
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleClient, this);
    }

    protected void setAutocomplete() {
        mAdapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1,
                mGoogleClient, getBounds(), null);

        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.buscar);

        textView.setAdapter(mAdapter);

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Log.i("hola", "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            // Request did not complete successfully
                            Log.e("hola", "Place query did not complete. Error: " + places.getStatus().toString());
                            places.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final Place place = places.get(0);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 14));
                        if (myPosition == null) {
                            myPosition = mMap.addMarker(new MarkerOptions()
                                    .position(place.getLatLng())
                                    .draggable(true)
                                    .title("aqui estoy"));
                        } else {
                            myPosition.setPosition(place.getLatLng());
                        }
                        myPosition.showInfoWindow();
                    }
                });

            }
        });
    }

    protected LatLngBounds getBounds() {
        return new LatLngBounds.Builder()
                .include(new LatLng(11.037286, -75.435715))
                .include(new LatLng(8.851531, -77.248459))
                .include(new LatLng(8.710382, -72.491379))
                .include(new LatLng(6.368912, -74.743577))
                .build();

    }

    public void send(View view) {
        int casa = getIntent().getIntExtra("casa", -1);
        String serviceUrl = getString(R.string.gps_casa, casa);

        final String lat = String.valueOf(myPosition.getPosition().latitude);
        final String lng = String.valueOf(myPosition.getPosition().longitude);

        String url = getUrl(serviceUrl);

        Log.i("url", url);

        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent data = new Intent();
                        data.putExtra("response", response);
                        data.putExtra("status", 200);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err = new String(error.networkResponse.data);
                        Intent data = new Intent();
                        data.putExtra("response", err);
                        data.putExtra("status", error.networkResponse.statusCode);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("latitud", lat);
                params.put("longitud", lng);
                return params;
            }
        };
        loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng cartagena = new LatLng(10.400196, -75.502797);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cartagena, 12));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapClickListener(this);

        if (myPosition == null) {
            myPosition = mMap.addMarker(new MarkerOptions()
                    .position(cartagena)
                    .draggable(true)
                    .title("aqui estoy"));
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdates();
                fab.setColorFilter(ContextCompat.getColor(MapCasaActivity.this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            }
        });

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
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14));
        if (myPosition == null) {
            myPosition = mMap.addMarker(new MarkerOptions()
                    .position(myLocation)
                    .draggable(true)
                    .title("aqui estoy"));
        } else {
            myPosition.setPosition(myLocation);
        }
        myPosition.showInfoWindow();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        stopLocationUpdates();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setColorFilter(ContextCompat.getColor(MapCasaActivity.this, R.color.grey), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }

    @Override
    public void onMapClick(LatLng latLng) {
        stopLocationUpdates();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setColorFilter(ContextCompat.getColor(MapCasaActivity.this, R.color.grey), PorterDuff.Mode.SRC_IN);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        if (myPosition == null) {
            myPosition = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("aqui estoy"));
        } else {
            myPosition.setPosition(latLng);
        }
        myPosition.showInfoWindow();
    }
}
