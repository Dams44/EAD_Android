package org.miage.placesearcher.event;

import org.miage.placesearcher.model.PlaceAddress;

import java.util.List;

/**
 * Created by alexmorel on 10/01/2018.
 */

public class SearchResultEvent {

    private List<PlaceAddress> places;

    public SearchResultEvent(List<PlaceAddress> places) {
        this.places = places;
    }

    public List<PlaceAddress> getPlaces() {
        return places;
    }
}
