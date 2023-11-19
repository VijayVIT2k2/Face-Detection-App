package vijay.vlr2k2.facedetectionfinaltrial.helpers;

import static androidx.camera.view.PreviewView.*;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import vijay.vlr2k2.facedetectionfinaltrial.MainActivity;
import vijay.vlr2k2.facedetectionfinaltrial.R;
import vijay.vlr2k2.facedetectionfinaltrial.helpers.vision.GraphicOverlay;
import vijay.vlr2k2.facedetectionfinaltrial.helpers.vision.VisionBaseProcessor;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class MLVideoHelperActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1001;
    protected PreviewView previewView;
    protected GraphicOverlay graphicOverlay;
    private TextView outputTextView;
    private ExtendedFloatingActionButton addFaceButton;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor executor = Executors.newSingleThreadExecutor();

    private VisionBaseProcessor processor;
    private ImageAnalysis imageAnalysis;
    private FloatingActionButton fab;

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Don't return to the previous activity
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_helper_new);

        previewView = findViewById(R.id.camera_source_preview);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        outputTextView = findViewById(R.id.output_text_view);
        addFaceButton = findViewById(R.id.button_add_face);
        fab = findViewById(R.id.floatingActionButton);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());

        processor = setProcessor();

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            initSource();
        }
        fab.setOnClickListener(view -> {
//            Snackbar.make(view, "Here's a Snack bar", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            PopupMenu popup = new PopupMenu(MLVideoHelperActivity.this, view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu, popup.getMenu());
            popup.show();
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if(id==R.id.action_item1){
                    return true;
                }else if(id==R.id.action_item2){
                    ParseUser.getCurrentUser();
                    ParseUser.logOut();
                    startActivity(new Intent(MLVideoHelperActivity.this,MainActivity.class));
                    return true;
                }else{
                    return false;
                }
//                        switch (item.getItemId()) {
//                            case R.id.action_item1:
//                                // Handle item 1 action
//                                return true;
//                            case R.id.action_item2:
//                                // Handle item 2 action
//                                return true;
//                            default:
//                                return false;
//                        }
            });

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processor != null) {
            processor.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initSource();
        }
    }

    protected void setOutputText(String text) {
        outputTextView.setText(text);
    }

    private void initSource() {

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getApplicationContext()));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        int lensFacing = getLensFacing();
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

        setFaceDetector(lensFacing);
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    /**
     * The face detector provides face bounds whose coordinates, width and height depend on the
     * preview's width and height, which is guaranteed to be available after the preview starts
     * streaming.
     */
    private void setFaceDetector(int lensFacing) {
        previewView.getPreviewStreamState().observe(this, new Observer<StreamState>() {
            @Override
            public void onChanged(StreamState streamState) {
                if (streamState != StreamState.STREAMING) {
                    return;
                }

                View preview = previewView.getChildAt(0);
                float width = preview.getWidth() * preview.getScaleX();
                float height = preview.getHeight() * preview.getScaleY();
                float rotation = preview.getDisplay().getRotation();
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                    float temp = width;
                    width = height;
                    height = temp;
                }

                imageAnalysis.setAnalyzer(
                        executor,
                        createFaceDetector((int) width, (int) height, lensFacing)
                );
                previewView.getPreviewStreamState().removeObserver(this);
            }
        });
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private ImageAnalysis.Analyzer createFaceDetector(int width, int height, int lensFacing) {
        graphicOverlay.setPreviewProperties(width, height, lensFacing);
        return imageProxy -> {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            // converting from YUV format
            processor.detectInImage(imageProxy, toBitmap(imageProxy.getImage()), rotationDegrees);
            // after done, release the ImageProxy object
            imageProxy.close();
        };
    }

    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    protected int getLensFacing() {
        return CameraSelector.LENS_FACING_BACK;
    }

    protected abstract VisionBaseProcessor setProcessor();

    public void makeAddFaceVisible() {
        addFaceButton.setVisibility(View.VISIBLE);
    }

    public void onAddFaceClicked(View view) {

    }
}
