package pl.agh.patrollingsupportsystem.activities;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.FieldValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties.CheckpointAdapter;
import pl.agh.patrollingsupportsystem.models.TaskCheckpoints;

public class TaskDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    TextView locationData, tvTaskName, tvTaskDescription, tvStartDate, tvEndDate;
    Button addReportButton;
    Button btnCoordinatorChat;
    String coordinator;

    //Checkpoints
    RecyclerView rvCheckpointList;
    CheckpointAdapter checkpointAdapter;
    List<GeoPoint> checkpoints;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        tvTaskName = findViewById(R.id.textViewTaskName);
        tvTaskDescription = findViewById(R.id.textViewTaskDescription);
        tvStartDate = findViewById(R.id.textViewStartDate);
        tvEndDate = findViewById(R.id.textViewEndDate);

        Button btnMap = (Button)findViewById(R.id.mapButton);
        btnCoordinatorChat = findViewById(R.id.coordinatorChat);


        btnMap.setOnClickListener(v -> startActivity(new Intent(TaskDetailsActivity.this, MapsActivityCurrentPlace.class)));
        btnCoordinatorChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TaskDetailsActivity.this, ChatActivity.class);
                i.putExtra("coordinator", coordinator);
                startActivity(i);
            }
        });

        locationData = findViewById(R.id.locationData);
        db = FirebaseFirestore.getInstance();
        addReportButton = findViewById(R.id.addReportButton);

        Bundle documentExtras = getIntent().getExtras();
        String documentId = null;
        if (documentExtras != null) {
            documentId = documentExtras.getString("documentId");
        }

        db.collection("Tasks").document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            locationData.setText(document.getString("location"));
                            coordinator = document.getString("coordinator");
                            tvTaskName.setText(document.getString("name"));
                            tvTaskDescription.setText(document.getString("taskDescription"));
                            tvStartDate.setText(document.getDate("startDate").toString());
                            tvEndDate.setText(document.getDate("endDate").toString());
                        } else {
                            // document doesn't exist - Toast?
                        }
                    } else {
                        // document downloading error - Toast?
                    }
                });

        addReportButton.setOnClickListener(v -> {
            startActivity(new Intent(TaskDetailsActivity.this, ReportForLocationActivity.class));
        });

        //RecyclerCheckpointView
        rvCheckpointList = findViewById(R.id.recyclerViewCheckpointList);
        checkpoints = new ArrayList<>();
        checkpointAdapter = new CheckpointAdapter(checkpoints);
        rvCheckpointList.setAdapter(checkpointAdapter);
        rvCheckpointList.setLayoutManager(new LinearLayoutManager(this));

        CollectionReference tasksRef = db.collection("Tasks");
        DocumentReference taskDocRef = tasksRef.document(documentId);

        taskDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Pobranie pola checkpoints z dokumentu
                    List<GeoPoint> checkpointsData = (List<GeoPoint>) documentSnapshot.get("checkpoints");

                    if (checkpointsData != null) {
                        // Aktualizacja listy checkpoints i powiadomienie adaptera
                        checkpoints.clear();
                        checkpoints.addAll(checkpointsData);
                        checkpointAdapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Obsługa błędu pobierania danych
            }
        });


    }

}