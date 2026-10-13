package com.nc13ja.cashier;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BarcodeScannerActivity extends AppCompatActivity {
    public static final String EXTRA_BARCODE = "barcode_value";
    private static final int CAMERA_PERMISSION_REQUEST = 7302;
    private PreviewView previewView;
    private ExecutorService analysisExecutor;
    private BarcodeScanner scanner;
    private Camera camera;
    private final AtomicBoolean barcodeHandled = new AtomicBoolean(false);
    private boolean torchOn = false;
    private String language = "fr";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(12, 25, 35));
        getWindow().setNavigationBarColor(Color.rgb(12, 25, 35));
        language = getIntent().getStringExtra("language");
        if (language == null) language = "fr";
        analysisExecutor = Executors.newSingleThreadExecutor();
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
        scanner = BarcodeScanning.getClient(options);
        buildScannerUi();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
    }


    private String label(String key) {
        if ("ar".equals(language)) {
            switch (key) {
                case "title": return "ماسح الباركود";
                case "hint": return "ضع الباركود داخل مجال الكاميرا";
                case "torch": return "☼  تشغيل الفلاش";
                case "torchOn": return "☼  الفلاش يعمل";
                case "noFlash": return "الفلاش غير متوفر";
                case "cameraError": return "تعذر تشغيل الكاميرا";
                case "permission": return "يلزم السماح بالكاميرا لمسح الباركود";
            }
        } else if ("en".equals(language)) {
            switch (key) {
                case "title": return "Barcode scanner";
                case "hint": return "Place the barcode inside the camera view";
                case "torch": return "☼  Flashlight";
                case "torchOn": return "☼  Flashlight on";
                case "noFlash": return "Flashlight not available";
                case "cameraError": return "Could not start camera";
                case "permission": return "Camera permission is required to scan barcodes";
            }
        } else if ("ja".equals(language)) {
            switch (key) {
                case "title": return "バーコードスキャナー";
                case "hint": return "バーコードをカメラの枠内に置いてください";
                case "torch": return "☼  フラッシュ";
                case "torchOn": return "☼  フラッシュ点灯中";
                case "noFlash": return "フラッシュを利用できません";
                case "cameraError": return "カメラを起動できません";
                case "permission": return "バーコード読み取りにはカメラの許可が必要です";
            }
        } else {
            switch (key) {
                case "title": return "Lecteur de code-barres";
                case "hint": return "Placez le code-barres dans le cadre";
                case "torch": return "☼  Lampe torche";
                case "torchOn": return "☼  Lampe allumée";
                case "noFlash": return "Lampe torche indisponible";
                case "cameraError": return "Impossible de démarrer la caméra";
                case "permission": return "L'autorisation de la caméra est nécessaire";
            }
        }
        return key;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void buildScannerUi() {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(Color.rgb(12, 25, 35));
        previewView = new PreviewView(this);
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        frame.addView(previewView, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(18), dp(18), dp(18), dp(18));
        top.setBackgroundColor(0x990C1923);
        TextView close = actionText("✕", 22);
        top.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        close.setOnClickListener(v -> finish());
        TextView title = actionText(label("title"), 18);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        top.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));
        frame.addView(top, new FrameLayout.LayoutParams(-1, -2, Gravity.TOP));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(dp(20), dp(18), dp(20), dp(24));
        bottom.setBackgroundColor(0xCC0C1923);
        bottom.addView(actionText(label("hint"), 15));
        TextView torch = actionText(label("torch"), 15);
        torch.setPadding(dp(18), dp(12), dp(18), dp(12));
        torch.setBackground(roundBackground(0xFF167D78));
        LinearLayout.LayoutParams torchParams = new LinearLayout.LayoutParams(-2, -2);
        torchParams.topMargin = dp(12);
        bottom.addView(torch, torchParams);
        torch.setOnClickListener(v -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                torchOn = !torchOn;
                camera.getCameraControl().enableTorch(torchOn);
                torch.setText(torchOn ? label("torchOn") : label("torch"));
            } else {
                Toast.makeText(this, label("noFlash"), Toast.LENGTH_SHORT).show();
            }
        });
        frame.addView(bottom, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));
        setContentView(frame);
    }

    private TextView actionText(String text, int size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return view;
    }

    private android.graphics.drawable.GradientDrawable roundBackground(int color) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(16));
        return drawable;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(analysisExecutor, imageProxy -> {
                    if (barcodeHandled.get()) {
                        imageProxy.close();
                        return;
                    }
                    try {
                        android.media.Image mediaImage = imageProxy.getImage();
                        if (mediaImage == null) {
                            imageProxy.close();
                            return;
                        }
                        InputImage image = InputImage.fromMediaImage(
                                mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                        scanner.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    if (barcodeHandled.get()) return;
                                    for (Barcode barcode : barcodes) {
                                        String value = barcode.getRawValue();
                                        if (value != null && !value.trim().isEmpty()
                                                && barcodeHandled.compareAndSet(false, true)) {
                                            runOnUiThread(() -> returnBarcode(value));
                                            break;
                                        }
                                    }
                                })
                                .addOnFailureListener(error -> android.util.Log.w(
                                        "CashierScanner", "Barcode analysis failed", error))
                                .addOnCompleteListener(task -> imageProxy.close());
                    } catch (Exception error) {
                        // Always release the camera frame if image conversion or task startup fails.
                        android.util.Log.e("CashierScanner", "Could not process camera frame", error);
                        imageProxy.close();
                    }
                });
                provider.unbindAll();
                camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
            } catch (Exception e) {
                Toast.makeText(this, label("cameraError") + ": " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void returnBarcode(String value) {
        Intent result = new Intent();
        result.putExtra(EXTRA_BARCODE, value);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startCamera();
            else {
                Toast.makeText(this, label("permission"), Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override protected void onDestroy() {
        if (analysisExecutor != null) analysisExecutor.shutdown();
        if (scanner != null) scanner.close();
        super.onDestroy();
    }
}
