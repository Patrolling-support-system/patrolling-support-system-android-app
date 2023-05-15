package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;
import static android.widget.ImageView.ScaleType.FIT_XY;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.datatransport.BuildConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
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
import pl.agh.patrollingsupportsystem.recyclerViews.audioRecRecyclerViewProperties.AudioRecordingGeneral;
import pl.agh.patrollingsupportsystem.recyclerViews.audioRecRecyclerViewProperties.AudioRecordingListAdapter;
import pl.agh.patrollingsupportsystem.recyclerViews.audioRecRecyclerViewProperties.RecyclerViewInterface;

public class ReportForLocationActivity extends AppCompatActivity implements RecyclerViewInterface {

    Button addImagesButton, takePictureButton, sendImagesButton, sendTextNote;
    EditText noteField;
    LinearLayout galleryLinearLayout;

    ActivityResultLauncher<String> mChoosePhoto;

    Uri mImageUri;
    List<Uri> res = new ArrayList<>();
    ActivityResultLauncher<Uri> mTakePictureLauncher;

    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private FirebaseFirestore db;

    //Audio
    MediaRecorder recorder;
    boolean isRecording = false;
    boolean isPaused = false;

    //rv
    RecyclerView rvAudioRecordingList;
    ArrayList<AudioRecordingGeneral> audioRecordingItemList;
    AudioRecordingListAdapter audioRecordingListAdapter;
    ArrayList<AudioRecordingGeneral> audioRecordingFiles = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_for_location);
        db = FirebaseFirestore.getInstance();

        addImagesButton = findViewById(R.id.addImagesButton);
        sendImagesButton = findViewById(R.id.sendImagesButton);
        takePictureButton = findViewById(R.id.takePictureButton);
        noteField = findViewById(R.id.textNote);
        sendTextNote = findViewById(R.id.sendTextNote);
        galleryLinearLayout = findViewById(R.id.galleryLinearLayout);

        //RecycleView
        audioRecordingItemList = new ArrayList<>();
        audioRecordingListAdapter = new AudioRecordingListAdapter(this, audioRecordingFiles, this);
        rvAudioRecordingList = findViewById(R.id.rvAudioRecordingList);
        rvAudioRecordingList.setHasFixedSize(true);
        rvAudioRecordingList.setAdapter(audioRecordingListAdapter);
        rvAudioRecordingList.setLayoutManager(new LinearLayoutManager(this));

        //Photo from memory
        mChoosePhoto = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                result -> {
                    res.addAll(result);
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

                        galleryLinearLayout.addView(imageView);
                    }
                });

        addImagesButton.setOnClickListener(v -> {
            mChoosePhoto.launch("image/*");
        });

        //Take picture

        mTakePictureLauncher =
                registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                    if (result) {
                        res.add(mImageUri);
                        ImageView imageView = new ImageView(ReportForLocationActivity.this);
                        imageView.setPadding(0,0,0,0);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        imageView.setLayoutParams(params);
                        imageView.setAdjustViewBounds(true);

                        Glide.with(ReportForLocationActivity.this)
                                .load(mImageUri)
                                .into(imageView);

                        galleryLinearLayout.addView(imageView);
                    } else {
                        Toast.makeText(this, "Picture not taken", Toast.LENGTH_SHORT).show();
                    }
                });

        takePictureButton.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });

        //Send images:
        sendImagesButton.setOnClickListener(v -> {
            for (int i = 0; i < res.size(); i++) {
                StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());
                UploadTask uploadTask = imageRef.putFile(res.get(i));

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // TODO Toast?
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO Toast?
                    }
                });
            }
        });

        //Audio recording


        Button recordButton = findViewById(R.id.startPauzeRecButton);
        recordButton.setOnClickListener(v -> {
            if (!isRecording) {
                // Start recording
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                String fileNameStr = (new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) +  "test.mp4");
                AudioRecordingGeneral fileName = new AudioRecordingGeneral();
                fileName.setFileName(fileNameStr);
                audioRecordingFiles.add(fileName);
                recorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + fileName);
                System.out.println(Environment.getExternalStorageDirectory() + File.separator
                        + Environment.DIRECTORY_DCIM + File.separator + "FILE_NAME");

                try {
                    recorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                recorder.start();
                isRecording = true;
                isPaused = false;
                recordButton.setText("Pauza");
            } else if (isRecording && !isPaused) {
                // Pause recording
                recorder.pause();
                isPaused = true;
                recordButton.setText("Wznów");
            } else if (isRecording && isPaused) {
                // Resume recording
                recorder.resume();
                isPaused = false;
                recordButton.setText("Pauza");
            }
        });

        Button stopButton = findViewById(R.id.stopRec);
        stopButton.setOnClickListener(v -> {
            if (isRecording) {
                // Stop recording
                recorder.stop();
                recorder.release();
                isRecording = false;
                isPaused = false;
                recordButton.setText("Nagraj");
                EventChangeListener();
            }
        });

        // text note


        sendTextNote.setOnClickListener(v -> {
            String note = noteField.getText().toString();

            // creating report with text note
            // after changing actionList to Tasks change to current checkpoint from db

            Map<String, Object> checkpointReport = new HashMap<>();
            checkpointReport.put("checkpointNumber", 1);
            checkpointReport.put("notes", note);

            db.collection("CheckpointReport")
                    .add(checkpointReport)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        });

    }

    private void EventChangeListener() {
        audioRecordingListAdapter.notifyDataSetChanged();
    }

    //Take picture
    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mImageUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID+ ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                mTakePictureLauncher.launch(mImageUri);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onItemClick(int position) {

    }
}