package server.model;

import com.google.gson.annotations.SerializedName;

public class DirectRoute {


    transient int id;
    transient int from;
    transient int to;
    transient int transportationType;

    @SerializedName("transportation_type")
    String transportationTypeName;

    @SerializedName("euro_price")
    float euroPrice;

    @SerializedName("duration_minutes")
    int timeInMinutes;

    @SerializedName("from")
    String fromName;

    @SerializedName("to")
    String toName;

    @SerializedName("line")
    String line;

    public int getTransportationType() {
        return transportationType;
    }

    public void setTransportationType(int transportationType) {
        this.transportationType = transportationType;
    }

    public String getTransportationTypeName() {
        return transportationTypeName;
    }

    public void setTransportationTypeName(String transportationTypeName) {
        this.transportationTypeName = transportationTypeName;
    }

    public int getTimeInMinutes() {
        return timeInMinutes;
    }

    public void setTimeInMinutes(int timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public DirectRoute(int id, int from, int to, float euroPrice) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.euroPrice = euroPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public float getEuroPrice() {
        return euroPrice;
    }

    public void setEuroPrice(float euroPrice) {
        this.euroPrice = euroPrice;
    }

    @Override
    public String toString() {
        return "DirectRoute{" +
                "id=" + id +
                ", from=" + from +
                ", to=" + to +
                ", transportationType=" + transportationType +
                ", transportationTypeName='" + transportationTypeName + '\'' +
                ", euroPrice=" + euroPrice +
                ", timeInMinutes=" + timeInMinutes +
                ", fromName='" + fromName + '\'' +
                ", toName='" + toName + '\'' +
                ", line='" + line + '\'' +
                '}';
    }
}
