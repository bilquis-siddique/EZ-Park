package ameya.com.maptest;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Garage {
    public int getTotalav() {
        return totalav;
    }

    public void setTotalav(int totalav) {
        this.totalav = totalav;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Garage(int totalav, String title, double lat, double lang, String key) {
        this.totalav = totalav;
        this.title = title;
        this.lat = lat;
        this.lang = lang;
        this.key = key;
    }

    public int totalav;
    public String title;
    public double lat;
    public double lang;
    public String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Double getLang() {
        return lang;
    }

    public void setLang(double lang) {
        this.lang = lang;
    }

    public Garage() {
        // Default constructor required for calls to DataSnapshot.getValue(Garage.class)
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("totalav", totalav);
        result.put("title", title);
        result.put("lat", lat);
        result.put("lang",lang);
        result.put("key",key);
        return result;
    }

}
