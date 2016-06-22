package com.barclays.facebook.bot.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Fulfillment implements Serializable {

    @SerializedName("speech")
    private String speech;

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(final String speech) {
        this.speech = speech;
    }
}
