package co.com.exile.piscix;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import co.com.exile.piscix.models.User;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean inHome = true;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Fragment fragment = HomeFragment.newInstance(getIntent().getBooleanExtra("piscinero", false));
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commit();

        setMenu();
    }

    private void setMenu() {
        boolean isPiscinero = getIntent().getBooleanExtra("piscinero", false);
        if (isPiscinero) {

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!inHome){
            mMenu.findItem(R.id.action_search).setVisible(false);
            inHome = true;
            Fragment fragment = HomeFragment.newInstance(getIntent().getBooleanExtra("piscinero", false));
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        menuSelected(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void menuClicked(View view){
        int id = view.getId();
        menuSelected(id);
    }

    public void menuSelected(int id){
        inHome = false;

        if (id == R.id.nav_clientes || id == R.id.clientes_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            Log.e("menu item", myActionMenuItem.toString());
            myActionMenuItem.setVisible(true);
            SearchView searchView = (SearchView) myActionMenuItem.getActionView();
            Fragment fragment = ClienteFragment.ClienteFragmentInstance(searchView);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Clientes");
        } else if (id == R.id.piscieros_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            Log.e("menu item", myActionMenuItem.toString());
            myActionMenuItem.setVisible(true);
            SearchView searchView = (SearchView) myActionMenuItem.getActionView();
            Fragment fragment = PiscineroFragment.newInstance(searchView);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Clientes");
        } else if (id == R.id.nav_rutas || id == R.id.rutas_btn) {
            startActivity(new Intent(this, RutaActivity.class));
        } else if (id == R.id.nav_actividades || id == R.id.actividades_btn) {
            startActivity(new Intent(this, CalendarActivity.class));
        } else if (id == R.id.nav_reportes || id == R.id.reporte_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            SearchView searchView = (SearchView) myActionMenuItem.getActionView();
            Fragment fragment = ListReporteFragment.listReporteFragmentInstance(searchView);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Reportes");
        } else if (id == R.id.nav_soluciones || id == R.id.soluciones_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            SearchView searchView = (SearchView) myActionMenuItem.getActionView();
            Fragment fragment = SolucionesFragment.SolucionesFragmentInstance(searchView);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Soluciones");
        } else if (id == R.id.nav_informativos || id == R.id.informativos_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            SearchView searchView = (SearchView) myActionMenuItem.getActionView();
            Fragment fragment = InformativoFragment.InformativoFragmentInstance(searchView);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Informativos");
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            User.delete(this);
            String url = "http://104.236.33.228:8050/usuarios/logout/";
            StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Activities", error.toString());
                }
            });
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        }
    }
}
