package pl.agh.patrollingsupportsystem.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.w3c.dom.Text;

import pl.agh.patrollingsupportsystem.R;

public class SubtaskDetailsActivity extends AppCompatActivity {

    String subtaskDocumentId;
    String taskDocumentId;
    TextView tvSubtaskName, tvSubtaskDescription, tvLocation;
    Button btnAddReport;
    FirebaseFirestore fbDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtask_details);

        fbDb = FirebaseFirestore.getInstance();

        tvSubtaskName = findViewById(R.id.textViewSubtaskName);
        tvSubtaskDescription = findViewById(R.id.textViewSubtaskDescription);
        tvLocation = findViewById(R.id.textViewLocation);
        btnAddReport = findViewById(R.id.buttonAddReport);

        Bundle documentExtras = getIntent().getExtras();
        subtaskDocumentId = null;
        taskDocumentId = null;
        if (documentExtras != null) {
            subtaskDocumentId =  documentExtras.getString("subtask_document");
            taskDocumentId = documentExtras.getString("task_document");
        }

        btnAddReport.setOnClickListener(v ->
                startActivity(new Intent(SubtaskDetailsActivity.this, ReportForLocationActivity.class)
                        .putExtra("task_document", taskDocumentId)
                        .putExtra("subtask_document", subtaskDocumentId)));


        FirebaseData();
    }

    public void FirebaseData(){
        fbDb.collection("CheckpointSubtasks").document(subtaskDocumentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            GeoPoint checkpoint = document.get("checkpoint", GeoPoint.class);
                            tvSubtaskName.setText(document.getString("subtaskName"));
                            tvSubtaskDescription.setText(document.getString("description"));
                            assert checkpoint != null;
                            tvLocation.setText(checkpoint.getLatitude() + " | " + checkpoint.getLongitude());
                        } else {
                            Toast.makeText(this, "Document doesn't exist - Exception: " + task.getException(), Toast.LENGTH_LONG);
                        }
                    } else {
                        Toast.makeText(this, "Cannot fetch the data - Exception: " + task.getException(), Toast.LENGTH_LONG);
                    }
                });
    }
}