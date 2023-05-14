package pl.agh.patrollingsupportsystem.recyclerViews.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class TaskCheckpoints {
    double latitude;
    double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
