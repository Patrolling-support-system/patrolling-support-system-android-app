package pl.agh.patrollingsupportsystem.activities.map;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.activities.AccountDetailsActivity;
import pl.agh.patrollingsupportsystem.activities.MapsActivityCurrentPlace;
import pl.agh.patrollingsupportsystem.activities.MenuActivity;
import pl.agh.patrollingsupportsystem.activities.SubtaskListActivity;
import pl.agh.patrollingsupportsystem.activities.TaskDetailsActivity;

public class CheckpointInfoWindowAdapter extends AppCompatActivity implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private final View view;

    public CheckpointInfoWindowAdapter(Context context) {
        this.context = context.getApplicationContext();
        view = LayoutInflater.from(context).inflate(R.layout.marker_custom_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, view);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, view);
        return view;
    }

    private void rendowWindowText(Marker marker, View v) {
        String title = marker.getTitle();
        String subtasks = marker.getSnippet();
        TextView tvTitle = (TextView) v.findViewById(R.id.checkpointTitle);
        TextView tvSubtasks = (TextView) v.findViewById(R.id.subtasks);

        System.out.println("Info Info");

        if (title != null && !title.equals("")) {
            tvTitle.setText(title);
            System.out.println(title);
        } else {
            tvTitle.setText("");
        }
        if (subtasks != null && !subtasks.equals("")) {
            tvSubtasks.setText(subtasks);
            System.out.println(subtasks);
        } else {
            tvSubtasks.setText("");
        }
    }
}
