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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import pl.agh.patrollingsupportsystem.R;

public class ActionDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    TextView locationData;
    Button addReportButton;
    Button btnCoordinatorChat;
    String coordinator;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_details);

        Button btnMap = (Button)findViewById(R.id.mapButton);
        btnCoordinatorChat = findViewById(R.id.coordinatorChat);


        btnMap.setOnClickListener(v -> startActivity(new Intent(ActionDetailsActivity.this, MapsActivityCurrentPlace.class)));
        btnCoordinatorChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ActionDetailsActivity.this, ChatActivity.class);
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

        db.collection("ActionList").document(documentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                locationData.setText(document.getString("location"));
                                coordinator = document.getString("coordinator");
                            } else {
                                // document doesn't exist - Toast?
                            }
                        } else {
                            // document downloading error - Toast?
                        }
                    }
                });

        addReportButton.setOnClickListener(v -> {
            startActivity(new Intent(ActionDetailsActivity.this, ReportForLocationActivity.class));
        });


    }
}