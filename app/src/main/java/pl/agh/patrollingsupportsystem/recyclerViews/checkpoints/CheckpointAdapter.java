package pl.agh.patrollingsupportsystem.recyclerViews.checkpoints;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.RecyclerViewInterface;
import pl.agh.patrollingsupportsystem.recyclerViews.checkpoints.nestedSubtasks.SubtaskAdapter;
import pl.agh.patrollingsupportsystem.recyclerViews.models.SubtaskExtended;

public class CheckpointAdapter extends RecyclerView.Adapter<CheckpointAdapter.ViewHolder>{
    FirebaseFirestore fbDb = FirebaseFirestore.getInstance();
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    String fbUserId = fbAuth.getCurrentUser().getUid();
    Context context;
    RecyclerViewInterface recyclerViewInterface;
    private List<GeoPoint> checkpoints;
    private List<String> checkpointNames;
    private String taskId;


    public CheckpointAdapter(Context context, List<GeoPoint> checkpoints, List<String> checkpointNames, String taskId, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.checkpoints = checkpoints;
        this.checkpointNames = checkpointNames;
        this.taskId = taskId;
        this.recyclerViewInterface = recyclerViewInterface;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_list, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeoPoint checkpoint = checkpoints.get(position);
        holder.rvSubtasks.setLayoutManager(new LinearLayoutManager(context));
        holder.rvSubtasks.setHasFixedSize(true);


        holder.textViewCheckpointName.setText(checkpointNames.get(position));
        List<SubtaskExtended> subtasks = new ArrayList<>();
        SubtaskAdapter subtaskAdapter = new SubtaskAdapter(subtasks, holder.rvSubtasks.getContext());
        fbDb.collection("CheckpointSubtasks")
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("patrolParticipantId", fbUserId)
                .whereEqualTo("checkpoint", checkpoint)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            SubtaskExtended subtask = document.toObject(SubtaskExtended.class);
                            subtasks.add(subtask);
                            subtaskAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }

                    holder.rvSubtasks.setAdapter(subtaskAdapter);
                });

    }


    @Override
    public int getItemCount() {
        return checkpoints.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewCheckpointName;
        RecyclerView rvSubtasks;

        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface){
            super(itemView);
            textViewCheckpointName = itemView.findViewById(R.id.textViewCheckpointName);
            rvSubtasks = itemView.findViewById(R.id.recyclerViewSubtask);

            itemView.setOnClickListener(view -> {
                if(recyclerViewInterface != null){
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}

