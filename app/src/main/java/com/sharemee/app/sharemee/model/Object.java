package com.sharemee.app.sharemee.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user-laptop on 01/04/2015.
 */
public class Object implements Parcelable {
    private int idObject;
    private String nameObject;
    private String nameCategory;
    private double latObject;
    private double longObject;
    private String imagePath1Object;
    
    public Object(JSONObject inObject){
        if(inObject!=null){
            try {
                this.idObject=inObject.getInt("idObject");
                this.nameObject=inObject.getString("nameObject");
                this.nameCategory=inObject.getString("nameCategory");
                this.latObject=inObject.getDouble("latObject");
                this.longObject=inObject.getDouble("longObject");
                this.imagePath1Object=inObject.getString("imagePath1Object");
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    
    public Object(Parcel source){
        if(source.dataSize()>0){
            this.idObject=source.readInt();
            this.nameObject=source.readString();
            this.nameCategory=source.readString();
            this.latObject=source.readDouble();
            this.longObject=source.readDouble();
            this.imagePath1Object=source.readString();
        }
    }

    //Generated Getters and Setters
    public int getIdObject() {
        return idObject;
    }

    public void setIdObject(int idObject) {
        this.idObject = idObject;
    }

    public String getNameObject() {
        return nameObject;
    }

    public void setNameObject(String nameObject) {
        this.nameObject = nameObject;
    }

    public String getNameCategory() {
        return nameCategory;
    }

    public void setNameCategory(String nameCategory) {
        this.nameCategory = nameCategory;
    }

    public double getLatObject() {
        return latObject;
    }

    public void setLatObject(double latObject) {
        this.latObject = latObject;
    }

    public double getLongObject() {
        return longObject;
    }

    public void setLongObject(double longObject) {
        this.longObject = longObject;
    }

    public String getImagePath1Object() {
        return imagePath1Object;
    }

    public void setImagePath1Object(String imagePath1Object) {
        this.imagePath1Object = imagePath1Object;
    }

    public final Creator<Object> CREATOR = new Creator<Object>(){
        public Object createFromParcel(Parcel in){
            return new Object(in);
        }
        public Object[] newArray(int size){
            return new Object[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.idObject);
        dest.writeString(this.nameObject);
        dest.writeString(this.nameCategory);
        dest.writeDouble(this.latObject);
        dest.writeDouble(this.longObject);
        dest.writeString(this.imagePath1Object);
    }
}
