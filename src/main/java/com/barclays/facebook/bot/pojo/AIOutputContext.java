package com.barclays.facebook.bot.pojo;


import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class AIOutputContext implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("parameters")
    private Map<String, JsonElement> parameters;

    @SerializedName("lifespan")
    private Integer lifespan;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Map<String, JsonElement> getParameters() {
        return parameters;
    }

    public void setParameters(final Map<String, JsonElement> parameters) {
        this.parameters = parameters;
    }

    /**
     * Lifespan of the context measured in requests
     * @return count of requests context will live
     */
    public Integer getLifespan() {
        return lifespan;
    }

    /**
     * Lifespan of the context measured in requests
     * @param lifespan count of requests context will live
     */
    public void setLifespan(final Integer lifespan) {
        this.lifespan = lifespan;
    }
}
