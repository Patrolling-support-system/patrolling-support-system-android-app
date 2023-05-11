package pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties;

import static android.content.ContentValues.TAG;

import android.accounts.AbstractAccountAuthenticator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties.subtaskRecyclerViewProperties.SubtaskAdapter;
import pl.agh.patrollingsupportsystem.models.SubtaskModel;

public class CheckpointAdapter extends RecyclerView.Adapter<CheckpointAdapter.ViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Context context;
    CheckpointRvInterface checkpointRvInterface;

    public CheckpointAdapter(Context context, List<GeoPoint> checkpoints, CheckpointRvInterface checkpointRvInterface) {
        this.context = context;
        this.checkpoints = checkpoints;
        this.checkpointRvInterface = checkpointRvInterface;
    }

    private List<GeoPoint> checkpoints;

    public CheckpointAdapter(List<GeoPoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeoPoint checkpoint = checkpoints.get(position);

        holder.rvSubtasks.setLayoutManager(new LinearLayoutManager(context));
        holder.rvSubtasks.setHasFixedSize(true);

        holder.textViewCheckpointName.setText(String.valueOf(checkpoint.getLatitude()) + " | " + String.valueOf(checkpoint.getLongitude()));
        List<SubtaskModel> subtasks = new ArrayList<>();

        db.collection("CheckpointSubtasks").whereEqualTo("checkpoint", checkpoint)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            SubtaskModel subtask = document.toObject(SubtaskModel.class);
                            subtasks.add(subtask);
                            if (subtasks.size() != 0){
                                for (SubtaskModel subtask1 : subtasks){
                                    System.out.println(subtask1.getSubtaskName());
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    SubtaskAdapter subtaskAdapter = new SubtaskAdapter(subtasks, holder.rvSubtasks.getContext(), checkpointRvInterface);
                    holder.rvSubtasks.setAdapter(subtaskAdapter);
                });
        //subtasks.add(new SubtaskModel("test"));
        //System.out.println(subtasks.size());

    }

    @Override
    public int getItemCount() {
        return checkpoints.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCheckpointName;
        RecyclerView rvSubtasks;
        //TextView longitudeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCheckpointName = itemView.findViewById(R.id.textViewCheckpointName);
            rvSubtasks = itemView.findViewById(R.id.recyclerViewSubtask);
            //longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
        }
    }
}

