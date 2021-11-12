package org.miage.placesearcher.model;

import com.google.gson.annotations.Expose;

import java.util.List;

public class PlaceSearchResult {
    @Expose
    public List<PlaceAddress> features;
}