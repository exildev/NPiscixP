package co.com.exile.piscix;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import co.com.exile.piscix.models.User;

public class LoginActivity extends BaseActivity {

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

        String url = getUrl(serviceUrl);
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
                        returnToLogin();
                        if (error.networkResponse != null && (error.networkResponse.statusCode == 404 || error.networkResponse.statusCode == 400)) {
                            Snackbar.make(findViewById(R.id.main_container), "usuario y/o contraseña incorrecta", 800).show();
                        } else {
                            final CardView container = (CardView) findViewById(R.id.error_container);
                            container.setVisibility(View.VISIBLE);
                            VolleySingleton.manageError(LoginActivity.this, error, container, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    container.setVisibility(View.GONE);
                                    login(username, password, piscinero);
                                }
                            });
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() {
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
        finish();
    }

    private void showLoading() {
        final TextInputLayout username = (TextInputLayout) findViewById(R.id.username_container);
        final TextInputLayout password = (TextInputLayout) findViewById(R.id.password_container);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        Button send = (Button) findViewById(R.id.send_button);
        username.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        radioGroup.setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    private void returnToLogin(){
        TextInputLayout username = (TextInputLayout) findViewById(R.id.username_container);
        TextInputLayout password = (TextInputLayout) findViewById(R.id.password_container);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        Button send = (Button) findViewById(R.id.send_button);
        username.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        send.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.VISIBLE);
    }
}
