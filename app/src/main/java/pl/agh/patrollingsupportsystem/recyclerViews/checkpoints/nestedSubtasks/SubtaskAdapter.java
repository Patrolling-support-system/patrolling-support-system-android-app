package pl.agh.patrollingsupportsystem.recyclerViews.checkpoints.nestedSubtasks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.models.SubtaskExtended;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.ViewHolder>{

    private List<SubtaskExtended> subtasksList;
    Context context;

    public SubtaskAdapter(List<SubtaskExtended> subtasksList, Context context) {
        this.subtasksList = subtasksList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubtaskExtended currentItem = subtasksList.get(position);
        holder.tvSubtaskName.setText(currentItem.getSubtaskName());
    }

    @Override
    public int getItemCount() {
        return subtasksList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvSubtaskName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubtaskName = itemView.findViewById(R.id.textViewSubtaskName);
        }
    }
}
