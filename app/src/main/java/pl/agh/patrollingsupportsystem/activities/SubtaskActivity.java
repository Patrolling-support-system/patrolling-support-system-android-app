package pl.agh.patrollingsupportsystem.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties.CheckpointRvInterface;
import pl.agh.patrollingsupportsystem.models.SubtaskModelExtended;
import pl.agh.patrollingsupportsystem.recyclerViewProperties.TaskModel;
import pl.agh.patrollingsupportsystem.subtaskListRecyclerView.SubtaskListAdapter;

public class SubtaskActivity extends AppCompatActivity implements CheckpointRvInterface {

    TextView tvCheckpointCoordinates;
    RecyclerView rvSubtasks;
    FirebaseFirestore db;
    List<SubtaskModelExtended> subtasks;
    List<String> documents;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtask);

        db = FirebaseFirestore.getInstance();
        subtasks = new ArrayList<>();
        documents = new ArrayList<>();

        Bundle documentExtras = getIntent().getExtras();
        Double latitude = null;
        Double longitude = null;
        if (documentExtras != null) {
            latitude = documentExtras.getDouble("checkpoint_latitude");
            longitude = documentExtras.getDouble("checkpoint_longitude");
        }
        System.out.println(latitude + " " + longitude);

        rvSubtasks = findViewById(R.id.recyclerViewSubtaskList);
        rvSubtasks.setLayoutManager(new LinearLayoutManager(this));

        tvCheckpointCoordinates = findViewById(R.id.textViewCheckpointCoordinates);
        tvCheckpointCoordinates.setText(latitude + " | " + longitude);
        GeoPoint checkpoint = new GeoPoint(latitude, longitude);


        db.collection("CheckpointSubtasks").whereEqualTo("checkpoint", checkpoint)
                .addSnapshotListener((value, error) -> {
                    if (error != null){
                        Log.e("Firestore error ", error.getMessage());
                        return ;
                    }
                    for (DocumentChange dc: value.getDocumentChanges()){
                        if (dc.getType() == DocumentChange.Type.ADDED){
                            //tvCheckpointCoordinates.setText(dc.getDocument().get("SubtaskName").toString());
                            subtasks.add(dc.getDocument().toObject(SubtaskModelExtended.class));
                            documents.add(dc.getDocument().getId());
                        }

                    }
                });
        SubtaskListAdapter subtaskListAdapter = new SubtaskListAdapter( this, subtasks, this);
        rvSubtasks.setAdapter(subtaskListAdapter);

    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent(SubtaskActivity.this, SubtaskDetailsActivity.class);
        i.putExtra("subtask_document", documents.get(position));
        startActivity(i);
    }
}