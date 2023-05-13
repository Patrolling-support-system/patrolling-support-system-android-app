package pl.agh.patrollingsupportsystem.recyclerViewProperties;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pl.agh.patrollingsupportsystem.R;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ActionViewHolder> {
    RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<TaskModel> list;

    public TaskListAdapter(Context context, ArrayList<TaskModel> list, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.list = list;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_task_list, parent, false);
        return new ActionViewHolder(v, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        TaskModel taskModel = list.get(position);
        holder.name.setText(taskModel.getName());
        holder.someData.setText(taskModel.getLocation());
        holder.startDate.setText(taskModel.getStartDate().toDate().toString());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ActionViewHolder extends RecyclerView.ViewHolder{

        TextView name, someData, startDate;

        public TextView getName() {
            return name;
        }

        public ActionViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            name = itemView.findViewById(R.id.textViewTaskName);
            someData = itemView.findViewById(R.id.textViewTaskLocation);
            startDate = itemView.findViewById(R.id.textViewTaskStartDate);

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
