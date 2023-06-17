package pl.agh.patrollingsupportsystem.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSION_FINE_LOCATION = 1;
    private GeoPoint checkpointLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtask_details);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        fbDb = FirebaseFirestore.getInstance();

        tvSubtaskName = findViewById(R.id.textViewSubtaskName);
        tvSubtaskDescription = findViewById(R.id.textViewSubtaskDescription);
        tvLocation = findViewById(R.id.textViewLocation);
        btnAddReport = findViewById(R.id.buttonAddReport);

        Bundle documentExtras = getIntent().getExtras();
        subtaskDocumentId = null;
        taskDocumentId = null;
        if (documentExtras != null) {
            subtaskDocumentId = documentExtras.getString("subtask_document");
            taskDocumentId = documentExtras.getString("task_document");
        }
        showReportButtonIfNearCheckpoint();
    }

    @SuppressLint("MissingPermission")
    public void showReportButtonIfNearCheckpoint() {
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
                            if (hasLocationPermission()) {
                                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        GeoPoint currLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                                        double distance = distanceInMeters(currLocation.getLatitude(), currLocation.getLongitude(),
                                                checkpoint.getLatitude(), checkpoint.getLongitude());
                                        System.out.println(distance);
                                        if (distance < 10.0) {
                                            // trzeba przebudować report, żeby przyjmował koordynaty checkpointa, jeśli przekazano null to wtedy z obecną lokalizacją
                                            btnAddReport.setOnClickListener(v ->
                                                    startActivity(new Intent(SubtaskDetailsActivity.this, ReportForLocationActivity.class)
                                                            .putExtra("task_document", taskDocumentId)
                                                            .putExtra("subtask_document", subtaskDocumentId)
                                                            .putExtra("checkpoint_latitude", checkpoint.getLatitude())
                                                            .putExtra("checkpoint_longitude", checkpoint.getLongitude())));
                                        }
                                        else {
                                            btnAddReport.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v)  {
                                                    Toast.makeText(getBaseContext(), "You have to be not more than 10 meters from checkpoint" , Toast.LENGTH_SHORT ).show();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(this, "Document doesn't exist - Exception: " + task.getException(), Toast.LENGTH_LONG);
                        }
                    } else {
                        Toast.makeText(this, "Cannot fetch the data - Exception: " + task.getException(), Toast.LENGTH_LONG);
                    }
                });

    }

    @SuppressLint("MissingPermission")
    private void getLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_FINE_LOCATION);
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        return dist * 1000;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}