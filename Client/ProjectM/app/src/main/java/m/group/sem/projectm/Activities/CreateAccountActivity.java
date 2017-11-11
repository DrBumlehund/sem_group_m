package m.group.sem.projectm.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import Model.User;
import m.group.sem.projectm.AccountHelper;
import m.group.sem.projectm.R;

/**
 * A login screen that offers login via email/password.
 */
public class CreateAccountActivity extends AppCompatActivity {

    private static String tag = "CREATE_ACCOUNT";

    private AccountHelper mAccountHelper;

    // request requestQueue
    private RequestQueue mRequestQueue;
    private boolean mRequestRunning;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private EditText mPasswordView2;
    private View mProgressView;
    private View mLoginFormView;

    // Object Mapper
    private ObjectMapper mMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mMapper = new ObjectMapper();

        mRequestQueue = Volley.newRequestQueue(this);
        mRequestRunning = false;

        // Set up the login form.
        mUsernameView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);

        mPasswordView2 = findViewById(R.id.password_check);
        mPasswordView2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptCreateAccount();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateAccount();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mAccountHelper = new AccountHelper();
    }


    public void continueToMainActivity(User user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptCreateAccount() {
        if (mRequestRunning) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password2 = mPasswordView2.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !password.equals(password2)) {
            mPasswordView2.setError(getString(R.string.create_pwd_no_match));
            focusView = mPasswordView2;
            cancel = true;
        }

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

            password = mAccountHelper.hashPassword(password);

            String url = "http://51.254.127.173:8080/api/users?username=" + username + "&password=" + password;

            StringRequest request = new StringRequest
                    (Request.Method.GET, url, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            mRequestRunning = false;
                            showProgress(false);
                            Log.d(tag, "received response: " + response);
                            try {
                                User mUser = mMapper.readValue(response, User.class);
                                continueToMainActivity(mUser);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mRequestRunning = false;
                            showProgress(false);
                            error.printStackTrace();
                            String response = new String(error.networkResponse.data);

                            if (response.contains("com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry")) {
                                mUsernameView.setError(getString(R.string.create_username_taken));
                                mUsernameView.requestFocus();
                            } else {
                                Toast.makeText(getApplicationContext(), getText(R.string.connection_err) + "\nerror code: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();

                            }
                        }

                    });
            mRequestRunning = true;
            mRequestQueue.add(request);
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

}

