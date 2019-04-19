package de.mfpl.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.journeyapps.barcodescanner.CaptureManager;

import java.util.List;

import de.mfpl.app.databinding.ActivityScanBinding;

public class ScanActivity extends AppCompatActivity{

    private final static String TAG = ScanActivity.class.getSimpleName();

    private static final int PERMISSION_ACCESS_CAMERA = 0;

    private ActivityScanBinding components;
    private CaptureManager captureManager;
    private boolean isTorchOn = false;

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

        //  set toolbar
        this.setSupportActionBar(this.components.scanToolbar);
        ActionBar supportActionBar = this.getSupportActionBar();
        if(supportActionBar != null) {
            supportActionBar.setTitle(this.getString(R.string.str_scan_activity_title));
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // init capture view
        this.captureManager = new CaptureManager(this, this.components.barcodeView);
        this.captureManager.initializeFromIntent(this.getIntent(), savedInstanceState);
        this.captureManager.decode();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.checkRequirements();
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
            this.checkRequirements();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fabTorchToggleClick(View view) {
        int fabBackgroundColor, fabForegroundColor;

        if(this.isTorchOn) {
            this.isTorchOn = false;
            this.components.barcodeView.setTorchOff();

            fabBackgroundColor = ContextCompat.getColor(this, android.R.color.white);
            fabForegroundColor = ContextCompat.getColor(this, android.R.color.darker_gray);
        } else {
            this.isTorchOn = true;
            this.components.barcodeView.setTorchOn();

            fabBackgroundColor = ContextCompat.getColor(this, android.R.color.darker_gray);
            fabForegroundColor = ContextCompat.getColor(this, android.R.color.white);
        }


        this.components.fabTorchToggle.setBackgroundTintList(ColorStateList.valueOf(fabBackgroundColor));
        this.components.fabTorchToggle.setColorFilter(fabForegroundColor);
    }

    private boolean hasTorchLight() {
        Camera camera = Camera.open(0);
        if(camera == null) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();
        if(parameters.getFlashMode() == null) {
            camera.release();
            return false;
        }

        List<String> flashModes = parameters.getSupportedFlashModes();
        if(flashModes == null || flashModes.isEmpty() || flashModes.size() == 1 && flashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            camera.release();
            return false;
        }

        camera.release();
        return true;
    }

    private void checkRequirements() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, PERMISSION_ACCESS_CAMERA);
            }

            return;
        }

        if(this.hasTorchLight()) {
            this.components.fabTorchToggle.show();
        }
    }
}
