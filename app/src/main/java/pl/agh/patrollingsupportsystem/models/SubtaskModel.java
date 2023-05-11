package pl.agh.patrollingsupportsystem.models;

public class SubtaskModel {
    String subtaskName;

    public SubtaskModel() {
    }

    public SubtaskModel(String subtaskName) {
        this.subtaskName = subtaskName;
    }

    public String getSubtaskName() {
        return subtaskName;
    }
}
