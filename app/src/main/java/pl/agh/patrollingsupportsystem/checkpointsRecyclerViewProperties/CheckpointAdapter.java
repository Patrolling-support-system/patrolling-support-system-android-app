package pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

import pl.agh.patrollingsupportsystem.R;

public class CheckpointAdapter extends RecyclerView.Adapter<CheckpointAdapter.ViewHolder> {

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
        holder.textViewCheckpointName.setText(String.valueOf(checkpoint.getLatitude()) + " | " + String.valueOf(checkpoint.getLongitude()));
        //holder.longitudeTextView.setText(String.valueOf(checkpoint.getLongitude()));
    }

    @Override
    public int getItemCount() {
        return checkpoints.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCheckpointName;
        //TextView longitudeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCheckpointName = itemView.findViewById(R.id.textViewCheckpointName);
            //longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
        }
    }
}

