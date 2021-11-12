package org.miage.placesearcher.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexmorel on 24/01/2018.
 */

@Table(name = "PlaceCoordinates")
public class PlaceCoordinates extends Model {

    @Column(name = "label", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String label;

    @Expose
    public List<Double> coordinates = new ArrayList<>();

    @Column(name = "latitude")
    public double latitude = 0;

    @Column(name = "longitude")
    public double longitude = 0;
}
