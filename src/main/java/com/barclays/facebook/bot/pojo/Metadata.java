package com.barclays.facebook.bot.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Metadata implements Serializable {

    /**
     * Name of the intent that produced this result
     */
    @SerializedName("intentName")
    private String intentName;

    /**
     * Id of the intent that produced this result
     */
    @SerializedName("intentId")
    private String intentId;

    /**
     * Name of the intent that produced this result
     */
    public String getIntentName() {
        return intentName;
    }

    public void setIntentName(final String intentName) {
        this.intentName = intentName;
    }

    /**
     * Id of the intent that produced this result
     */
    public String getIntentId() {
        return intentId;
    }

    public void setIntentId(final String intentId) {
        this.intentId = intentId;
    }
}
