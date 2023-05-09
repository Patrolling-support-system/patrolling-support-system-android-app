package pl.agh.patrollingsupportsystem.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import pl.agh.patrollingsupportsystem.R;

public class TaskDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    TextView locationData, tvTaskName, tvTaskDescription, tvStartDate, tvEndDate;
    Button addReportButton;
    Button btnCoordinatorChat;
    String coordinator;

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


    }
}