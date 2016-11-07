package co.com.exile.piscix;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import co.com.exile.piscix.models.User;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        autoSession();
    }

    private void autoSession(){
        User user = User.get(this);
        if(user != null){
            login(user.getUsername(), user.getPassword(), user.isPiscinero());
        }
    }

    public void login(View view){
        TextInputEditText username = (TextInputEditText) findViewById(R.id.username);
        TextInputEditText password = (TextInputEditText) findViewById(R.id.password);
        RadioButton piscinero = (RadioButton) findViewById(R.id.piscinero);
        login(username.getText().toString(), password.getText().toString(), piscinero.isChecked());
    }

    public void login(final String username, final String password, final boolean piscinero) {
        String serviceUrl = getString(R.string.login_supervisor);

        if (piscinero) {
            serviceUrl = getString(R.string.login_piscinero);
        }

        String url = getString(R.string.url, serviceUrl);
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("login", response);
                        User user = new User(username, password, piscinero);
                        user.save(LoginActivity.this);
                        initHome(response, piscinero);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("login", error.toString());
                        Snackbar.make(findViewById(R.id.main_container),"usuario y/o contraseña incorrecta", 800).show();
                        returnToLogin();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        loginRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
        showLoading();
    }

    private void initHome(String user, boolean piscinero) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("piscinero", piscinero);
        startActivity(intent);
    }

    private void showLoading() {
        final TextInputLayout username = (TextInputLayout) findViewById(R.id.username_container);
        final TextInputLayout password = (TextInputLayout) findViewById(R.id.password_container);
        Button send = (Button) findViewById(R.id.send_button);
        username.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    private void returnToLogin(){
        final TextInputLayout username = (TextInputLayout) findViewById(R.id.username_container);
        final TextInputLayout password = (TextInputLayout) findViewById(R.id.password_container);
        findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        Button send = (Button) findViewById(R.id.send_button);
        username.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        send.setVisibility(View.VISIBLE);
    }
}
