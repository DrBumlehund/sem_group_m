package m.group.sem.projectm.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import m.group.sem.projectm.AccountHelper;
import m.group.sem.projectm.R;
import m.group.sem.projectm.Services.TipLocationService;
import m.group.sem.projectm.Services.TipNotificationService;

public class LoginActivity extends AppCompatActivity {

    private static final String tag = "LoginActivity";


    // request queue
    private RequestQueue mRequestQueue;
    private boolean mRequestRunning;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private AccountHelper mAccountHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = new Intent(this, TipNotificationService.class);
        startService(intent);
        Log.d("Mine", "try to bind from activity: ");
        Intent locationIntent = new Intent(this, TipLocationService.class);
        bindService(locationIntent, mConnection, Context.BIND_AUTO_CREATE);


        mRequestQueue = Volley.newRequestQueue(this);
        mRequestRunning = false;

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mAccountHelper = new AccountHelper();
    }

    public void logoClick(View view) {
        Toast.makeText(getApplicationContext(), "I literally stole this, and we should definitely remove this before hand-in", Toast.LENGTH_LONG).show();
    }

    public void createNewAccount(View view) {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    public void continueToMainActivity(int id, String username) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mRequestRunning) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !mAccountHelper.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!mAccountHelper.isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            // hash password
            password = mAccountHelper.hashPassword(password);

            //TODO: Remove UserLoginTask if Volley is actually async.
//            mAuthTask = new UserLoginTask(email, password, this);
//            mAuthTask.execute((Void) null);


            String url = "http://51.254.127.173:8080/api/login?username=" + username + "&password=" + password;

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            mRequestRunning = false;
                            showProgress(false);
                            int id = -1;
                            String username = "";
                            try {
                                Log.d(tag, "received id: " + response.get("id"));
                                id = response.getInt("id");
                                username = response.getString("username");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            continueToMainActivity(id, username);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mRequestRunning = false;
                            error.printStackTrace();
                            String response = new String(error.networkResponse.data);
                            showProgress(false);

                            if (response.contains("Incorrect username or password")) {
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            } else {
                                Toast.makeText(getApplicationContext(), getText(R.string.connection_err) + "\nerror code: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                            }
                        }

                    });
            mRequestRunning = true;
            mRequestQueue.add(jsObjRequest);
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        public TipLocationService mService;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("Mine", "connected activity: ");

            try {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                Log.d("Mine", "onServiceConnected activity: " + service.getClass());
                TipLocationService.TipLocationBinder binder = (TipLocationService.TipLocationBinder) service;
                mService = binder.getService();

                mService.exampleCallbackImplementation(new TipLocationService.ExampleCallbackInterface() {
                    @Override
                    public void newLocationReceived(double someeVar) {
                        Log.d("Mine", "newLocationReceived activity: " + someeVar);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Mine", "onServiceConnected activity: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("Mine", "disconnected activity: ");
        }
    };
}

