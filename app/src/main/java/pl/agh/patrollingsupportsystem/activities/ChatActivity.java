package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.chatRecyclerViewProperties.ChatAdapter;
import pl.agh.patrollingsupportsystem.databinding.ActivityChatBinding;
import pl.agh.patrollingsupportsystem.recyclerViews.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    FrameLayout btn;
    TextView tvCoordinatorName;
    TextView tvMessage;
    RecyclerView rvChat;
    FirebaseAuth mAuth;
    String coordinator;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle documentExtras1 = getIntent().getExtras();
        String taskDocumentId = null;
        if (documentExtras1 != null) {
            taskDocumentId = documentExtras1.getString("task_document");
        }

        Bundle documentExtras = getIntent().getExtras();
        if (documentExtras != null) {
            coordinator = documentExtras.getString("coordinator");
        }

        mAuth = FirebaseAuth.getInstance();
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_chat);
        //init();
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, mAuth.getCurrentUser().getUid(), coordinator /* preferenceManager.getString(Constants.KEY_USER_ID)*/); //Tutaj generalnie userId???
        //binding.chatRecyclerView.setAdapter(chatAdapter);
        rvChat = findViewById(R.id.chatRecyclerView);
        rvChat.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        tvCoordinatorName = findViewById(R.id.coordinatorName);
        //init();
        //setListeners();
        //binding.layoutSend.setOnClickListener(v -> sendMessage());
        btn = findViewById(R.id.layoutSend);
        tvMessage = findViewById(R.id.inputMessage);
        String finalTaskDocumentId = taskDocumentId;
        btn.setOnClickListener(v -> sendMessage(finalTaskDocumentId));
        // setListeners();
        listenMessages(taskDocumentId);

        database.collection("User").whereEqualTo("userId", coordinator).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    String name = document.getString("name");
                    String surname = document.getString("surname");
                    tvCoordinatorName.setText(name + " " + surname);
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });

    }


    private void listenMessages(String taskDocumentID){
        database.collection("Chat")
                .whereEqualTo("taskId", taskDocumentID)
                .whereEqualTo("senderId", mAuth.getCurrentUser().getUid())
                .whereEqualTo("receiverId", coordinator)
                .addSnapshotListener(ev);
        database.collection("Chat")
                .whereEqualTo("taskId", taskDocumentID)
                .whereEqualTo("receiverId", mAuth.getCurrentUser().getUid())
                .whereEqualTo("senderId", coordinator)
                .addSnapshotListener(ev);
    }

    private final EventListener<QuerySnapshot> ev = (value, err) ->{
        if (err != null){
            return;
        }
        if(value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
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
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                rvChat.smoothScrollToPosition(chatMessages.size() - 1);
            }
            rvChat.setVisibility(View.VISIBLE);
        }
    };
    private void sendMessage(String taskDocumentId){
        HashMap<String, Object> message = new HashMap<>();
        message.put("senderId", mAuth.getCurrentUser().getUid());
        message.put("receiverId", coordinator);
        message.put("message", tvMessage.getText().toString());
        message.put("taskId", taskDocumentId);
        database.collection("Chat")
                .add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                tvMessage.setText("");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

//    private void setListeners() {
//        binding.layoutSend.setOnClickListener(v -> sendMessage());
//    }
}