package co.com.exile.piscix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.exile.piscix.models.Cliente;
import co.com.exile.piscix.models.User;
import co.com.exile.piscix.notix.Notix;
import co.com.exile.piscix.notix.NotixFactory;
import co.com.exile.piscix.notix.onNotixListener;
import tarek360.animated.icons.NotificationAlertIcon;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, onNotixListener {

    private boolean inHome = true;
    private Menu mMenu;
    private Notix notix;
    private OnSearchListener searchListener;

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

        notix = NotixFactory.buildNotix(this);
        notix.setNotixListener(this);

        NotificationAlertIcon toolbarIcon = (NotificationAlertIcon) findViewById(R.id.toolbarIcon);
        toolbarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
            }
        });
        toolbarIcon.setNotificationCount(NotixFactory.notifications.size());
        toolbarIcon.setColors(Color.WHITE, Color.WHITE, ContextCompat.getColor(this, R.color.colorReport));
        toolbarIcon.startAnimation();

        setUser();
    }

    private void setSearchView(){
        MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
        myActionMenuItem.setVisible(true);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                Log.i("search", "SearchOnQueryTextSubmit: " + query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if (searchListener != null) {
                    searchListener.onSearch(s);
                }
                return false;
            }
        });
    }

    private void setUser() {
        if (getIntent().hasExtra("user")) {
            try {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View headerLayout = navigationView.getHeaderView(0);
                JSONObject user = new JSONObject(getIntent().getStringExtra("user"));
                TextView username = (TextView) headerLayout.findViewById(R.id.username_header);
                TextView email = (TextView) headerLayout.findViewById(R.id.email_header);
                username.setText(user.getString("first_name") + " " + user.getString("last_name"));
                email.setText(user.getString("email"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveTo(String action) {
        inHome = false;
        if (action.equals("Solucion")) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            SolucionesFragment fragment = SolucionesFragment.SolucionesFragmentInstance();
            searchListener = fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Soluciones");
        } else if (action.equals("informativo")) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            InformativoFragment fragment = InformativoFragment.InformativoFragmentInstance();
            searchListener = fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Informativos");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        notix.setNotixListener(this);
        NotificationAlertIcon toolbarIcon = (NotificationAlertIcon) findViewById(R.id.toolbarIcon);
        toolbarIcon.setNotificationCount(NotixFactory.notifications.size());
        toolbarIcon.setColors(Color.WHITE, Color.WHITE, ContextCompat.getColor(this, R.color.colorReport));
        toolbarIcon.startAnimation();
        super.onResume();
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
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Piscix");
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        mMenu = menu;
        if (getIntent().hasExtra("action")) {
            moveTo(getIntent().getStringExtra("action"));
            getIntent().removeExtra("action");
        }

        setSearchView();
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
            myActionMenuItem.setVisible(true);
            ClienteFragment fragment = ClienteFragment.ClienteFragmentInstance();
            searchListener = fragment;
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
            toolbar.setTitle("Piscineros");
        } else if (id == R.id.nav_rutas || id == R.id.rutas_btn) {
            startActivity(new Intent(this, RutaActivity.class));
        } else if (id == R.id.nav_recordatorio) {
            startActivity(new Intent(this, AlarmActivity.class));
        } else if (id == R.id.nav_actividades || id == R.id.actividades_btn) {
            startActivity(new Intent(this, CalendarActivity.class));
        } else if (id == R.id.nav_reportes || id == R.id.reporte_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            ListReporteFragment fragment = ListReporteFragment.listReporteFragmentInstance();
            searchListener = fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Reportes");
        } else if (id == R.id.nav_soluciones || id == R.id.soluciones_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            SolucionesFragment fragment = SolucionesFragment.SolucionesFragmentInstance();
            searchListener = fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Soluciones");
        } else if (id == R.id.nav_informativos || id == R.id.informativos_btn) {
            MenuItem myActionMenuItem = mMenu.findItem(R.id.action_search);
            myActionMenuItem.setVisible(true);
            InformativoFragment fragment = InformativoFragment.InformativoFragmentInstance();
            searchListener = fragment;
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
            String serviceUrl = getString(R.string.logout);
            String url = getString(R.string.url, serviceUrl);
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

    @Override
    public void onNotix(JSONObject data) {
        NotixFactory.buildNotification(this, data);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NotificationAlertIcon toolbarIcon = (NotificationAlertIcon) findViewById(R.id.toolbarIcon);
                toolbarIcon.setNotificationCount(NotixFactory.notifications.size());
                toolbarIcon.startAnimation();
            }
        });
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
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NotificationAlertIcon toolbarIcon = (NotificationAlertIcon) findViewById(R.id.toolbarIcon);
                toolbarIcon.setNotificationCount(NotixFactory.notifications.size());
                toolbarIcon.setColors(Color.WHITE, Color.WHITE, ContextCompat.getColor(HomeActivity.this, R.color.colorReport));
                toolbarIcon.startAnimation();
            }
        });
    }
}
