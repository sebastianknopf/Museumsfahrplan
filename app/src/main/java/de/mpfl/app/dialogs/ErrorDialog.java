package de.mpfl.app.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;

import de.mpfl.app.R;
import de.mpfl.app.databinding.LayoutDialogErrorBinding;

public final class ErrorDialog {

    private Context context;
    private LayoutDialogErrorBinding components;
    private OnRetryClickListener onRetryClickListener;

    public ErrorDialog(Context context) {
        this.context = context;
        this.components = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_dialog_error, null, false);
    }

    public void setDialogImage(@DrawableRes int dialogImage) {
        this.components.imgErrorImage.setImageDrawable(context.getDrawable(dialogImage));
    }

    public void setDialogTitle(@StringRes int dialogTitle) {
        this.components.lblErrorTitle.setText(dialogTitle);
    }

    public void setDialogText(@StringRes int dialogText) {
        this.components.lblErrorText.setText(dialogText);
    }

    public void setOnRetryClickListener(OnRetryClickListener onRetryClickListener) {
        this.onRetryClickListener = onRetryClickListener;
    }

    public void show() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.context);
        dialogBuilder.setView(this.components.getRoot());

        if(this.onRetryClickListener != null) {
            this.components.btnErrorRetry.setText(R.string.str_retry);
        }

        AlertDialog dialog = dialogBuilder.create();
        this.components.btnErrorRetry.setOnClickListener(view -> {
            if(this.onRetryClickListener != null) {
                this.onRetryClickListener.onRetryClick();
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    public interface OnRetryClickListener {

        void onRetryClick();

    }

}
