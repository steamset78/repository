package server.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MultiRoutePath {

    @SerializedName("direct_paths")
    List<DirectRoute> directRoutes;


    @SerializedName("euro_price")
    float totalEuroPrice;

    @SerializedName("duration_minutes")
    int totalTimeInMinutes;

    public MultiRoutePath(List<DirectRoute> directRoutes, float totalEuroPrice, int totalTimeInMinutes) {
        this.directRoutes = directRoutes;
        this.totalEuroPrice = totalEuroPrice;
        this.totalTimeInMinutes = totalTimeInMinutes;
    }

    public List<DirectRoute> getDirectRoutes() {
        return directRoutes;
    }

    public void setDirectRoutes(List<DirectRoute> directRoutes) {
        this.directRoutes = directRoutes;
    }

    public float getTotalEuroPrice() {
        return totalEuroPrice;
    }

    public void setTotalEuroPrice(float totalEuroPrice) {
        this.totalEuroPrice = totalEuroPrice;
    }

    public int getTotalTimeInMinutes() {
        return totalTimeInMinutes;
    }

    public void setTotalTimeInMinutes(int totalTimeInMinutes) {
        this.totalTimeInMinutes = totalTimeInMinutes;
    }

    @Override
    public String toString() {
        return "MultiRoutePath{" +
                "directRoutes=" + directRoutes +
                ", totalEuroPrice=" + totalEuroPrice +
                ", totalTimeInMinutes=" + totalTimeInMinutes +
                '}';
    }
}
