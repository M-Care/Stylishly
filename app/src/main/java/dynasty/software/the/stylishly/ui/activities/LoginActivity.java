package dynasty.software.the.stylishly.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.edt_username_layout_login)
    MaterialEditText usernameEditText;
    @BindView(R.id.edt_password_layout_login)
    MaterialEditText passwordEditText;

    private ProgressDialog progressDialog;
    private List<String> permissions = Arrays.asList("public_profile", "email");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
    }

    @OnClick(R.id.btn_sign_in_layout_login) public void onSignInClick() {

        String username = Util.textOf(usernameEditText);
        String password = Util.textOf(passwordEditText);

        if (username.length() < 3) {
            usernameEditText.setError("Invalid username.");
            return;
        }
        if (password.length() < 4) {
            passwordEditText.setError("Invalid password. Too short");
            return;
        }

        Util.hideKeyboard(this);

        progressDialog.show();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {

                progressDialog.cancel();
                if (e != null) {
                    L.wtf(e);

                    if (e.getCode() != ParseException.CONNECTION_FAILED) {
                        snack(e.getMessage());
                    }else {
                        snack("Failed to connect to server. Please retry");
                    }
                    return;
                }

                snack("Login Success!. Welcome back " + user.getUsername());
                SyncActivity.start(LoginActivity.this);
            }
        });
    }

    @OnClick(R.id.btn_close_login_layout) public void onCloseClick() {
        finish();
    }

    @OnClick(R.id.btn_create_account_layout_login) public void onCreateAccountClick() {
        startActivity(new Intent(this, CreateAccountActivity.class));
    }

    @OnClick(R.id.btn_continue_with_facebook_layout_login) public void onFacebookLogin() {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    snack("Failed to complete facebook login. Please retry");
                    return;
                }

                if (parseUser == null) {
                    //cancelled
                    L.fine("Cancelled");
                }else if (parseUser.isNew()) {
                    getDetailsFromFb();
                    L.fine("New Users");
                }else {
                    L.fine("Known user!");

                    SyncActivity.start(LoginActivity.this);
                }
            }
        });
    }

    @OnClick(R.id.btn_continue_with_twitter_layout_login) public void onTwitterLogin() {
        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    snack("Failed to login with Twitter. Please retry");
                    return;
                }

                if (parseUser == null) {
                    //Reg cancelled
                }else if (parseUser.isNew()) {

                    parseUser.setUsername(ParseTwitterUtils.getTwitter().getScreenName());
                    parseUser.saveEventually();


                    SyncActivity.start(LoginActivity.this);
                }else {
                    L.fine("User => " + parseUser.getUsername());

                    SyncActivity.start(LoginActivity.this);
                }
            }
        });
    }
    public void getDetailsFromFb() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                progressDialog.cancel();

                if (object != null) {

                    try {
                        ParseUser parseUser = ParseUser.getCurrentUser();
                        if (parseUser != null) {
                            parseUser.setUsername(object.has("name") ? object.getString("name") : "");
                            parseUser.setEmail(object.has("email") ? object.getString("email") : "");

                            L.fine("Logged In ==> " + parseUser.getUsername());
                            parseUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e != null) {
                                        L.wtf(e);
                                        return;
                                    }
                                    SyncActivity.start(LoginActivity.this);
                                }
                            });
                        }
                    }catch (Exception e) {
                        L.wtf(e);
                    }
                }
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("fields","name,email");
        request.setParameters(bundle);
        request.executeAsync();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

}
