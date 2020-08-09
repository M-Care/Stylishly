package dynasty.software.the.stylishly.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;

/**
 * Author : Aduraline.
 */

public class LoadingDialog {

    private LoadingDialog() {}

    private String title = "";
    private String subTitle = "";
    private AlertDialog alertDialog;
    private LoadingDialog(Builder builder) {

        title = builder.title;
        subTitle = builder.subtitle;

        Context context = StylishlyApplication.getApplication().getApplicationContext();
        alertDialog = new AlertDialog.Builder(context, R.style.AppTheme_NoActionBar)
                .setView(LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null))
                .create();
    }

    public void show() {
        if (alertDialog == null) return;
        alertDialog.show();
    }

    public void cancel() {

        if (alertDialog == null)
            return;

        alertDialog.cancel();
    }

    public static class Builder {

        String title = "";
        String subtitle = "";

        public Builder setSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public LoadingDialog build() {
            return new LoadingDialog(this);
        }
    }
}
