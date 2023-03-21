package pl.agh.patrollingsupportsystem.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_details);

        locationData = findViewById(R.id.locationData);
        db = FirebaseFirestore.getInstance();

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
                            } else {
                                // document doesn't exist - Toast?
                            }
                        } else {
                            // document downloading error - Toast?
                        }
                    }
                });


    }
}