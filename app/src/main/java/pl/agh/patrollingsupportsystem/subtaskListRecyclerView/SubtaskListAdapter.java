package pl.agh.patrollingsupportsystem.subtaskListRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties.CheckpointRvInterface;
import pl.agh.patrollingsupportsystem.models.SubtaskModelExtended;

public class SubtaskListAdapter extends RecyclerView.Adapter<SubtaskListAdapter.ViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Context context;
    CheckpointRvInterface checkpointRvInterface;
    List<SubtaskModelExtended> subtasks;

    public SubtaskListAdapter(Context context, List<SubtaskModelExtended> subtasks, CheckpointRvInterface checkpointRvInterface) {
        this.context = context;
        this.subtasks = subtasks;
        this.checkpointRvInterface = checkpointRvInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask_extended, parent, false);
        return new SubtaskListAdapter.ViewHolder(view, checkpointRvInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubtaskModelExtended currentSubtask = subtasks.get(position);

        holder.tvSubtaskName.setText(currentSubtask.getSubtaskName());
        holder.tvSubtaskDescription.setText(currentSubtask.getDescription());

    }


    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvSubtaskName;
        TextView tvSubtaskDescription;

        public ViewHolder(@NonNull View itemView, CheckpointRvInterface checkpointRvInterface) {
            super(itemView);
            tvSubtaskName = itemView.findViewById(R.id.textViewSubtaskName);
            tvSubtaskDescription = itemView.findViewById(R.id.textViewSubtaskDescription);

            itemView.setOnClickListener(view -> {
                if(checkpointRvInterface != null){
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION){
                        checkpointRvInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}
