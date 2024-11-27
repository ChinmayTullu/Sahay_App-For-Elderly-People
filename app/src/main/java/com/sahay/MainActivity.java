package com.sahay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CAMERA = 10;
    String search;

    private PreviewView previewView;
    private TextView textView;
    private Button captureButton, speakButton;
    private ImageCapture imageCapture;
    private Bitmap capturedImageBitmap;
    private ExecutorService cameraExecutor;
    private TextRecognizer textRecognizer;
    private ProcessCameraProvider cameraProvider;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=getIntent();
        search=intent.getStringExtra(SchedulePage.detailsKey);

        // Initialize UI components
        previewView = findViewById(R.id.previewView);
        textView = findViewById(R.id.text_view);
        captureButton = findViewById(R.id.capture_button);
        speakButton = findViewById(R.id.speak_button);

        // Setup Executor and Text Recognizer
        cameraExecutor = Executors.newSingleThreadExecutor();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Initialize TextToSpeech
        initializeTextToSpeech();

        // Check and request camera permission
        if (allPermissionsGranted()) {
            Log.d(TAG, "Camera permission already granted, starting camera");
            startCamera();
        } else {
            Log.d(TAG, "Requesting camera permission");
            requestCameraPermission();
        }

        // Set button click listeners
        captureButton.setOnClickListener(v -> captureImage());
        speakButton.setOnClickListener(v -> {
            if (capturedImageBitmap != null) {
                detectTextFromImage();
            } else {
                Toast.makeText(this, "No image captured yet", Toast.LENGTH_SHORT).show();
            }
        });

        finish();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                }
            } else {
                Log.e(TAG, "Initialization failed");
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: ", e);
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        try {
            // Unbind any bound use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(this, "Error setting up camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void captureImage() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        File outputFile = new File(getExternalFilesDir(null), "captured_image.jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(MainActivity.this, "Image captured successfully", Toast.LENGTH_SHORT).show();
                        processCapturedImage(outputFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Image capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(MainActivity.this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processCapturedImage(File imageFile) {
        capturedImageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        if (capturedImageBitmap != null) {
            detectTextFromImage();
        } else {
            Log.e(TAG, "Failed to load captured image");
            Toast.makeText(this, "Failed to load captured image", Toast.LENGTH_SHORT).show();
        }
    }

    private void detectTextFromImage() {
        if (capturedImageBitmap == null) {
            Toast.makeText(this, "No image to process", Toast.LENGTH_SHORT).show();
            return;
        }

        InputImage inputImage = InputImage.fromBitmap(capturedImageBitmap, 0);

        Task<Text> result = textRecognizer.process(inputImage)
                .addOnSuccessListener(text -> {
                    StringBuilder detectedText = new StringBuilder();
                    for (Text.TextBlock block : text.getTextBlocks()) {
                        for (Text.Line line : block.getLines()) {
                            detectedText.append(line.getText()).append("\n");
                        }
                    }
                    String textDisplayed=detectedText.toString();
                    Toast.makeText(MainActivity.this, detectedText, Toast.LENGTH_LONG).show();

                    Toast.makeText(MainActivity.this, search, Toast.LENGTH_SHORT).show();
                    boolean isPresent = isWordPresent(textDisplayed, search);
                    Utils.displayAlertDialog(MainActivity.this, "Correct medicine, you can take it!", "Verified!");

                    textView.setText(textDisplayed);
                    speakDetectedText(detectedText.toString());


                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error detecting text", e);
                    Toast.makeText(this, "Error detecting text", Toast.LENGTH_SHORT).show();
                });
    }

    private void speakDetectedText(String text) {
        if (text != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Toast.makeText(this, "No text detected", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult called with requestCode: " + requestCode);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted, starting camera");
                startCamera();
            } else {
                Log.d(TAG, "Camera permission denied");
                Toast.makeText(this, "Camera permission is required for this app to function.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        // Unbind use cases
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    private boolean isWordPresent(String textDisplayed, String search) {
        // Split the text by spaces to get the words
        String[] words = textDisplayed.split(" ");

        // Loop through the words and check if any match the search word
        for (String word : words) {
            // Compare the words (you can ignore case if needed by using equalsIgnoreCase)
            if (word.equalsIgnoreCase(search)) {
                return true;  // Word found
            }
        }
        return false;  // Word not found
    }
}
