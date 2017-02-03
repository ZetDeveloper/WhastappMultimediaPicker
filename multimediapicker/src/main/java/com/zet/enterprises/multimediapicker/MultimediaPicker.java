package com.zet.enterprises.multimediapicker;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.zet.enterprises.multimediapicker.model.Image;
import com.zet.enterprises.multimediapicker.utils.Constants;

import java.util.ArrayList;

/**
 * Created by admin on 03/02/2017.
 */

public abstract class MultimediaPicker {

    private int mode;
    private int limit;
    private boolean showCamera;
    private String folderTitle;
    private String imageTitle;
    private ArrayList<Image> selectedImages;
    private boolean folderMode;
    private String imageDirectory;

    public static boolean onlyVideos = false;
    public static boolean onlyImages = false;

    public abstract void start(int requestCode);

    public static class MultimediaPickerWithActivity extends MultimediaPicker {

        private Activity activity;

        public MultimediaPickerWithActivity(Activity activity) {
            this.activity = activity;
            init(activity);
        }

        @Override
        public void start(int requestCode) {
            Intent intent = getIntent(activity);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static class MultimediaPickerWithFragment extends MultimediaPicker {

        private Fragment fragment;

        public MultimediaPickerWithFragment(Fragment fragment) {
            this.fragment = fragment;
            init(fragment.getActivity());
        }

        @Override
        public void start(int requestCode) {
            Intent intent = getIntent(fragment.getActivity());
            fragment.startActivityForResult(intent, requestCode);
        }
    }


    public void init(Activity activity) {
        this.mode = GalleryPickerActivity.MODE_MULTIPLE;
        this.limit = Constants.MAX_LIMIT;
        this.showCamera = true;
        this.folderTitle = activity.getString(R.string.title_folder);
        this.imageTitle = activity.getString(R.string.title_select_image);
        this.selectedImages = new ArrayList<>();
        this.folderMode = false;
        this.imageDirectory = activity.getString(R.string.image_directory);
    }


    public static MultimediaPickerWithActivity create(Activity activity) {
        return new MultimediaPickerWithActivity(activity);
    }

    public static MultimediaPickerWithFragment create(Fragment fragment) {
        return new MultimediaPickerWithFragment(fragment);
    }

    public MultimediaPicker single() {
        mode = GalleryPickerActivity.MODE_SINGLE;
        return this;
    }

    public MultimediaPicker setOnlyImages(boolean onlyImages) {
        this.onlyImages = onlyImages;
        return this;
    }

    public MultimediaPicker setOnlyVideos(boolean onlyVideos) {
        this.onlyVideos = onlyVideos;
        return this;
    }

    public MultimediaPicker multi() {
        mode = GalleryPickerActivity.MODE_MULTIPLE;
        return this;
    }


    public MultimediaPicker limit(int count) {
        limit = count;
        return this;
    }

    public MultimediaPicker showCamera(boolean show) {
        showCamera = show;
        return this;
    }

    public MultimediaPicker folderTitle(String title) {
        this.folderTitle = title;
        return this;
    }

    public MultimediaPicker imageTitle(String title) {
        this.imageTitle = title;
        return this;
    }

    public MultimediaPicker origin(ArrayList<Image> images) {
        selectedImages = images;
        return this;
    }

    public MultimediaPicker folderMode(boolean folderMode) {
        this.folderMode = folderMode;
        return this;
    }

    public MultimediaPicker imageDirectory(String directory) {
        this.imageDirectory = directory;
        return this;
    }

    public Intent getIntent(Activity activity) {
        Intent intent = new Intent(activity, GalleryPickerActivity.class);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_MODE, mode);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_LIMIT, limit);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_SHOW_CAMERA, showCamera);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_FOLDER_TITLE, folderTitle);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_IMAGE_TITLE, imageTitle);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES, selectedImages);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_FOLDER_MODE, folderMode);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_IMAGE_DIRECTORY, imageDirectory);

        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_ONLY_IMAGES, onlyImages);
        intent.putExtra(GalleryPickerActivity.INTENT_EXTRA_ONLY_VIDEO, onlyVideos);

        return intent;
    }


}
