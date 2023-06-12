package pl.agh.patrollingsupportsystem.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.models.Task;
import pl.agh.patrollingsupportsystem.recyclerViews.tasks.TaskListAdapter;
import pl.agh.patrollingsupportsystem.recyclerViews.RecyclerViewInterface;

public class TaskListActivity extends AppCompatActivity implements RecyclerViewInterface {

    RecyclerView rvTaskList;
    FirebaseFirestore fbDb;
    FirebaseAuth fbAuth;
    TaskListAdapter taskListAdapter;
    ArrayList<Task> taskList;
    ArrayList<String> taskDocumentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        //Firebase properties
        fbDb = FirebaseFirestore.getInstance();
        fbAuth = FirebaseAuth.getInstance();

        //Lists for RecyclerView
        taskList = new ArrayList<>();
        taskDocumentList = new ArrayList<>();

        //Layout elements and properties
        taskListAdapter = new TaskListAdapter(this, taskList, this);
        rvTaskList = findViewById(R.id.recyclerViewTaskList);
        rvTaskList.setHasFixedSize(true);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        rvTaskList.setAdapter(taskListAdapter);

        EventChangeListener();
    }

    private void EventChangeListener() {
        fbDb.collection("Tasks")
                .whereArrayContains("patrolParticipants", fbAuth.getCurrentUser().getUid())
                .whereGreaterThanOrEqualTo("endDate", new Date(System.currentTimeMillis()))
                .addSnapshotListener((value, error) -> {
                    if (error != null){
                        Log.e("Firestore error ", error.getMessage());
                        return ;
                    }
                    for (DocumentChange dc: value.getDocumentChanges()){
                        if (dc.getType() == DocumentChange.Type.ADDED){
                            taskList.add(dc.getDocument().toObject(Task.class));
                            taskDocumentList.add(dc.getDocument().getId());
                        }
                        taskListAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        startActivity(new Intent(TaskListActivity.this, TaskDetailsActivity.class)
                .putExtra("task_document", taskDocumentList.get(position)));
    }
}

