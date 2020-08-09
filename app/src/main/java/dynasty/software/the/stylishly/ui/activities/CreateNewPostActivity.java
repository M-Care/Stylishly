package dynasty.software.the.stylishly.ui.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.hendraanggrian.widget.SocialTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.services.CreateNewPostService;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class CreateNewPostActivity extends BaseActivity {

    public static final int PHOTO_PICKER = 11;
    public static final String EXTRA_POST_PAYLOAD = "extra_post_payload";
    private String mTags = "", mTagsString = "";
    private File selectedPhoto;
    public static final int RC_CAMERA = 13;
    private ProgressDialog progressDialog;
    private Dialog mTagsDialog;
    private EditText tagsEditText;


    @BindView(R.id.tv_tags_new_post)
    SocialTextView tagsTextView;
    @BindView(R.id.iv_photo_new_post)
    ImageView imageView;
    @BindView(R.id.edt_say_something_new_post)
    EditText captionEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_new_post);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating post...");

        createTagsDialog();
    }

    private void createTagsDialog() {


        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_add_tags, null);
        tagsEditText = view.findViewById(R.id.edt_tags_add_tags_dialog);
        mTagsDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mTags = Util.textOf(tagsEditText);
                        L.fine("Tags ==> " + mTags);
                        processTags();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .create();

    }

    @OnClick(R.id.btn_add_photo_new_post) public void onAddPhotoClick() {
        startImagePicker();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            switch (requestCode) {
                case PHOTO_PICKER:
                    Image picked = ImagePicker.getFirstImageOrNull(data);
                    loadImage(picked);
                    break;
                case RC_CAMERA:
                    selectedPhoto = new File(data.getStringExtra(CameraActivity.PHOTO_RESULT));
                    loadImage();
                    break;
            }
        }
    }

    private void loadImage(Image picked) {

        if (picked == null)
            return;

        selectedPhoto = new File(picked.getPath());
        loadImage();
    }

    private void loadImage() {
        resizePhoto();
        Glide.with(this)
                .load(selectedPhoto)
                .apply(Util.requestOptions())
                .into(imageView);
    }

    private void resizePhoto() {

        Tiny.getInstance().source(selectedPhoto)
                .asFile().compress(new FileCallback() {
            @Override
            public void callback(boolean isSuccess, String outfile, Throwable t) {

                if (t != null) {
                    L.wtf(t);
                    return;
                }
                selectedPhoto = new File(outfile);
                L.fine("Photo resized ==> " + selectedPhoto.getAbsolutePath());
            }
        });
    }

    @OnClick(R.id.btn_add_tags_new_post) public void onAddTagClick() {

        mTagsDialog.show();
    }

    private void processTags() {

        if(mTags.isEmpty()) return;

        tagsToList();
    }

    private List<String> tagsToList() {

        List<String> strings = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        String[] split = mTags.split(",");
        for (String tag : split) {
            strings.add(tag);
            builder.append(" ").append("#")
                    .append(tag.trim());
        }

        mTagsString = builder.toString().trim();
        tagsTextView.setText(mTagsString);
        return strings;
    }

    private void startImagePicker() {

        ImagePicker.create(this)
                .folderMode(true)
                .toolbarImageTitle("Select Image")
                .toolbarFolderTitle("Select Image")
                .single()
                .theme(R.style.ImagePickerTheme)
                .start(PHOTO_PICKER);

    }

    @OnClick(R.id.btn_create_post_new_post) public void onCreateNewPostClick() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("caption", Util.textOf(captionEditText));
            jsonObject.put("photo_uri", selectedPhoto.getAbsolutePath());
            jsonObject.put("tags", mTags);
            jsonObject.put("tag_string", mTagsString);
        }catch (Exception e) {
            L.wtf(e);
        }

        Intent intent = new Intent(this, CreateNewPostService.class);
        intent.putExtra(EXTRA_POST_PAYLOAD, jsonObject.toString());
        startService(intent);

        progressDialog.show();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra(CreateNewPostService.KEY_MESSAGE);
            String status = intent.getStringExtra(CreateNewPostService.KEY_STATUS);
            snack(message);

            progressDialog.cancel();
            if (status != null && status.equalsIgnoreCase("success")) {

                toast("Post created successfully.");
                HomeActivity.start(CreateNewPostActivity.this);
            }

        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.fine("OnREsume");

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter(CreateNewPostService.INTENT_DATA));
    }

    @OnClick(R.id.btn_close_new_post) public void onCloseClick() {
        finish();
    }

    @OnClick(R.id.btn_take_photo_new_post) public void onTakePhotoClick() {

        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, RC_CAMERA);
    }
}
