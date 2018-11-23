package de.mfpl.staticnet.lib.data;

import com.google.gson.annotations.SerializedName;

public final class Error {

    @SerializedName("error_code")
    private String errorCode;

    @SerializedName("error_message")
    private String errorMessage;

    @SerializedName("error_details")
    private String errorDetails;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    @Override
    public String toString() {
        return this.errorCode + " " + this.errorMessage + " " + this.errorDetails;
    }
}
