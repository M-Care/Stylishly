package dynasty.software.the.stylishly.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.PermissionsDelegate;
import dynasty.software.the.stylishly.utils.Util;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.error.CameraErrorListener;
import io.fotoapparat.exception.camera.CameraException;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.result.transformer.ResolutionTransformersKt.scaled;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;

/**
 * Author : Aduraline.
 */

public class CameraActivity extends BaseActivity {

    @BindView(R.id.camera_view_main)
    CameraView mCameraView;
    @BindView(R.id.loading_layout_camera_activity)
    FrameLayout loadingLayout;
    @BindView(R.id.iv_camera_activity)
    ImageView imageView;
    @BindView(R.id.btn_use_photo_camera_activity)
    FrameLayout usePhotoLayout;
    @BindView(R.id.btn_discard_photo_camera_activity)
    FrameLayout discardPhotoLayout;


    private Fotoapparat fotoapparat;
    private PermissionsDelegate permissionsDelegate;
    private boolean hasCameraPermission = false;
    private File photoTaken;
    public static final String PHOTO_RESULT = "photo_result";
    private boolean isProcessing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_camera_activity);

        permissionsDelegate = new PermissionsDelegate(this);
        hasCameraPermission = permissionsDelegate.hasCameraPermission();
        if (hasCameraPermission) {
            mCameraView.setVisibility(View.VISIBLE);
        }else {
            permissionsDelegate.requestCameraPermission();
        }

        fotoapparat = createFotoapparat();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hasCameraPermission) {
            fotoapparat.stop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            fotoapparat.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            hasCameraPermission = true;
            fotoapparat.start();
            mCameraView.setVisibility(View.VISIBLE);
        }
    }

    private Fotoapparat createFotoapparat() {

        return Fotoapparat
                .with(this)
                .into(mCameraView)
                .previewScaleType(ScaleType.CenterCrop)
                .lensPosition(back())
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .cameraErrorCallback(new CameraErrorListener() {
                    @Override
                    public void onError(@NotNull CameraException e) {

                        L.wtf("Error " + e);
                    }
                })
                .build();
    }

    @OnClick(R.id.fab_take_photo) public void onTakePhoto() {

        if (isProcessing) return;


        isProcessing = true;
        PhotoResult photoResult = fotoapparat.takePicture();
        photoTaken = Util.randomFile();
        loadingLayout.setVisibility(View.VISIBLE);
        photoResult.saveToFile(photoTaken);

        L.fine("Photo ==> " + photoTaken.getAbsolutePath());

        photoResult
                .toBitmap(scaled(0.25f))
                .whenDone(new WhenDoneListener<BitmapPhoto>() {
                    @Override
                    public void whenDone(BitmapPhoto bitmapPhoto) {
                        if (bitmapPhoto == null) {
                            return;
                        }

                        show(imageView);
                        hide(mCameraView);
                        loadingLayout.setVisibility(View.GONE);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setImageBitmap(bitmapPhoto.bitmap);
                        imageView.setRotation(-bitmapPhoto.rotationDegrees);

                        show(usePhotoLayout, discardPhotoLayout);
                    }
                });
    }
    @OnClick(R.id.btn_discard_photo_camera_activity) public void onDiscardClick() {
        hide(usePhotoLayout, discardPhotoLayout, imageView);
        show(mCameraView);

        isProcessing = false;
    }

    @OnClick(R.id.btn_use_photo_camera_activity) public void onUsePhotoClick() {

        Intent intent = getIntent();
        intent.putExtra(PHOTO_RESULT, photoTaken.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }
}
