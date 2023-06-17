package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.chat.ChatAdapter;
import pl.agh.patrollingsupportsystem.databinding.ActivityChatBinding;
import pl.agh.patrollingsupportsystem.recyclerViews.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore fbDb;
    FrameLayout flSendButton;
    TextView tvCoordinatorName;
    TextView tvMessage;
    RecyclerView rvChat;
    FirebaseAuth mAuth;
    String coordinatorId;
    String taskDocumentId;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSION_FINE_LOCATION = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatMessages = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        //Extras handling
        Bundle documentExtras = getIntent().getExtras();
        if (documentExtras != null) {
            taskDocumentId = documentExtras.getString("task_document");
            coordinatorId = documentExtras.getString("coordinator");
        }
        String finalTaskDocumentId = taskDocumentId;

        fbDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //Layout Elements
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        flSendButton = findViewById(R.id.layoutSend);
        tvMessage = findViewById(R.id.inputMessage);
        tvCoordinatorName = findViewById(R.id.coordinatorName);

        //RecyclerView for messages
        chatAdapter = new ChatAdapter(chatMessages, mAuth.getCurrentUser().getUid(), coordinatorId);
        rvChat = findViewById(R.id.chatRecyclerView);
        rvChat.setAdapter(chatAdapter);

        flSendButton.setOnClickListener(v -> sendMessage(finalTaskDocumentId));

        coordinatorHeaderSet();

        listenMessages(taskDocumentId);
    }


    private void listenMessages(String taskDocumentID) {
        fbDb.collection("Chat")
                .whereEqualTo("taskId", taskDocumentID)
                .whereEqualTo("senderId", mAuth.getCurrentUser().getUid())
                .whereEqualTo("receiverId", coordinatorId)
                .addSnapshotListener(ev);
        fbDb.collection("Chat")
                .whereEqualTo("taskId", taskDocumentID)
                .whereEqualTo("receiverId", mAuth.getCurrentUser().getUid())
                .whereEqualTo("senderId", coordinatorId)
                .addSnapshotListener(ev);
    }

    //TODO Change with coordinator collection
    private void coordinatorHeaderSet() {
        fbDb.collection("Coordinator").whereEqualTo("userId", coordinatorId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    String name = document.getString("name");
                    String surname = document.getString("surname");
                    tvCoordinatorName.setText(name + " " + surname);
                    System.out.println(name + surname);
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    private final EventListener<QuerySnapshot> ev = (value, err) -> {
        if (err != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.receiverId = documentChange.getDocument().getString("receiverId");
                    chatMessage.senderId = documentChange.getDocument().getString("senderId");
                    chatMessage.message = documentChange.getDocument().getString("message");
                    chatMessage.taskId = documentChange.getDocument().getString("taskId");
                    chatMessage.dateObject = LocalDateTime.now();
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                rvChat.smoothScrollToPosition(chatMessages.size() - 1);
            }
            rvChat.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("MissingPermission")
    private void sendMessage(String taskDocumentId) {
        if (hasLocationPermission()) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    HashMap<String, Object> message = new HashMap<>();
                    message.put("senderId", mAuth.getCurrentUser().getUid());
                    message.put("receiverId", coordinatorId);
                    message.put("message", tvMessage.getText().toString());
                    message.put("taskId", taskDocumentId);
                    message.put("date", Timestamp.now());
                    message.put("location", new GeoPoint(location.getLatitude(), location.getLongitude()));

                    fbDb.collection("Chat")
                            .add(message)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                tvMessage.setText("");
                            })
                            .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
                }
            });
        }
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
}