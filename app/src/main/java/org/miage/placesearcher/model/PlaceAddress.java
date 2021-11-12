package org.miage.placesearcher.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;

@Table(name = "PlaceAddress")
public class PlaceAddress extends Model {

    @Expose
    @Column(name = "label", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String label;

    @Expose
    public PlaceProperties properties;

    @Expose
    public PlaceCoordinates geometry;

    public PlaceAddress() {
        super();
    }

    public PlaceProperties getProperties() {
        if (properties == null) {
            properties = new Select().from(PlaceProperties.class).where("label='" + label.replace("'", "''") + "'").executeSingle();
        }
        return properties;
    }

    public PlaceCoordinates getCoordinates() {
        if (geometry == null) {
            geometry = new Select().from(PlaceCoordinates.class).where("label='" + label.replace("'", "''") + "'").executeSingle();
        }
        return geometry;
    }
}