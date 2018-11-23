package de.mfpl.staticnet.lib.base;

import com.google.gson.annotations.SerializedName;

public final class Container {

    @SerializedName("app_id")
    private String appId;

    @SerializedName("api_key")
    private String apiKey;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("instance")
    private String instance;

    @SerializedName("request")
    private Request request;

    @SerializedName("delivery")
    private Delivery delivery;

    public Container setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getAppId() {
        return this.appId;
    }

    public Container setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public Container setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Container setInstance(String instance) {
        this.instance = instance;
        return this;
    }

    public String getInstance() {
        return this.instance;
    }

    public Container setRequest(Request request) {
        this.request = request;
        return this;
    }

    public Request getRequest() {
        return this.request;
    }

    public Container setDelivery(Delivery delivery) {
        this.delivery = delivery;
        return this;
    }

    public Delivery getDelivery() {
        return this.delivery;
    }

}
