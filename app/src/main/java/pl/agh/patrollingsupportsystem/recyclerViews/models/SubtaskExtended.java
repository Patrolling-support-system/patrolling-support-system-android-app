package pl.agh.patrollingsupportsystem.recyclerViews.models;

public class SubtaskExtended {
    String description;
    String subtaskName;

    public SubtaskExtended() {
    }

    public SubtaskExtended(String subtaskName) {
        this.subtaskName = subtaskName;
    }

    public SubtaskExtended(String description, String subtaskName) {
        this.description = description;
        this.subtaskName = subtaskName;
    }

    public String getDescription() {
        return description;
    }

    public String getSubtaskName() {
        return subtaskName;
    }
}
