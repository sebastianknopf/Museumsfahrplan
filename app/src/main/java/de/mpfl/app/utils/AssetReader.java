package de.mpfl.app.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public final class AssetReader {

    private Context context;
    private String assetFileName;

    public AssetReader(Context context, String assetFileName) {
        this.context = context;
        this.assetFileName = assetFileName;
    }

    public String getContent() {
        String contentString = null;

        try {
            InputStream input = this.context.getAssets().open(this.assetFileName);

            int size = input.available();
            byte[] buffer = new byte[size];

            input.read(buffer);
            input.close();

            // byte buffer into a string
            contentString = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentString;
    }

}
