package com.zetenterprises.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zet.enterprises.multimediapicker.GalleryPickerActivity;
import com.zet.enterprises.multimediapicker.MultimediaPicker;
import com.zet.enterprises.multimediapicker.model.Image;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button buttonPickImage;

    private ArrayList<Image> images = new ArrayList<>();

    private int REQUEST_CODE_PICKER = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);
        buttonPickImage = (Button) findViewById(R.id.button_pick_image);
        buttonPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });


    }


    // Recomended builder
    public void start() {
        MultimediaPicker.create(this)
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select") // image selection title
                .single() // single mode
                .multi() // multi mode (default mode)
                .limit(10) // max images can be selected (999 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(REQUEST_CODE_PICKER); // start image picker activity with request code
    }

    // Traditional intent
    public void startWithIntent() {
        Intent intent = new Intent(this, GalleryPickerActivity.class);

        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_FOLDER_MODE, true);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_MODE, GalleryPickerActivity.MODE_MULTIPLE);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_LIMIT, 10);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_SHOW_CAMERA, true);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES, images);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_FOLDER_TITLE, "Album");
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_IMAGE_TITLE, "Tap to select images");
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_IMAGE_DIRECTORY, "Camera");
        startActivityForResult(intent, REQUEST_CODE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = data.getParcelableArrayListExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
            StringBuilder sb = new StringBuilder();
            for (int i = 0, l = images.size(); i < l; i++) {
                sb.append(images.get(i).getPath() + "\n");
            }
            textView.setText(sb.toString());
        }
    }
}
