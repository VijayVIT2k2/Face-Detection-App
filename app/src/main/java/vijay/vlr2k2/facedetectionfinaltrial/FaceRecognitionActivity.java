package vijay.vlr2k2.facedetectionfinaltrial;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;

import com.google.mlkit.vision.face.Face;
import com.parse.ParseFile;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import vijay.vlr2k2.facedetectionfinaltrial.helpers.MLVideoHelperActivity;
import vijay.vlr2k2.facedetectionfinaltrial.helpers.vision.VisionBaseProcessor;
import vijay.vlr2k2.facedetectionfinaltrial.helpers.vision.recogniser.FaceRecognitionProcessor;

public class FaceRecognitionActivity extends MLVideoHelperActivity implements FaceRecognitionProcessor.FaceRecognitionCallback {

    private Interpreter faceNetInterpreter;
    private FaceRecognitionProcessor faceRecognitionProcessor;

    private Face face;
    private Bitmap faceBitmap;
    private float[] faceVector;

    private int count=0;
    private boolean isFrontCamera = false;
    private ArrayList<String> imgsNames =new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeAddFaceVisible();
//        ToggleButton toggleCameraButton = findViewById(R.id.toggleCameraButton);
//
//        toggleCameraButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            isFrontCamera = isChecked;
//            if (isFrontCamera) {
//                switchCamera("1"); // Switch to front camera
//            } else {
//                switchCamera("0"); // Switch to rear camera
//            }
//        });
    }
//    private void switchCamera(String cameraId) {
//        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
//        try {
//            if (manager != null) {
//                String[] cameraIds = manager.getCameraIdList();
//                for (String id : cameraIds) {
//                    if (id.equals(cameraId)) {
//                        // Close the current camera
////                            closeCamera();
//                        Log.i("Cams",id);
//                        // Open the selected camera
////                            openCamera(id);
//                        return;
//                    }
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
    @Override
    protected VisionBaseProcessor setProcessor() {
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        faceRecognitionProcessor = new FaceRecognitionProcessor(
                faceNetInterpreter,
                graphicOverlay,
                this
        );
        faceRecognitionProcessor.activity = this;
        return faceRecognitionProcessor;
    }

    public void setTestImage(Bitmap cropToBBox) {
        if (cropToBBox == null) {
            return;
        }
        runOnUiThread(() -> ((ImageView) findViewById(R.id.testImageView)).setImageBitmap(cropToBBox));
    }

    @Override
    public void onFaceDetected(Face face, Bitmap faceBitmap, float[] faceVector) {
        this.face = face;
        this.faceBitmap = faceBitmap;
        this.faceVector = faceVector;
    }

    @Override
    public void onFaceRecognised(Face face, float probability, String name) {

    }

    @Override
    public void onAddFaceClicked(View view) {
        super.onAddFaceClicked(view);

        if (face == null || faceBitmap == null) {
            return;
        }

        Face tempFace = face;
        Log.v("Face_Face", LocalDateTime.now().toString()+"\t" +face.toString());
        Bitmap tempBitmap = faceBitmap;
        float[] tempVector = faceVector;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_face_dialog, null);
        ((ImageView) dialogView.findViewById(R.id.dlg_image)).setImageBitmap(tempBitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            Editable input  = ((EditText) dialogView.findViewById(R.id.dlg_input)).getEditableText();
//            imgsNames.add(input.toString());
//            count++;
            if (input.length() > 0) {
                faceRecognitionProcessor.registerFace(input, tempVector);
                FancyToast.makeText(this,"Face Regsitered",FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,true);
//                saveFaceDataToParseServer(tempBitmap, input.toString(), tempVector);
            }
        });
        builder.show();
    }
    private void saveFaceDataToParseServer(Bitmap faceBitmap, String userInput, float[] faceVector) {
        // Convert Bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        faceBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Create a ParseFile and associate it with the ParseObject
        ParseFile parseFile = new ParseFile("image.png", byteArray);

        // Create a ParseObject representing your data model class
        ParseServerClass parseObject = new ParseServerClass();
        parseObject.setImage(parseFile);
        parseObject.setFaceVector(faceVector);
        parseObject.put("userName", userInput);

        // Save the ParseObject
        parseObject.saveInBackground(e -> {
            if (e == null) e.printStackTrace();
            else Log.e("ParseError", "Failed to save object: " + e.getMessage());
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}