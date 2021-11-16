package org.miage.placesearcher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import org.miage.placesearcher.event.EventBusManager;
import org.miage.placesearcher.event.SearchResultEvent;
import org.miage.placesearcher.model.PlaceAddress;

import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by alexmorel on 17/01/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.activity_main_search_adress_edittext)
    EditText mSearchEditText;

    @BindView(R.id.activity_main_loader)
    ProgressBar mProgressBar;
    private GoogleMap mActiveGoogleMap;
    private Map<String, PlaceAddress> mMarkersToPlaces = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Get map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set textfield value according to intent
        if (getIntent().hasExtra("currentSearch")) {
            mSearchEditText.setText(getIntent().getStringExtra("currentSearch"));
        }

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing to do when texte is about to change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // While text is changing, hide list and show loader
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Once text has changed
                // Show a loader
                mProgressBar.setVisibility(View.VISIBLE);

                // Launch a search through the PlaceSearchService
                PlaceSearchService.INSTANCE.searchPlacesFromAddress(editable.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

        // Register to Event bus : now each time an event is posted, the activity will receive it if it is @Subscribed to this event
        EventBusManager.BUS.register(this);

        // Refresh search
        PlaceSearchService.INSTANCE.searchPlacesFromAddress(mSearchEditText.getText().toString());
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        // Do NOT forget to call super.onPause()
        super.onPause();
    }

    @Subscribe
    public void searchResult(final SearchResultEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Here someone has posted a SearchResultEvent
                // Check that map is ready
                if (mActiveGoogleMap != null) {
                    // Update map's markers
                    mActiveGoogleMap.clear();
                    mMarkersToPlaces.clear();

                    LatLngBounds.Builder cameraBounds = LatLngBounds.builder();
                    for (PlaceAddress place : event.getPlaces()) {
                        // Step 1: create marker icon (and resize drawable so that marker is not too big)
                        int markerIconResource;
                        if (place.getProperties().isStreet()) {
                            markerIconResource = R.drawable.street_icon;
                        } else {
                            markerIconResource = R.drawable.home_icon;
                        }
                        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), markerIconResource);
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 50, 50, false);

                        // Step 2: define marker options
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(place.getCoordinates().latitude, place.getCoordinates().longitude))
                                .title(place.getProperties().name)
                                .snippet(place.getProperties().postcode + "  " + place.getProperties().city)
                                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

                        // Step 3: include marker in camera bounds
                        cameraBounds.include(markerOptions.getPosition());

                        // Step 4: add marker
                        Marker marker = mActiveGoogleMap.addMarker(markerOptions);
                        mMarkersToPlaces.put(marker.getId(), place);
                    }

                    // Hide loader
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick(R.id.activity_map_switch_button)
    public void clickedOnSwitchToList() {
        Intent switchToListIntent = new Intent(this, MainActivity.class);
        switchToListIntent.putExtra("currentSearch", mSearchEditText.getText().toString());
        startActivity(switchToListIntent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mActiveGoogleMap = googleMap;
        mActiveGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mActiveGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        mActiveGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                PlaceAddress associatedPlace = mMarkersToPlaces.get(marker.getId());
                if (associatedPlace != null) {
                    Intent seePlaceDetailIntent = new Intent(MapActivity.this, PlaceDetailActivity.class);
                    seePlaceDetailIntent.putExtra("placeStreet", associatedPlace.getProperties().name);
                    //Récupération de la latitude et longitude pour créer un QR code avec coordonées GPS
                    seePlaceDetailIntent.putExtra("Latitude", associatedPlace.getCoordinates().latitude);
                    seePlaceDetailIntent.putExtra("Longitude", associatedPlace.getCoordinates().longitude);
                    startActivity(seePlaceDetailIntent);
                }
            }
        });
    }
}