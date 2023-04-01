package pl.agh.patrollingsupportsystem.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import pl.agh.patrollingsupportsystem.BuildConfig;
import pl.agh.patrollingsupportsystem.R;

public class ReportForLocationActivity extends AppCompatActivity {

    Button addImagesButton, takePictureButton, sendImagesButton;
    LinearLayout galleryLinearLayout;

    ActivityResultLauncher<String> mChoosePhoto;

    Uri mImageUri;
    List<Uri> res = new ArrayList<>();
    ActivityResultLauncher<Uri> mTakePictureLauncher;

    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_for_location);

        addImagesButton = findViewById(R.id.addImagesButton);
        sendImagesButton = findViewById(R.id.sendImagesButton);
        takePictureButton = findViewById(R.id.takePictureButton);
        galleryLinearLayout = findViewById(R.id.galleryLinearLayout);

        //Photo from memory
        mChoosePhoto = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> result) {
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

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // TODO Toast?
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO Toast?
                    }
                });
            }
        });


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
                        BuildConfig.APPLICATION_ID + ".provider",
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
}