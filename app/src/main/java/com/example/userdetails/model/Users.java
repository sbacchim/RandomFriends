package com.example.userdetails.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.realm.RealmObject;

public class Users extends RealmObject {

    @SerializedName("results")
    @Expose
    public ArrayList<Results> results;
    @SerializedName("info")
    @Expose
    private Info info;

    public ArrayList<Results> getResults() {
        return results;
    }

    public void setResults(ArrayList<Results> results) {
        this.results = results;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }
}
