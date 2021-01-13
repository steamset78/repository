package server.model;

import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("id")
    int id;

    @SerializedName("name")
    String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "java.model.Location{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
