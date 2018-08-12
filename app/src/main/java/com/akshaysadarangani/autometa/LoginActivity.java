package com.akshaysadarangani.autometa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static boolean FACEBOOK_FLAG = false;

    // TODO - replace the SITE KEY with yours
    private static final String SAFETY_NET_API_SITE_KEY = "6LcTdV8UAAAAAFL8awCTvtx4JXKxWyjIUIWkm478";

    // TODO - replace the SERVER URL with yours
    private static final String URL_VERIFY_ON_SERVER = "https://autometa-app.herokuapp.com/recaptcha_verification.php";


    private FirebaseAuth mAuth;
    private DatabaseReference mFirebaseDatabase;
    public FirebaseDatabase mFirebaseInstance;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;
    private LoginButton loginButton;
    private SignInButton signInButton;
    private ProgressBar progressBar;
    private String userID, externalUrl;
    private static final int RC_SIGN_IN = 9001;
    private PrefManager prefManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        setContentView(R.layout.activity_login);
        externalUrl = null;
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type))
                externalUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        // Making notification bar transparent and hide soft keys
        //hideUI();

        prefManager = new PrefManager(this);
        progressBar = findViewById(R.id.progressBar);
        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        // Set the dimensions of the sign-in button.
        signInButton = findViewById(R.id.sign_in_button);
        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");
        // [END initialize_auth]

        // [START initialize_fblogin]
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Log.d(TAG, "facebook:onSuccess:" + loginResult);
                loginButton.setVisibility(View.GONE);
                signInButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                //Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                // Log.d(TAG, "facebook:onError", error);
                progressBar.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Sign in error!", Toast.LENGTH_SHORT).show();

            }
        });
        // [END initialize_fblogin]

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void hideUI() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            progressBar.setVisibility(View.GONE);
            if(prefManager.devicePaired())
                switchIntent(currentUser, externalUrl);
            else
                pairDevice(currentUser);
            finish();
        }
    }
    // [END on_start_check_user]

    // [START on_activity_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                assert account != null;
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                Toast.makeText(getApplicationContext(), "Sign in error!", Toast.LENGTH_LONG).show();
                // [END_EXCLUDE]
            }
        }

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    // [END on_activity_result]

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        loginButton.setVisibility(View.GONE);
        signInButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            userID = user.getUid();
                            progressBar.setVisibility(View.GONE);
                            User u = new User(user.getDisplayName(), user.getEmail());
                            mFirebaseDatabase.child(userID).setValue(u);
                            pairDevice(user);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            loginButton.setVisibility(View.VISIBLE);
                            signInButton.setVisibility(View.VISIBLE);
                        }

                        // [START_EXCLUDE]
                        progressBar.setVisibility(View.GONE);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken accessToken) {

        // Log.d(TAG, "handleFacebookAccessToken:" + accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //  Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            userID = user.getUid();
                            progressBar.setVisibility(View.GONE);
                            User u = new User(user.getDisplayName(), user.getEmail());
                            mFirebaseDatabase.child(userID).setValue(u);
                            pairDevice(user);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginActivity", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    // [END auth_with_facebook]

    public void switchIntent(FirebaseUser currentUser, String extra) {
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        myIntent.putExtra("userName", currentUser.getDisplayName());
        myIntent.putExtra("uid", currentUser.getUid());
        myIntent.putExtra("extra", extra);
        LoginActivity.this.startActivity(myIntent);
    }

    public void pairDevice(FirebaseUser user) {
        Intent myIntent = new Intent(LoginActivity.this, TriggersActivity.class);
        myIntent.putExtra("userName", user.getDisplayName());
        myIntent.putExtra("uid", user.getUid());
        LoginActivity.this.startActivity(myIntent);
    }

    @Override
    public void onClick(View view) {
       /* SafetyNet.getClient(this).verifyWithRecaptcha("6LcTdV8UAAAAAFL8awCTvtx4JXKxWyjIUIWkm478")
                .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        if (!response.getTokenResult().isEmpty()) {
                            handleSiteVerify(response.getTokenResult());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.d(TAG, "Error message: " +
                                    CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                        } else {
                            Log.d(TAG, "Unknown type of error: " + e.getMessage());
                        }
                    }
                });*/
        int i = view.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        //Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Verifying the captcha token on the server
     * Post param: recaptcha-response
     * Server makes call to https://www.google.com/recaptcha/api/siteverify
     * with SECRET Key and Captcha token
     */
    public void handleSiteVerify(final String token) {
        Log.d(TAG, "Captcha Token" + token);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_VERIFY_ON_SERVER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    String message = jsonObject.getString("message");

                    if (success) {
                        // Congrats! captcha verified successfully on server
                        // TODO - submit the feedback to your server
                        signIn();
                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("recaptcha-response", token);

                return params;
            }
        };

        MyApplication.getInstance().addToRequestQueue(strReq);
    }
}
