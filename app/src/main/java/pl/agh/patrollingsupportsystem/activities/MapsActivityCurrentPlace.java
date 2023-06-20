package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.libraries.places.api.Places;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.agh.patrollingsupportsystem.BuildConfig;
import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.activities.map.CheckpointInfoWindowAdapter;

public class MapsActivityCurrentPlace extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;

    // A default location (Poland, Cracow) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(50.06192492003556, 19.93918752197243);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSION_FINE_LOCATION = 1;
    private LocationRequest locationRequest;
    private static final long MIN_TIME = 60000;
    private static final long MIN_DISTANCE = 5;
    LocationCallback locationCallback;
    private String taskId;
    private static final int COLOR_GREEN_ARGB = 0xffa9ac5d;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle documentExtras = getIntent().getExtras();
        if (documentExtras != null) {
            taskId = documentExtras.getString("taskId");
        }

        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        locationRequest = getLocationRequest();

        beginUpdates();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    private void beginUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @SuppressLint("MissingPermission")
    private void updateLocation(Location locationCall) {
        if (hasLocationPermission()) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateLocationUI(locationCall);
                    sentLocationToFirebase(locationCall);
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_FINE_LOCATION);
            if (map != null) {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            }
        }
    }

    @SuppressLint({"MissingPermission", "ServiceCast"})
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        setCheckpointsOnMap();
        showRoute();
    }

    private void setCheckpointsOnMap() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<GeoPoint, MarkerOptions> markers = new HashMap<>();
        db.collection("Tasks").document(taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<GeoPoint> checkpoints = (List<GeoPoint>) document.get("checkpoints");
                            List<String> checkpointNames = (List<String>) document.get("checkpointNames");
                            for (int i=0; i<checkpoints.size(); i++) {
                                MarkerOptions marker = new MarkerOptions()
                                        .position(new LatLng(checkpoints.get(i).getLatitude(), checkpoints.get(i).getLongitude()))
                                        .title(checkpointNames.get(i))
                                        .alpha(0.9F)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                markers.put(checkpoints.get(i), marker);
                            }

                        } else {
                            Toast.makeText(this, "Document doesn't exist - Exception: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Cannot fetch the data - Exception: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });

        db.collection("CheckpointSubtasks").whereEqualTo("taskId", taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot qquery = task.getResult();
                        if (!qquery.isEmpty()) {
                            for (DocumentSnapshot doc : qquery.getDocuments()) {
                                GeoPoint point = (GeoPoint) doc.get("checkpoint");
                                String snippet = (String) doc.get("subtaskName");
                                String participantId = ((String) doc.get("participant")).replaceAll("\\s", "");
                                MarkerOptions marker = markers.get(point);
                                if (marker != null && snippet != null && participantId.equalsIgnoreCase(userId)) {
                                    if (marker.getSnippet() != null) {
                                        String newSnippet = marker.getSnippet()+ "\n" + snippet;
                                        marker.snippet(newSnippet);
                                    } else {
                                        marker.snippet(snippet);
                                    }
                                }
                            }
                            for (MarkerOptions marker : markers.values()) {
                                map.addMarker(marker);
                            }
                            map.setInfoWindowAdapter(new CheckpointInfoWindowAdapter(MapsActivityCurrentPlace.this));
                            map.setOnInfoWindowClickListener(v -> startActivity(new Intent(MapsActivityCurrentPlace.this, SubtaskListActivity.class)
                                    .putExtra("checkpoint_latitude", v.getPosition().latitude)
                                    .putExtra("checkpoint_longitude", v.getPosition().longitude)
                                    .putExtra("task_document", taskId)
                                    .putExtra("checkpoint_name", v.getTitle())));

                        } else {
                            Toast.makeText(this, "Document doesn't exist - Exception: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Cannot fetch the data - Exception: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showRoute() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("RoutePoint").whereEqualTo("taskId", taskId)
                .get()
                .addOnCompleteListener(task -> {
                    Map<Timestamp, LatLng> map1 = new HashMap<>();
                    List<LatLng> points = new ArrayList<>();
                    if (task.isSuccessful()) {
                        QuerySnapshot qquery = task.getResult();
                        if (!qquery.isEmpty()) {
                            for (DocumentSnapshot doc : qquery.getDocuments()) {
                                String participantId = (String) doc.get("patrolParticipantId");
                                assert participantId != null;
                                if (participantId.equalsIgnoreCase(userId)) {
                                    GeoPoint geoPoint = (GeoPoint) doc.get("location");
                                    assert geoPoint != null;
                                    LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                    map1.put((Timestamp) doc.get("date"), latLng);
                                }
                            }
                            List<Timestamp> time = new ArrayList<>(map1.keySet());
                            Collections.sort(time);
                            for (Timestamp t : time) {
                                points.add(map1.get(t));
                            }
                            LatLng[] list = points.toArray(new LatLng[0]);
                            Polyline polyline = map.addPolyline(new PolylineOptions()
                                    .clickable(false)
                                    .add(list));
                            polyline.setColor(COLOR_GREEN_ARGB);
                        } else {
                            Toast.makeText(this, "Document doesn't exist - Exception: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Cannot fetch the data - Exception: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void updateLocationUI(Location location) {
        if (map == null) {
            return;
        }
        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),
                            location.getLongitude()), DEFAULT_ZOOM));
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void sentLocationToFirebase(Location location) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        GeoPoint currLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        Map<String, Object> checkpointReport = new HashMap<>();
        checkpointReport.put("location", currLocation);
        checkpointReport.put("patrolParticipantId", userId);
        checkpointReport.put("taskId", taskId);
        checkpointReport.put("date", Timestamp.now());

        db.collection("RoutePoint").add(checkpointReport)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot written");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    protected LocationRequest getLocationRequest() {
        return new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, MIN_TIME)
                .setMinUpdateDistanceMeters(MIN_DISTANCE)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setWaitForAccurateLocation(true)
                .build();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

}