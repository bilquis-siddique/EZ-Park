package ameya.com.maptest;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class GarageParking {

    public int status;
    public String title;
    public String type;
    public String gkey;
    public String key;
    public String dimen;
    public String userid;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGkey() {
        return gkey;
    }

    public void setGkey(String gkey) {
        this.gkey = gkey;
    }

    public String getDimen() {
        return dimen;
    }

    public void setDimen(String dimen) {
        this.dimen = dimen;
    }

    public GarageParking(int status, String title, String type, String gkey, String dimen,String key,String uid) {
        this.status = status;
        this.title = title;
        this.type = type;
        this.gkey = gkey;
        this.dimen = dimen;
        this.key=key;
        this.userid=uid;
    }

    public GarageParking() {
        // Default constructor required for calls to DataSnapshot.getValue(Garage.class)
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("title", title);
        result.put("type", type);
        result.put("dimen",dimen);
        result.put("gkey",gkey);
        result.put("key",key);
        result.put("userid",userid);
        return result;
    }
}
