package org.miage.placesearcher.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@Table(name = "PlaceProperties")
public class PlaceProperties extends Model {
    @Expose
    @Column(name = "label", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String label;

    @Expose
    @Column(name = "name")
    public String name;

    @Expose
    @Column(name = "postcode")
    public String postcode;

    @Expose
    @Column(name = "city")
    public String city;

    @Expose
    @Column(name = "type")
    public String type;

    public boolean isStreet() {
        return type != null && type.equals("street");
    }
}