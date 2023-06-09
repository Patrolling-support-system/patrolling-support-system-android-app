package pl.agh.patrollingsupportsystem.recyclerViews.models;

import com.google.firebase.Timestamp;

public class Task {
    String name;
    String location;
    Timestamp startDate;

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Timestamp getStartDate() {
        return startDate;
    }
}
