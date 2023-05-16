package pl.agh.patrollingsupportsystem.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.checkpointsRecyclerViewProperties.CheckpointAdapter;
import pl.agh.patrollingsupportsystem.recyclerViews.checkpointsRecyclerViewProperties.RecyclerViewInterface;

public class TaskDetailsActivity extends AppCompatActivity implements RecyclerViewInterface {

    TextView tvTaskName;
    TextView tvTaskDescription;
    TextView tvLocation;
    TextView tvStartDate;
    TextView tvEndDate;
    Button btnCoordinatorChat;
    Button btnAddReport;
    Button btnMap;
    String coordinator;
    FirebaseFirestore fbDb;

    //Used for RecyclerView
    RecyclerView rvCheckpointList;
    CheckpointAdapter checkpointAdapter;
    List<GeoPoint> checkpointList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        //Catch extras
        Bundle documentExtras = getIntent().getExtras();
        String taskDocumentId = null;
        if (documentExtras != null) {
            taskDocumentId = documentExtras.getString("task_document");
        }
        String finalTaskDocumentId = taskDocumentId;

        //Layout elements
        tvTaskName = findViewById(R.id.textViewTaskName);
        tvTaskDescription = findViewById(R.id.textViewTaskDescription);
        tvLocation = findViewById(R.id.textViewLocation);
        tvStartDate = findViewById(R.id.textViewStartDate);
        tvEndDate = findViewById(R.id.textViewEndDate);
        btnCoordinatorChat = findViewById(R.id.buttonCoordinatorChat);
        btnAddReport = findViewById(R.id.buttonAddReport);
        btnMap = findViewById(R.id.mapButton);

        fbDb = FirebaseFirestore.getInstance();

        //Button onClick functionalities
        btnCoordinatorChat.setOnClickListener(v ->
            startActivity(new Intent(TaskDetailsActivity.this, ChatActivity.class)
                    .putExtra("coordinator", coordinator)
                    .putExtra("task_document", finalTaskDocumentId))
        );
        btnAddReport.setOnClickListener(v -> {
            Intent i = new Intent(TaskDetailsActivity.this, ReportForLocationActivity.class);
            i.putExtra("task_document", finalTaskDocumentId);
            startActivity(i);
        });
        btnMap.setOnClickListener(v -> startActivity(new Intent(TaskDetailsActivity.this, MapsActivityCurrentPlace.class)));


        fbDb.collection("Tasks").document(taskDocumentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            tvLocation.setText(document.getString("location"));
                            coordinator = document.getString("coordinator");
                            tvTaskName.setText(document.getString("name"));
                            tvTaskDescription.setText(document.getString("taskDescription"));
                            tvStartDate.setText(document.getDate("startDate").toString());
                            tvEndDate.setText(document.getDate("endDate").toString());
                        } else {
                            Toast.makeText(this, "Document doesn't exist - Exception: " + task.getException(), Toast.LENGTH_LONG);
                        }
                    } else {
                        Toast.makeText(this, "Cannot fetch the data - Exception: " + task.getException(), Toast.LENGTH_LONG);
                    }
                });


        //RecyclerCheckpointView
        rvCheckpointList = findViewById(R.id.recyclerViewCheckpointList);
        checkpointList = new ArrayList<>();
        checkpointAdapter = new CheckpointAdapter(this, checkpointList, this);
        rvCheckpointList.setAdapter(checkpointAdapter);
        rvCheckpointList.setLayoutManager(new LinearLayoutManager(this));
        rvCheckpointList.setHasFixedSize(true);

        CollectionReference taskCollectionReference = fbDb.collection("Tasks");
        DocumentReference taskDocumentReference = taskCollectionReference.document(taskDocumentId);

        taskDocumentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<GeoPoint> tempCheckpointList = (List<GeoPoint>) documentSnapshot.get("checkpoints");
                if (tempCheckpointList != null) {
                    checkpointList.clear();
                    checkpointList.addAll(tempCheckpointList);
                    checkpointAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Database issue: " + e.getMessage().toString(), Toast.LENGTH_LONG).show());


    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent(TaskDetailsActivity.this, SubtaskListActivity.class);
        i.putExtra("checkpoint_latitude", checkpointList.get(position).getLatitude());
        i.putExtra("checkpoint_longitude", checkpointList.get(position).getLongitude());
        startActivity(i);
    }
}