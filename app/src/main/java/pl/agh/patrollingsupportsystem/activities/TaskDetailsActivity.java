package pl.agh.patrollingsupportsystem.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.RecyclerViewInterface;
import pl.agh.patrollingsupportsystem.recyclerViews.checkpoints.CheckpointAdapter;

public class TaskDetailsActivity extends AppCompatActivity implements RecyclerViewInterface {

    private TextView tvTaskName, tvTaskDescription, tvLocation, tvStartDate, tvEndDate;
    private Button btnCoordinatorChat, btnAddReport, btnMap;
    private String coordinator;
    private FirebaseFirestore fbDb;
    private String taskDocumentIdExtras;
    //Used for RecyclerView
    private RecyclerView rvCheckpointList;
    private CheckpointAdapter checkpointAdapter;
    private List<GeoPoint> checkpointList;
    private List<String> checkpointNamesList;

    @SuppressLint({"MissingInflatedId", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        //Catch and set extras
        Bundle documentExtras = getIntent().getExtras();
        String taskDocumentId = null;
        if (documentExtras != null) {
            taskDocumentId = documentExtras.getString("task_document");
        }
        String finalTaskDocumentId = taskDocumentId;
        taskDocumentIdExtras = taskDocumentId;

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

        btnCoordinatorChat.setOnClickListener(v ->
                startActivity(new Intent(TaskDetailsActivity.this, ChatActivity.class)
                        .putExtra("coordinator", coordinator)
                        .putExtra("task_document", finalTaskDocumentId))
        );
        btnAddReport.setOnClickListener(v ->
                startActivity(new Intent(TaskDetailsActivity.this, ReportForLocationActivity.class)
                        .putExtra("task_document", finalTaskDocumentId))
        );

        btnMap.setOnClickListener(v -> startActivity(new Intent(TaskDetailsActivity.this, MapsActivityCurrentPlace.class).putExtra("taskId", finalTaskDocumentId)));

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
                            tvStartDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Objects.requireNonNull(document.getDate("startDate"))));
                            tvEndDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Objects.requireNonNull(document.getDate("endDate"))));
                        } else {
                            Toast.makeText(this, "Document doesn't exist - Exception: " + task.getException(), Toast.LENGTH_LONG);
                        }
                    } else {
                        Toast.makeText(this, "Cannot fetch the data - Exception: " + task.getException(), Toast.LENGTH_LONG);
                    }
                });

        //RecyclerCheckpointView
        rvCheckpointList = findViewById(R.id.recyclerViewCheckpointList);
        ViewCompat.setNestedScrollingEnabled(rvCheckpointList, false);
        checkpointList = new ArrayList<>();
        checkpointNamesList = new ArrayList<>();
        checkpointAdapter = new CheckpointAdapter(this, checkpointList, checkpointNamesList, this);
        rvCheckpointList.setAdapter(checkpointAdapter);
        rvCheckpointList.setLayoutManager(new LinearLayoutManager(this));
        rvCheckpointList.setHasFixedSize(true);

        CollectionReference taskCollectionReference = fbDb.collection("Tasks");
        DocumentReference taskDocumentReference = taskCollectionReference.document(taskDocumentId);

        taskDocumentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<GeoPoint> tempCheckpointList = (List<GeoPoint>) documentSnapshot.get("checkpoints");
                List<String> tempCheckpointNamesList = (List<String>) documentSnapshot.get("checkpointNames");
                if (tempCheckpointList != null) {
                    checkpointList.clear();
                    checkpointList.addAll(tempCheckpointList);
                    checkpointNamesList.clear();
                    checkpointNamesList.addAll(tempCheckpointNamesList);
                    checkpointAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Database issue: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onItemClick(int position) {
        startActivity(new Intent(TaskDetailsActivity.this, SubtaskListActivity.class)
                .putExtra("checkpoint_latitude", checkpointList.get(position).getLatitude())
                .putExtra("checkpoint_longitude", checkpointList.get(position).getLongitude())
                .putExtra("task_document", taskDocumentIdExtras)
                .putExtra("checkpoint_name", checkpointNamesList.get(position)));
    }
}