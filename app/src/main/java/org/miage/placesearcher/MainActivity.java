package org.miage.placesearcher;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.otto.Subscribe;

import org.miage.placesearcher.event.EventBusManager;
import org.miage.placesearcher.event.SearchResultEvent;
import org.miage.placesearcher.ui.PlaceAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private PlaceAdapter mPlaceAdapter;

    @BindView(R.id.activity_main_search_adress_edittext)
    EditText mSearchEditText;

    @BindView(R.id.activity_main_loader)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Instanciate a PlaceAdpater with empty content
        mPlaceAdapter = new PlaceAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mPlaceAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        // Log current token (if any define, otherwise our toekn service will be notified)
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isComplete()) {
                    Log.d("[FireBase Token]", "Current token: " + task.getResult());
                }
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

        super.onPause();
    }

    @Subscribe
    public void searchResult(final SearchResultEvent event) {
        // Here someone has posted a SearchResultEvent
        runOnUiThread (() -> {
            // Step 1: Update adapter's model
            mPlaceAdapter.setPlaces(event.getPlaces());
            mPlaceAdapter.notifyDataSetChanged();

            // Step 2: hide loader
            mProgressBar.setVisibility(View.GONE);
        });

    }

    @OnClick(R.id.activity_main_switch_button)
    public void clickedOnSwitchToMap() {
        Intent switchToMapIntent = new Intent(this, MapActivity.class);
        switchToMapIntent.putExtra("currentSearch", mSearchEditText.getText().toString());
        startActivity(switchToMapIntent);
    }


    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Aucun contenu", Toast.LENGTH_LONG).show();
                } else {

                    String data = result.getContents();
                    String [] prefixe = data.split(":");
                    if (prefixe[0].equals("geo")) {
                        //Pour le moment on va juste faire une recherche sur le nom du lieu scanner
                        String [] prefixe2 = data.split("q=");
                        String nom_lieu;
                        if (prefixe2.length > 1) {
                            nom_lieu = prefixe2[1];
                        }else {
                            nom_lieu = "";
                            Toast.makeText(this, "Le nom du lieu n'est pas precisé", Toast.LENGTH_LONG).show();
                        }
                        Intent switchToMapIntent = new Intent(this, MapActivity.class);
                        switchToMapIntent.putExtra("currentSearch", nom_lieu);
                        startActivity(switchToMapIntent);
                    } else if (prefixe[0].equals("http") || prefixe[0].equals("https")) {
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                        startActivity(launchBrowser);
                    } else if (prefixe.length == 1) {
                        mSearchEditText.setText(data);
                    } else {
                        Toast.makeText(this, "[Non supported data] " + data, Toast.LENGTH_LONG).show();
                    }

                }
            });


    @OnClick(R.id.activity_main_qr_scan)
    public void onQrScanButtonClick() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        barcodeLauncher.launch(options);
        //barcodeLauncher.launch(new ScanOptions());
    }

}
