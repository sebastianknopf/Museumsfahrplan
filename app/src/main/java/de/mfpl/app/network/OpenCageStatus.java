package de.mfpl.app.network;

import com.google.gson.annotations.SerializedName;

public final class OpenCageStatus {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
