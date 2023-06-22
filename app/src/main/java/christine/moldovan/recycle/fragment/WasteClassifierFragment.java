package christine.moldovan.recycle.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import christine.moldovan.recycle.R;

public class WasteClassifierFragment extends Fragment {
    private TextView result, confidence;
    private TextView classifiedText, confidenceText;
    private TextView infoText, infoPhoto;
    private ImageView imageView, photoDemo;
    private CardView photoCard;
    private ImageButton takePhoto;
    private Handler handler;
    private int imageSize = 224;
    private Interpreter interpreter = null;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_waste_classifier, container, false);
        result = (TextView) v.findViewById(R.id.result);
        confidence = (TextView) v.findViewById(R.id.confidence);
        imageView = (ImageView) v.findViewById(R.id.imageView);
        takePhoto = (ImageButton) v.findViewById(R.id.takePicture_button);
        classifiedText = (TextView) v.findViewById(R.id.classifiedText);
        confidenceText = (TextView) v.findViewById(R.id.confidencesText);

        infoPhoto = (TextView) v.findViewById(R.id.infoPhoto);
        infoText = (TextView) v.findViewById(R.id.infoText);
        photoDemo = (ImageView) v.findViewById(R.id.photoDemo);
        photoCard = (CardView) v.findViewById(R.id.photoCard);


        takePhoto.setEnabled(false);

        downloadModel();

        handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                downloadModel();
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(r, 5000);

        //Request camera permission if we don't have it.
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 100);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch camera if we have permission
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activityResultLauncher.launch(cameraIntent);
                } else {
                    Toast.makeText(getContext(), "Permission for camera was denied. Please review application permission.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 100);
                }

            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                    int dimension = Math.min(image.getWidth(), image.getHeight());
                    image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                    imageView.setImageBitmap(image);
                    image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                    FirebaseApp.initializeApp(getActivity().getApplicationContext());
                    classifyImage(image);
                }
            }
        });

        return v;
    }

    private synchronized void downloadModel() {
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("Recyclables-Detector", DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(model -> {
                    try {
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                            interpreter = new Interpreter(modelFile);
                        }
                        takePhoto.setEnabled(true);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to initialize the model. ", e);
                        Toast.makeText(
                                        getActivity(),
                                        "Model initialization failed.",
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to download the model. ", e);
                    Toast.makeText(
                                    getActivity(),
                                    "Model download failed, please check your connection.",
                                    Toast.LENGTH_LONG)
                            .show();
                });
    }

    public void classifyImage(Bitmap image) {
        classifiedText.setVisibility(View.VISIBLE);
        confidenceText.setVisibility(View.VISIBLE);
        result.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        confidence.setVisibility(View.VISIBLE);
        photoCard.setVisibility(View.GONE);
        infoText.setVisibility(View.GONE);
        Bitmap bitmap = Bitmap.createScaledBitmap(image, 224, 224, true);
        ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());

        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int px = bitmap.getPixel(x, y);

                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                float rf = (r - 127) / 255.0f;
                float gf = (g - 127) / 255.0f;
                float bf = (b - 127) / 255.0f;

                input.putFloat(rf);
                input.putFloat(gf);
                input.putFloat(bf);
            }
        }

        int bufferSize = 1000 * Float.SIZE / Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        interpreter.run(input, modelOutput);
        modelOutput.rewind();
        FloatBuffer confidences = modelOutput.asFloatBuffer();
        // find the index of the class with the biggest confidence.
        int maxPos = 0;
        float maxConfidence = confidences.get((int) 0);
        for (int i = 0; i < confidences.capacity(); i++) {
            if (confidences.get(i) > maxConfidence) {
                maxConfidence = confidences.get(i);
                maxPos = i;
                System.out.println("max conf = " + maxConfidence + " at pos " + maxPos);
            }
        }
        String[] classes = {"Plastic", "Paper", "Glass", "Metal", "Electronics", "Batteries"};
        result.setText(classes[maxPos]);
        String s = "";
        for (int i = 0; i < classes.length; i++) {
            s += String.format("%s: %.1f%%\n", classes[i], confidences.get((int) i) * 100);
        }
        confidence.setText(s);
    }
}