package de.mfpl.app.database;

public final class Favorite {

    private int id;
    private String tripId;
    private String tripType;
    private String tripName;
    private String tripDesc;
    private String tripDate;
    private String tripTime;

    public Favorite() {
        this.tripId = new String();
        this.tripType = new String();
        this.tripName = new String();
        this.tripDesc = new String();
        this.tripDate = new String();
        this.tripTime = new String();
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getTripDesc() {
        return tripDesc;
    }

    public void setTripDesc(String tripDesc) {
        this.tripDesc = tripDesc;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public String getTripTime() {
        return tripTime;
    }

    public void setTripTime(String tripTime) {
        this.tripTime = tripTime;
    }

}
