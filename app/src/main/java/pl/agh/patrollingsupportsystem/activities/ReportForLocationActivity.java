package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.datatransport.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

//import pl.agh.patrollingsupportsystem.BuildConfig;
import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.models.AudioRecording;
import pl.agh.patrollingsupportsystem.recyclerViews.audioRecordings.AudioRecordingListAdapter;

public class ReportForLocationActivity extends AppCompatActivity {

    Button btnAddImage, btnTakePicture, btnSendReport, recordButton, stopButton;
    EditText etNote;
    RecyclerView rvAudioRecordingList;
    LinearLayout llGallery;
    List<Uri> imagesList = new ArrayList<>();
    ActivityResultLauncher<String> choosePhoto;
    ActivityResultLauncher<Uri> takePictureLauncher;
    ArrayList<AudioRecording> audioRecordingList;
    ArrayList<AudioRecording> audioRecordingFiles;
    List<String> audioRecordingReferenceList;
    List<String> imageReferenceList;
    String reportDocumentId;
    boolean isRecording, isPaused;
    Uri imageUri;
    MediaRecorder recorder;
    AudioRecordingListAdapter audioRecordingListAdapter;
    FirebaseFirestore fbDb;
    String fbAuthUser;
    StorageReference fbStorageReference;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_for_location);

        reportDocumentId = generateDocumentId();

        //References
        imageReferenceList = new ArrayList<>();
        audioRecordingReferenceList = new ArrayList<>();

        //Firebase
        fbDb = FirebaseFirestore.getInstance();
        fbAuthUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fbStorageReference = FirebaseStorage.getInstance().getReference();

        //Layout elements
        btnAddImage = findViewById(R.id.buttonAddImage);
        btnTakePicture = findViewById(R.id.buttonTakePicture);
        recordButton = findViewById(R.id.buttonStartPauzeRecording);
        stopButton = findViewById(R.id.buttonStopRecording);
        etNote = findViewById(R.id.editTextNote);
        llGallery = findViewById(R.id.galleryLinearLayout);
        btnSendReport = findViewById(R.id.buttonSendReport);

        //Audio flags
        isRecording = false;
        isPaused = false;

        //Audio lists
        audioRecordingList = new ArrayList<>();
        audioRecordingFiles = new ArrayList<>();

        //RecycleView for Audio
        audioRecordingListAdapter = new AudioRecordingListAdapter(this, audioRecordingFiles);
        rvAudioRecordingList = findViewById(R.id.recyclerViewAudioRecordingList);
        rvAudioRecordingList.setHasFixedSize(true);
        rvAudioRecordingList.setAdapter(audioRecordingListAdapter);
        rvAudioRecordingList.setLayoutManager(new LinearLayoutManager(this));

        //Fetching extras
        Bundle documentExtras = getIntent().getExtras();
        String taskDocumentId = null;
        if (documentExtras != null) {
            taskDocumentId = documentExtras.getString("task_document");
        }
        String finalTaskDocumentId = taskDocumentId; //To use as parameter

        //Photo from device
        choosePhoto = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                result -> {
                    imagesList.addAll(result);
                    for (int i = 0; i < result.size(); i++) {
                        ImageView imageView = new ImageView(ReportForLocationActivity.this);
                        imageView.setPadding(0,0,0,0);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        imageView.setLayoutParams(params);
                        imageView.setAdjustViewBounds(true);

                        Glide.with(ReportForLocationActivity.this)
                                .load(result.get(i))
                                .into(imageView);

                        llGallery.addView(imageView);
                    }
                });

        btnAddImage.setOnClickListener(v -> choosePhoto.launch("image/*"));

        //Take picture

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), result -> {
                    if (result) {
                        imagesList.add(imageUri);
                        ImageView imageView = new ImageView(ReportForLocationActivity.this);
                        imageView.setPadding(0,0,0,0);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        imageView.setLayoutParams(params);
                        imageView.setAdjustViewBounds(true);

                        Glide.with(ReportForLocationActivity.this)
                                .load(imageUri)
                                .into(imageView);
                        llGallery.addView(imageView);
                    } else {
                        Toast.makeText(this, "Picture not taken", Toast.LENGTH_SHORT).show();
                    }
                });

        btnTakePicture.setOnClickListener(v -> dispatchTakePictureIntent());

        //Audio recording

        recordButton.setOnClickListener(v -> {
            if (!isRecording) {
                // Start recording
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                String fileNameStr = (new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) +  ".mpeg4");
                AudioRecording fileName = new AudioRecording();
                fileName.setFileName(fileNameStr);
                audioRecordingList.add(fileName);
                recorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + fileName.getFileName());
                try {
                    recorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                recorder.start();
                isRecording = true;
                isPaused = false;
                recordButton.setText("Pause");
            } else if (isRecording && !isPaused) {
                // Pause recording
                recorder.pause();
                isPaused = true;
                recordButton.setText("Resume");
            } else if (isRecording && isPaused) {
                // Resume recording
                recorder.resume();
                isPaused = false;
                recordButton.setText("Pause");
            }
        });


        stopButton.setOnClickListener(v -> {
            if (isRecording) {
                // Stop recording
                recorder.stop();
                recorder.release();
                isRecording = false;
                isPaused = false;
                recordButton.setText("Record");
                audioRecordingFiles.clear();
                audioRecordingFiles.addAll(audioRecordingList);
                EventChangeListener();
            }
        });

        btnSendReport.setOnClickListener( v -> {
            SendImages(finalTaskDocumentId);
            SendAudioRecordings(finalTaskDocumentId);
            SendNote(finalTaskDocumentId);
        });

    }

    private void EventChangeListener() {
        audioRecordingListAdapter.notifyDataSetChanged();
    }

    private void SendImages(String finalTaskDocumentId){
        for (int i = 0; i < imagesList.size(); i++) {
            StorageReference imageRef = fbStorageReference.child(finalTaskDocumentId + '/' + reportDocumentId + '/' + "images/" + UUID.randomUUID().toString());
            imageReferenceList.add(imageRef.getPath());
            UploadTask uploadTask = imageRef.putFile(imagesList.get(i));

            uploadTask
                    .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Images uploaded for upload task: " + taskSnapshot.getTask()))
                    .addOnFailureListener(e -> Log.w(TAG, "Error images upload: ", e));
        }
        Toast.makeText(this, "Report sent", Toast.LENGTH_LONG).show();
        finish();
    }

    private void SendAudioRecordings(String finalTaskDocumentId){
        for (int i = 0; i < audioRecordingList.size(); i++) {
            File f = new File(getExternalCacheDir().getAbsolutePath() + audioRecordingList.get(i).getFileName());
            System.out.println(getExternalCacheDir().getAbsolutePath() + audioRecordingList.get(i).getFileName());
            Uri mAudioUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    BuildConfig.APPLICATION_ID+ ".provider", f
            );
            StorageReference audioRef = fbStorageReference.child(finalTaskDocumentId + '/' + reportDocumentId + '/' + "audio/" + audioRecordingList.get(i).getFileName());
            audioRecordingReferenceList.add(audioRef.getPath());
            UploadTask uploadTask = audioRef.putFile(mAudioUri);

            uploadTask
                    .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Audio recordings uploaded for upload task: " + taskSnapshot.getTask()))
                    .addOnFailureListener(e -> Log.w(TAG, "Error audio recording upload: ", e));
        }
    }
    public static String generateDocumentId() {
        String uuid = UUID.randomUUID().toString().substring(0,20);
        return uuid.replaceAll("-", "");
    }

    private void SendNote(String finalTaskDocumentId){
        String note = etNote.getText().toString();

        // creating report with text note
        // after changing actionList to Tasks change to current checkpoint from db

        Map<String, Object> checkpointReport = new HashMap<>();
        checkpointReport.put("checkpointNumber", 1);
        checkpointReport.put("note", note);
        checkpointReport.put("images", imageReferenceList);
        checkpointReport.put("recordings", audioRecordingReferenceList);
        checkpointReport.put("patrolParticipant", fbAuthUser);
        checkpointReport.put("task", finalTaskDocumentId);

        fbDb.collection("CheckpointReport").document(reportDocumentId)
                .set(checkpointReport)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot written");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }


    //Take picture
    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID+ ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                takePictureLauncher.launch(imageUri);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        return image;
    }
}