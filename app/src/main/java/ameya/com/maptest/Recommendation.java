package ameya.com.maptest;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Recommendation {
    public int status;
    public String btime;
    public String etime;
    public String desc;
    public double lat;
    public double lang;
    public String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Recommendation(String key, String desc, String btime, String etime, double lat, double lang, int status) {
        this.btime = btime;
        this.etime = etime;
        this.desc = desc;
        this.lat = lat;
        this.lang = lang;
        this.status=status;
        this.key=key;
    }

    public String getBtime() {
        return btime;
    }

    public void setBtime(String btime) {
        this.btime = btime;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public Recommendation() {
        // Default constructor required for calls to DataSnapshot.getValue(Recommendation.class)
    }



    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("desc", desc);
        result.put("btime", btime);
        result.put("etime", etime);
        result.put("lat", lat);
        result.put("lang",lang);
        result.put("status",status);
        result.put("key",key);
        return result;
    }

}

