package co.com.exile.piscix;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private String name;

    private ArrayList<Contacto> contactos;
    private ArrayList<Piscina> piscinas;
    private ArrayList<Casa> casas;

    private RecyclerView contactoRV;
    private RecyclerView piscinaRV;
    private RecyclerView casaRV;

    private BottomSheetBehavior mBottomSheetBehavior;

    private String pJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeData();

        setContactosRV();
        setPiscinasRV();

        casaRV = (RecyclerView) findViewById(R.id.casa_rv);
        casaRV.setHasFixedSize(true);
        casaRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        RecyclerView.Adapter caAdapter = new CasaAdapter(casas);
        casaRV.swapAdapter(caAdapter, false);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    hideSheetShadow();
                }else if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    View sheet_modal = findViewById(R.id.modal_sheet);
                    if(sheet_modal.getVisibility() == View.GONE){
                        showSheetShadow();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i("slide", "slide: " + slideOffset);
            }
        });

        getCliente();
    }

    public void onBackPressed() {
        if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else{
            super.onBackPressed();
        }
    }

    private void initializeData() {
        contactos = new ArrayList<>();
        piscinas = new ArrayList<>();
        casas = new ArrayList<>();
    }

    public void back(View view) {
        finish();
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.reporte, popup.getMenu());
        popup.show();
    }

    public void openSheet(){
        final View sheet_modal = findViewById(R.id.modal_sheet);
        sheet_modal.setVisibility(View.VISIBLE);

        int colorFrom = ContextCompat.getColor(this, R.color.colorTransparent);
        final int colorTo = ContextCompat.getColor(this, R.color.colorShadow);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                sheet_modal.setBackgroundColor((int) animator.getAnimatedValue());
                if((int) animator.getAnimatedValue() == colorTo){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

        });
        colorAnimation.start();
    }

    public void closeSheet(View view){
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void showSheetShadow(){
        final View sheet_modal = findViewById(R.id.modal_sheet);
        sheet_modal.setVisibility(View.VISIBLE);
        sheet_modal.setBackgroundColor(ContextCompat.getColor(this, R.color.colorShadow));
    }

    void hideSheetShadow(){
        final View sheet_modal = findViewById(R.id.modal_sheet);
        sheet_modal.setVisibility(View.VISIBLE);

        int colorFrom = ContextCompat.getColor(this, R.color.colorShadow);
        final int colorTo = ContextCompat.getColor(this, R.color.colorTransparent);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(150); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                sheet_modal.setBackgroundColor((int) animator.getAnimatedValue());
                if ((int) animator.getAnimatedValue() == colorTo){
                    sheet_modal.setVisibility(View.GONE);
                }
            }

        });
        colorAnimation.start();
    }

    private void getCliente() {

        int id = getIntent().getIntExtra("id", -1);
        showLoading();
        String url = "http://104.236.33.228:8050/usuarios/single/cliente/" + id + "/";
        JsonObjectRequest formRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("reunion", response.toString());
                try {
                    setProfile(response.getJSONObject("cliente"));
                    setContactos(response.getJSONArray("contactos"));
                    setPiscinas(response.getJSONArray("piscinas"));
                    setCasas(response.getJSONArray("casas"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hideLoading();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Activities", error.toString());
                ProfileActivity.this.finish();
                Toast.makeText(ProfileActivity.this, "El cliente no existe", Toast.LENGTH_SHORT).show();
            }
        });
        formRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(formRequest);
    }

    void setProfile(JSONObject cliente) {
        try {
            String first_name = cliente.getString("first_name");
            String email = cliente.getString("email");
            String telefono = cliente.getString("telefono");

            TextView name = (TextView) findViewById(R.id.name);
            TextView emailtv = (TextView) findViewById(R.id.email);
            TextView phone = (TextView) findViewById(R.id.telefono);
            this.name = first_name;
            name.setText(first_name);
            emailtv.setText(email);
            if (telefono == null || telefono.equals("") || telefono.equals("null")){
                phone.setVisibility(View.GONE);
            }else{
                phone.setText(telefono);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setContactos(JSONArray contactos) {
        try {
            if (contactos.length() < 1) {
                findViewById(R.id.contacto_title).setVisibility(View.GONE);
                contactoRV.setVisibility(View.GONE);
            } else {
                for (int i = 0; i < contactos.length(); i++) {
                    JSONObject contacto = contactos.getJSONObject(i);
                    int id = contacto.getInt("id");
                    String nombre = contacto.getString("nombre");
                    String apellido = contacto.getString("apellido");
                    String telefono = contacto.getString("telefono");
                    String correo = contacto.getString("correo");
                    String relacion = contacto.getString("relacion");

                    this.contactos.add(new Contacto(id, nombre, apellido, telefono, correo, relacion));
                }
                setContactosRV();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setPiscinas(JSONArray piscinas) {
        pJSON = piscinas.toString();
        try {
            if (piscinas.length() < 1) {
                findViewById(R.id.piscina_title).setVisibility(View.GONE);
                piscinaRV.setVisibility(View.GONE);
            } else {
                for (int i = 0; i < piscinas.length(); i++) {
                    JSONObject piscina = piscinas.getJSONObject(i);
                    int id = piscina.getInt("id");
                    String nombre= piscina.getString("nombre");
                    double ancho = piscina.getDouble("ancho");
                    double largo = piscina.getDouble("largo");
                    double profundidad = piscina.getDouble("profundidad");
                    boolean estado = piscina.getBoolean("estado");
                    String tipo = piscina.getString("tipo__nombre");

                    this.piscinas.add(new Piscina(id, nombre, ancho, largo, profundidad, estado, tipo));
                }
                setPiscinasRV();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setCasas(JSONArray casas) {
        try {
            if (casas.length() < 1) {
                findViewById(R.id.casa_title).setVisibility(View.GONE);
                casaRV.setVisibility(View.GONE);
            } else {
                for (int i = 0; i < casas.length(); i++) {
                    JSONObject casa = casas.getJSONObject(i);
                    String direccion= casa.getString("direccion");
                    String latitud = casa.getString("latitud");
                    String longitud = casa.getString("longitud");

                    this.casas.add(new Casa(direccion, latitud, longitud));
                }
                RecyclerView.Adapter cAdapter = new CasaAdapter(this.casas);
                casaRV.swapAdapter(cAdapter, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showLoading() {
        findViewById(R.id.fab).setVisibility(View.GONE);
        final CardView loading = (CardView) findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        View modal =  findViewById(R.id.modal);
        modal.setVisibility(View.VISIBLE);

        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        final int actualHeight = 0;
        int targetHeight = (int) (50 * scale + 0.5f);
        int targetPadding = (int) (16 * scale + 0.5f);
        ValueAnimator va = ValueAnimator.ofInt(actualHeight, targetHeight);
        ValueAnimator va2 = ValueAnimator.ofInt(0, targetPadding);

        va.setDuration(200);
        va2.setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                loading.getLayoutParams().height = value;
                loading.getLayoutParams().width = value;
                loading.requestLayout();
            }
        });
        va2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) loading.getLayoutParams();
                layoutParams.setMargins(value, value, value, value);
                loading.requestLayout();
            }
        });
        va.start();
        va2.start();
    }

    private void hideLoading() {

        findViewById(R.id.fab).setVisibility(View.VISIBLE);

        final CardView loading = (CardView) findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        View modal =  findViewById(R.id.modal);
        modal.setVisibility(View.GONE);

        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        final int targetHeight = 0;
        int actualHeight = (int) (50 * scale + 0.5f);
        int actualPadding = (int) (16 * scale + 0.5f);
        ValueAnimator va = ValueAnimator.ofInt(actualHeight, targetHeight);
        ValueAnimator va2 = ValueAnimator.ofInt(actualPadding, 0);

        va.setDuration(200);
        va2.setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                loading.getLayoutParams().height = value;
                loading.getLayoutParams().width = value;
                loading.requestLayout();
            }
        });
        va2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) loading.getLayoutParams();
                layoutParams.setMargins(value, value, value, value);
                loading.requestLayout();
            }
        });
        va.start();
        va2.start();
    }

    public void setContactosRV() {
        contactoRV = (RecyclerView) findViewById(R.id.contacto_rv);
        contactoRV.setHasFixedSize(true);
        contactoRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        RecyclerView.Adapter cAdapter = new ContactoAdapter(contactos, new RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                openSheet();
                Fragment fragment = ContactoFragment.newInstance(contactos.get(position));
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_sheet, fragment)
                        .commit();
            }
        });
        contactoRV.swapAdapter(cAdapter, false);
    }

    public void setPiscinasRV(){
        piscinaRV = (RecyclerView) findViewById(R.id.piscina_rv);
        piscinaRV.setHasFixedSize(true);
        piscinaRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        RecyclerView.Adapter pAdapter = new PiscinaAdapter(piscinas, new RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                openSheet();
                Fragment fragment = PiscinaFragment.newInstance(piscinas.get(position));
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_sheet, fragment)
                        .commit();
            }
        });
        piscinaRV.swapAdapter(pAdapter, false);
    }

    public void launchCliente(View view){
        Intent intent = new Intent(this, ReporteActivity.class);
        intent.putExtra("piscinas", pJSON);
        intent.putExtra("name", name);
        startActivity(intent);
    }
}
