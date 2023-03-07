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

public class ActionListAdapter extends RecyclerView.Adapter<ActionListAdapter.ActionViewHolder> {

    Context context;

    public ActionListAdapter(Context context, ArrayList<ActionGeneral> list) {
        this.context = context;
        this.list = list;
    }

    ArrayList<ActionGeneral> list;

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.action_list_item, parent, false);
        return new ActionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        ActionGeneral actionGeneral= list.get(position);
        holder.name.setText(actionGeneral.getActionName());
        holder.someData.setText(actionGeneral.getSomeData());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ActionViewHolder extends RecyclerView.ViewHolder{

        TextView name, someData;
        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvActionName);
            someData = itemView.findViewById(R.id.tvSomeData);
        }
    }
}
