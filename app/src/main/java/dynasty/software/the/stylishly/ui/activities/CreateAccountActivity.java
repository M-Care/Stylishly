package dynasty.software.the.stylishly.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Patterns;

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
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.ui.dialogs.LoadingDialog;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class CreateAccountActivity extends BaseActivity {


    @BindView(R.id.edt_email_layout_create_account)
    MaterialEditText emailEditText;
    @BindView(R.id.edt_username_layout_create_account)
    MaterialEditText usernameEditText;
    @BindView(R.id.edt_password_layout_create_account)
    MaterialEditText passwordEditText;

    private ProgressDialog loadingDialog;
    private List<String> permissions = Arrays.asList("public_profile", "email");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_account);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
    }

    @OnClick(R.id.btn_create_account_layout_create_account) public void onCreateAccountClick() {

        String email = Util.textOf(emailEditText);
        String username = Util.textOf(usernameEditText);
        String password = Util.textOf(passwordEditText);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email address.");
            return;
        }
        if (username.length() < 3) {
            usernameEditText.setError("Invalid username. Too short");
            return;
        }
        if (password.length() < 4) {
            passwordEditText.setError("Invalid password. Too short");
            return;
        }

        ParseUser parseUser = new ParseUser();
        parseUser.setEmail(email);
        parseUser.setPassword(password);
        parseUser.setUsername(username);
        parseUser.put("bio", getString(R.string.default_bio));

        Util.hideKeyboard(this);

        loadingDialog.show();
        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {

                loadingDialog.cancel();
                if (e != null) {
                    L.wtf(e);

                    switch (e.getCode()) {
                        case ParseException.USERNAME_TAKEN:
                            snack(e.getMessage());
                            usernameEditText.setError(e.getMessage());
                            break;
                    }

                    return;
                }

                snack("Welcome to Stylish.ly!");
                SyncActivity.start(CreateAccountActivity.this);
            }
        });
    }

    @OnClick(R.id.btn_close_create_account_layout) public void onCloseActivityClick() {
        finish();
    }

    @OnClick(R.id.btn_sign_in_create_account) public void onSignInClick() {

        startActivity(new Intent(this, LoginActivity.class));
    }

    @OnClick(R.id.btn_twitter_login_create_account) public void onTwitterLoginClick() {

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


                    SyncActivity.start(CreateAccountActivity.this);
                }else {

                    L.fine("User => " + parseUser.getUsername());
                    SyncActivity.start(CreateAccountActivity.this);
                }
            }
        });
    }

    @OnClick(R.id.btn_fb_login_create_account) public void onFbLoginClick() {

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
                    SyncActivity.start(CreateAccountActivity.this);
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

                            L.fine(object.toString());

                            L.fine("Logged In ==> " + parseUser.getUsername());
                            parseUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e != null) {
                                        L.wtf(e);
                                        return;
                                    }
                                    SyncActivity.start(CreateAccountActivity.this);
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
