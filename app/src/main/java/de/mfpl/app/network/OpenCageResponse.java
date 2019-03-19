package de.mfpl.app.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class OpenCageResponse {

    @SerializedName("rate")
    private OpenCageRate rate;

    @SerializedName("status")
    private OpenCageStatus status;

    @SerializedName("results")
    private List<OpenCageResult> resultList;

    public OpenCageRate getRate() {
        return rate;
    }

    public OpenCageStatus getStatus() {
        return status;
    }

    public List<OpenCageResult> getResultList() {
        return resultList;
    }

}
