package dynasty.software.the.stylishly.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class EditProfileActivity extends BaseActivity {

    @BindView(R.id.iv_user_photo_edit_profile)
    CircleImageView circleImageView;
    @BindView(R.id.edt_username_edit_profile)
    MaterialEditText usernameEditText;
    @BindView(R.id.edt_email_edit_profile)
    MaterialEditText emailEditText;
    @BindView(R.id.edt_bio_edit_profile)
    MaterialEditText bioEditText;

    private File profilePhotoFile;
    private ParseUser currentParseUser;
    private ProgressDialog progressDialog;

    public static final int RC_SELECT_PHOTO = 12;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Edit Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        currentParseUser = ParseUser.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);

        render();
    }

    private void render() {

        String photo = "";
        ParseFile photoFile = currentParseUser.getParseFile("photo_uri");
        if (photoFile != null) {
            photo = photoFile.getUrl();
        }
        if (!photo.isEmpty()) {
            Glide.with(this).load(photo).apply(Util.requestOptions()).into(circleImageView);
        }

        String bio = currentParseUser.getString("user_bio");
        if (bio != null && !bio.isEmpty()) {
            bioEditText.setText(bio);
        }else {
            bioEditText.setText("");
            bioEditText.setHelperText(String.valueOf("Default Bio : " + getString(R.string.default_bio)));
        }

        usernameEditText.setText(currentParseUser.getUsername());
        emailEditText.setText(currentParseUser.getEmail());
    }

    @OnClick(R.id.btn_change_photo_edit_profile) public void onChangePhotoClick() {
        openImagePicker();
    }

    private void openImagePicker() {
        ImagePicker.create(this)
                .folderMode(true)
                .single()
                .toolbarFolderTitle("Select Photo")
                .toolbarImageTitle("Select Photo")
                .theme(R.style.ImagePickerTheme)
                .start(RC_SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            switch (requestCode) {
                case RC_SELECT_PHOTO:
                    Image selected = ImagePicker.getFirstImageOrNull(data);
                    String path = selected.getPath();
                    Glide.with(this)
                            .load(new File(path))
                            .into(circleImageView);

                    resizePhoto(selected);
                    break;
            }
        }
    }

    private void resizePhoto(Image selected) {

        Tiny.getInstance().source(selected.getPath())
                .asFile().compress(new FileCallback() {
            @Override
            public void callback(boolean isSuccess, String outfile, Throwable t) {
                if (t != null) {
                    L.wtf(t);
                    return;
                }

                profilePhotoFile = new File(outfile);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_save_changes:
                saveChanges();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {

        boolean changes = false;

        String bio = Util.textOf(bioEditText);
        if (!bio.isEmpty() && !bio.trim().equalsIgnoreCase(currentParseUser.getString("user_bio"))) {
            currentParseUser.put("user_bio", bio);
            changes = true;
        }
        if (profilePhotoFile != null) {
            currentParseUser.put("photo_uri", new ParseFile(profilePhotoFile));
            changes = true;
        }

        if (!changes) {
            toast("No Changes made");
            finish();
        }

        progressDialog.show();
        currentParseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                progressDialog.cancel();
                if (e != null) {
                    L.wtf(e);
                    snack("Failed to update profile. Network error. Please retry");
                    return;
                }

                toast("Profile has been updated!");
                finish();
            }
        });
    }
}
