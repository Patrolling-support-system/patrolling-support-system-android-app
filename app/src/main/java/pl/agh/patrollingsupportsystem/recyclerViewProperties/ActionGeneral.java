package pl.agh.patrollingsupportsystem.recyclerViewProperties;

import com.google.firebase.Timestamp;

public class ActionGeneral {
//Variables and getters for ActionProperties
    String name, location;
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
