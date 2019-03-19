package de.mfpl.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.journeyapps.barcodescanner.CaptureManager;

import de.mfpl.app.databinding.ActivityScanBinding;

public class ScanActivity extends AppCompatActivity{

    private final static String TAG = ScanActivity.class.getSimpleName();

    private static final int PERMISSION_ACCESS_CAMERA = 0;

    private ActivityScanBinding components;
    private CaptureManager captureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set theme before doing any other thing
        this.setTheme(R.style.AppTheme);

        // set window on keep on state
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // initialize activity content
        super.onCreate(savedInstanceState);
        this.components = DataBindingUtil.setContentView(this, R.layout.activity_scan);

        // init capture view
        this.captureManager = new CaptureManager(this, this.components.barcodeView);
        this.captureManager.initializeFromIntent(this.getIntent(), savedInstanceState);
        this.captureManager.decode();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.components.barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.components.barcodeView.pause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        this.captureManager.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.components.barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            this.finish();
        } else {
            this.checkPermissions();
        }
    }

    private void checkPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, PERMISSION_ACCESS_CAMERA);
            }
        }
    }
}
