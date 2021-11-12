package org.miage.placesearcher.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.miage.placesearcher.PlaceDetailActivity;
import org.miage.placesearcher.R;
import org.miage.placesearcher.model.PlaceAddress;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by alexmorel on 04/01/2018.
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private LayoutInflater inflater;
    private Activity context;
    private List<PlaceAddress> mPlaces;

    public PlaceAdapter(Activity context, List<PlaceAddress> Places) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mPlaces = Places;
    }

    @Override
    public PlaceAdapter.PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.place_item, parent, false);
        PlaceAdapter.PlaceViewHolder holder = new PlaceAdapter.PlaceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(PlaceAdapter.PlaceViewHolder holder, int position) {
        // Adapt the ViewHolder state to the new element
        final PlaceAddress place = mPlaces.get(position);
        holder.mPlaceStreetTextView.setText(place.getProperties().name);
        holder.mPlaceZipTextView.setText(""+place.getProperties().postcode);
        holder.mPlaceCityTextView.setText(place.getProperties().city);
        if (place.getProperties().isStreet()) {
            holder.mPlaceIcon.setImageResource(R.drawable.street_icon);
        } else {
            holder.mPlaceIcon.setImageResource(R.drawable.home_icon);
        }
        holder.mPlaceIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play mp3
                AssetFileDescriptor afd = null;
                try {
                    afd = context.getAssets().openFd("house.mp3");

                    MediaPlayer player = new MediaPlayer();
                    player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                    player.prepare();
                    player.start();

                } catch (IOException e) {
                    // Silent catch : sound will not be played
                    e.printStackTrace();
                }

                // Open place details activity
                Intent seePlaceDetailIntent = new Intent(context, PlaceDetailActivity.class);
                seePlaceDetailIntent.putExtra("placeStreet", place.getProperties().name);
                context.startActivity(seePlaceDetailIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }

    public void setPlaces(List<PlaceAddress> places) {
        this.mPlaces = places;
    }

    // Pattern ViewHolder
    class PlaceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.place_adapter_street)
        TextView mPlaceStreetTextView;

        @BindView(R.id.place_adapter_zip)
        TextView mPlaceZipTextView;

        @BindView(R.id.place_adapter_city)
        TextView mPlaceCityTextView;

        @BindView(R.id.place_adapter_icon)
        ImageView mPlaceIcon;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}