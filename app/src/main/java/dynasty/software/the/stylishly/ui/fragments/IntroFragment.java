package dynasty.software.the.stylishly.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.OnClick;
import dynasty.software.the.stylishly.R;

import dynasty.software.the.stylishly.ui.activities.CreateAccountActivity;
import dynasty.software.the.stylishly.ui.activities.HomeActivity;
import dynasty.software.the.stylishly.ui.activities.LoginActivity;
import dynasty.software.the.stylishly.ui.activities.SyncActivity;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class IntroFragment extends BaseFragment {

    private List<String> permissions = Arrays.asList("public_profile", "email");

    public static IntroFragment newInstance() {

        Bundle args = new Bundle();

        IntroFragment fragment = new IntroFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_splash_fragment, container, false);
    }

    @OnClick(R.id.btn_twitter_login) public void onTwitterLoginClick() {

        ParseTwitterUtils.logIn(getActivity(), new LogInCallback() {
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

                    SyncActivity.start(getActivity());
                }else {
                    L.fine("User => " + parseUser.getUsername());
                    SyncActivity.start(getActivity());
                }
            }
        });
    }

    @OnClick(R.id.btn_fb_login) public void onFbLogin() {

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
                    SyncActivity.start(getActivity());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @OnClick(R.id.btn_join_splash_fragment) public void onJoinClick() {
        startActivity(new Intent(getActivity(), CreateAccountActivity.class));
    }

    public void getDetailsFromFb() {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
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
                                    SyncActivity.start(getActivity());
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
}
