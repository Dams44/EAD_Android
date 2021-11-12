package org.miage.placesearcher;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.miage.placesearcher.event.EventBusManager;
import org.miage.placesearcher.event.SearchResultEvent;
import org.miage.placesearcher.model.PlaceAddress;
import org.miage.placesearcher.model.PlaceSearchResult;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * Created by alexmorel on 05/01/2018.
 */

public class PlaceSearchService {

    private static final long REFRESH_DELAY = 650;
    public static PlaceSearchService INSTANCE = new PlaceSearchService();
    private final PlaceSearchRESTService mPlaceSearchRESTService;
    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mLastScheduleTask;

    private PlaceSearchService() {
        // Create GSON Converter that will be used to convert from JSON to Java
        Gson gsonConverter = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation().create();

        // Create Retrofit client
        Retrofit retrofit = new Retrofit.Builder()
                // Using OkHttp as HTTP Client
                .client(new OkHttpClient())
                // Having the following as server URL
                .baseUrl("https://api-adresse.data.gouv.fr")
                // Using GSON to convert from Json to Java
                .addConverterFactory(GsonConverterFactory.create(gsonConverter))
                .build();

        // Use retrofit to generate our REST service code
        mPlaceSearchRESTService = retrofit.create(PlaceSearchRESTService.class);
    }

    public void searchPlacesFromAddress(final String search) {
        // Cancel last scheduled network call (if any)
        if (mLastScheduleTask != null && !mLastScheduleTask.isDone()) {
            mLastScheduleTask.cancel(true);
        }

        // Schedule a network call in REFRESH_DELAY ms
        mLastScheduleTask = mScheduler.schedule(new Runnable() {
            public void run() {
                // Step 1 : first run a local search from DB and post result
                searchPlacesFromDB(search);

                // Step 2 : Call to the REST service
                mPlaceSearchRESTService.searchForPlaces(search).enqueue(new Callback<PlaceSearchResult>() {
                    @Override
                    public void onResponse(Call<PlaceSearchResult> call, Response<PlaceSearchResult> response) {
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().features != null) {
                            // Save all results in Database
                            ActiveAndroid.beginTransaction();
                            for (PlaceAddress place : response.body().features) {
                                // Set id for place & geometry
                                place.label = place.properties.label;
                                place.geometry.label = place.properties.label;
                                // Convert coordinates list to actual latitude/longitude fields
                                place.geometry.latitude = place.geometry.coordinates.get(1);
                                place.geometry.longitude = place.geometry.coordinates.get(0);
                                place.save();
                                place.geometry.save();
                                place.properties.save();
                            }
                            ActiveAndroid.setTransactionSuccessful();
                            ActiveAndroid.endTransaction();

                            // Send a new event with results from network
                            searchPlacesFromDB(search);
                        } else {
                            // Null result
                            // We may want to display a warning to user (e.g. Toast)

                            Log.e("[PlaceSearcher] [REST]", "Response error : null body");
                        }
                    }

                    @Override
                    public void onFailure(Call<PlaceSearchResult> call, Throwable t) {
                        // Request has failed or is not at expected format
                        // We may want to display a warning to user (e.g. Toast)
                        Log.e("[PlaceSearcher] [REST]", "Response error : " + t.getMessage());
                    }
                });
            }
        }, REFRESH_DELAY, TimeUnit.MILLISECONDS);
    }

    private void searchPlacesFromDB(String search) {
        // Get places matching the search from DB
        List<PlaceAddress> matchingPlacesFromDB = new Select().
                from(PlaceAddress.class)
                .where("label LIKE '%" + search + "%'")
                .orderBy("label")
                .execute();
        // Post result as an event
        EventBusManager.BUS.post(new SearchResultEvent(matchingPlacesFromDB));
    }

    // Service describing the REST APIs
    public interface PlaceSearchRESTService {

        @GET("search/")
        Call<PlaceSearchResult> searchForPlaces(@Query("q") String search);
    }
}
