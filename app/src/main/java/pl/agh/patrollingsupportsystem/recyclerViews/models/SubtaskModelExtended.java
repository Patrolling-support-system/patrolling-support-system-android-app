package pl.agh.patrollingsupportsystem.recyclerViews.models;

public class SubtaskModelExtended {
    String description;
    String subtaskName;

    public SubtaskModelExtended() {
    }

    public SubtaskModelExtended(String subtaskName) {
        this.subtaskName = subtaskName;
    }

    public SubtaskModelExtended(String description, String subtaskName) {
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
