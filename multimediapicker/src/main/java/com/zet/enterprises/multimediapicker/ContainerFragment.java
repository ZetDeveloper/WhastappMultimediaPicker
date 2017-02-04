package com.zet.enterprises.multimediapicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zet.enterprises.multimediapicker.adapter.FolderPickerAdapter;
import com.zet.enterprises.multimediapicker.adapter.ImagePickerAdapter;
import com.zet.enterprises.multimediapicker.custom.GridSpacingItemDecoration;
import com.zet.enterprises.multimediapicker.custom.ProgressWheel;
import com.zet.enterprises.multimediapicker.listeners.OnFolderClickListener;
import com.zet.enterprises.multimediapicker.listeners.OnImageClickListener;
import com.zet.enterprises.multimediapicker.model.Folder;
import com.zet.enterprises.multimediapicker.model.Image;
import com.zet.enterprises.multimediapicker.utils.Constants;
import com.zet.enterprises.multimediapicker.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.zet.enterprises.multimediapicker.GalleryPickerActivity.folderMode;
import static com.zet.enterprises.multimediapicker.GalleryPickerActivity.mode;
import static com.zet.enterprises.multimediapicker.GalleryPickerActivity.updateTitle2;

/**
 * Created by admin on 03/02/2017.
 */

public class ContainerFragment extends Fragment implements OnImageClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SHOW_VIDEO = "video" ;

    public boolean showVideo = false;

    private List<Folder> folders;
    private String currentImagePath;
    private String imageDirectory;
    private boolean showCamera;


    private int limit;
    private String folderTitle, imageTitle;

    public ArrayList<Image> images;
    public ArrayList<Image> selectedImages;

    private RelativeLayout mainLayout;
    private ProgressWheel progressBar;
    private TextView emptyTextView;
    private RecyclerView recyclerView;

    private GridLayoutManager layoutManager;
    private GridSpacingItemDecoration itemOffsetDecoration;

    private int imageColumns;
    private int folderColumns;

    private ImagePickerAdapter imageAdapter;
    private FolderPickerAdapter folderAdapter;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

    private Parcelable foldersState;

    private  String parent = "";


    protected View mView;
    private final int REQUEST_VIDEO_CAPTURE = 1;

    private OnFragmentInteractionListener mListener;

    public ContainerFragment() {
        // Required empty public constructor
    }


    public static ContainerFragment newInstance(boolean showVideo,int limit) {
        ContainerFragment fragment = new ContainerFragment();
        fragment.showVideo = showVideo;
        fragment.limit = limit;
        fragment.imageDirectory = "Camera";
        Bundle args = new Bundle();
        args.putBoolean(SHOW_VIDEO, showVideo);

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        getDataWithPermission();
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setImageAdapter(ArrayList<Image> images) {
        imageAdapter.setData(images);
        setItemDecoration(imageColumns);
        recyclerView.setAdapter(imageAdapter);

        File file = new File(images.get(0).getPath());

        parent = file.getParentFile().getName();

        updateTitle2(parent);
    }


    /**
     * Check permission
     */
    private void getDataWithPermission() {
        int rc = ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED)
            getData();
        else
            requestWriteExternalPermission();
    }

    /**
     * Get data
     */
    private void getData() {
        abortLoading();

        ImageLoaderRunnable runnable = new ImageLoaderRunnable();
        thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Request for permission
     * If permission denied or app is first launched, request for permission
     * If permission denied and user choose 'Nerver Ask Again', show snackbar with an action that navigate to app settings
     */
    private void requestWriteExternalPermission() {

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale( getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions( getActivity(), permissions, Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            if (!isPermissionRequested(Constants.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED)) {
                ActivityCompat.requestPermissions( getActivity(), permissions, Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                setPermissionRequested(Constants.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED);
            } else {
                Snackbar snackbar = Snackbar.make(mainLayout, R.string.msg_no_write_external_permission,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAppSettings();
                    }
                });
                snackbar.show();
            }
        }

    }


    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (ActivityCompat.shouldShowRequestPermissionRationale( getActivity(), Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions( getActivity(), permissions, Constants.PERMISSION_REQUEST_CAMERA);
        } else {
            if (!isPermissionRequested(Constants.PREF_CAMERA_REQUESTED)) {
                ActivityCompat.requestPermissions( getActivity(), permissions, Constants.PERMISSION_REQUEST_CAMERA);
                setPermissionRequested(Constants.PREF_CAMERA_REQUESTED);
            } else {
                Snackbar snackbar = Snackbar.make(mainLayout, R.string.msg_no_camera_permission,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAppSettings();
                    }
                });
                snackbar.show();
            }
        }
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getData();
                    return;
                }

                getActivity().finish();
            }
            case Constants.PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    captureImage();
                    return;
                }

                break;
            }
            default: {

                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    /**
     * Open app settings screen
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package",  getActivity().getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Set a permission is requested
     */
    private void setPermissionRequested(String permission) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(permission, true);
        editor.apply();
    }

    /**
     * Check if a permission is requestted or not (false by default)
     */
    private boolean isPermissionRequested(String permission) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( getActivity());
        return preferences.getBoolean(permission, false);
    }

    @Override
    public void onClick(View view, int position) {
        clickImage(position);
    }

    /**
     * Handle image selection event: add or remove selected image, change title
     */
    private void clickImage(int position) {
        int selectedItemPosition = selectedImagePosition(images.get(position));
        if (mode == GalleryPickerActivity.MODE_MULTIPLE) {
            if (selectedItemPosition == -1) {
                if (selectedImages.size() < limit) {
                    imageAdapter.addSelected(images.get(position));
                } else {
                    Toast.makeText(getActivity(), R.string.msg_limit_images, Toast.LENGTH_SHORT).show();
                }
            } else {
                imageAdapter.removeSelectedPosition(selectedItemPosition, position);
            }
        } else {
            if (selectedItemPosition != -1)
                imageAdapter.removeSelectedPosition(selectedItemPosition, position);
            else {
                if (selectedImages.size() > 0) {
                    imageAdapter.removeAllSelectedSingleClick();
                }
                imageAdapter.addSelected(images.get(position));
            }
        }
        updateTitle2(parent);
    }

    private int selectedImagePosition(Image image) {
        for (int i = 0; i < selectedImages.size(); i++) {
            if (selectedImages.get(i).getPath().equals(image.getPath())) {
                return i;
            }
        }

        return -1;
    }

    public void reload()
    {
        abortLoading();

        showLoading();

        ImageLoaderRunnable runnable = new ImageLoaderRunnable();
        thread = new Thread(runnable);
        thread.start();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor =  getActivity().managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public void setFolderAdapter() {

        imageAdapter.removeAllSelectedSingleClick();

        folderAdapter.setData(folders);
        setItemDecoration(folderColumns);
        recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
        updateTitle2("dan");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }
    /**
     * Set item size, column size base on the screen orientation
     */
    private void orientationBasedUI(int orientation) {
        imageColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5;
        folderColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;

        int columns = isDisplayingFolderView() ? folderColumns : imageColumns;
        layoutManager = new GridLayoutManager( getActivity(), columns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        setItemDecoration(columns);
    }

    /**
     * Set item decoration
     */
    private void setItemDecoration(int columns) {
        layoutManager.setSpanCount(columns);
        if (itemOffsetDecoration != null)
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        itemOffsetDecoration = new GridSpacingItemDecoration(columns, getResources().getDimensionPixelSize(R.dimen.item_padding), false);
        recyclerView.addItemDecoration(itemOffsetDecoration);
    }

    public void captureImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int rc = ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                requestCameraPermission();
            }
        } else {
            captureImage();
        }
    }

    /**
     * Start camera intent
     * Create a temporary file and pass file Uri to camera intent
     */
    private void captureImage() {


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity( getActivity().getPackageManager()) != null) {
            File imageFile = ImageUtils.createImageFile(imageDirectory);
            if (imageFile != null) {

                if(!showVideo) {
                    String authority =  getActivity().getPackageName() + ".fileprovider";
                    Uri uri = FileProvider.getUriForFile( getActivity(), authority, imageFile);
                    currentImagePath = "file:" + imageFile.getAbsolutePath();


                    File file = new File(imageFile.getAbsolutePath());
                    String dirAsFile = file.getParent();

                    File folder = new File(dirAsFile);

                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdir();
                    }

                    String imageFilePath = imageFile.getAbsolutePath();
                    File imageFile2 = new File(imageFilePath);
                    Uri imageFileUri = Uri.fromFile(imageFile2); // convert path to Uri

                    Intent it = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    it.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
                    startActivityForResult(it, Constants.REQUEST_CODE_CAPTURE);
                    //intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    //startActivityForResult(intent, Constants.REQUEST_CODE_CAPTURE);
                }else
                {
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (takeVideoIntent.resolveActivity( getActivity().getPackageManager()) != null) {
                        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                    }
                }
            } else {
                Toast.makeText( getActivity(), getString(R.string.error_create_image_file), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText( getActivity(), getString(R.string.error_no_camera), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Init handler to handle loading data results
     */
    @Override
    public void onStart() {
        super.onStart();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.FETCH_STARTED: {
                        showLoading();
                        break;
                    }
                    case Constants.FETCH_COMPLETED: {
                        ArrayList<Image> temps = new ArrayList<>();
                        temps.addAll(selectedImages);

                        ArrayList<Image> newImages = new ArrayList<>();
                        newImages.addAll(images);


                        if (folderMode) {
                            setFolderAdapter();
                            if (folders.size() != 0)
                                hideLoading();
                            else
                                showEmpty();

                        } else {
                            setImageAdapter(newImages);
                            if (images.size() != 0)
                                hideLoading();
                            else
                                showEmpty();
                        }

                        break;
                    }
                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                getData();
            }
        };
        if(!showVideo) {

            getActivity().getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
        }else
        {
            getActivity().getContentResolver().registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false, observer);
        }
    }

    /**
     * Stop loading data task
     */
    private void abortLoading() {
        if (thread == null)
            return;
        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if displaying folders view
     */
    public boolean isDisplayingFolderView() {
        return (folderMode &&
                (recyclerView.getAdapter() == null || recyclerView.getAdapter() instanceof FolderPickerAdapter));
    }

    /**
     * Update activity title
     * If we're displaying folder, set folder title
     * If we're displaying images, show number of selected images
     */



    public void onBackPressed() {
        if (folderMode && !isDisplayingFolderView()) {
            setFolderAdapter();
            return;
        }

        getActivity().setResult(RESULT_CANCELED);

    }


    /**
     * Show progessbar when loading data
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
    }

    /**
     * Hide progressbar when data loaded
     */
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    /**
     * Show empty data
     */
    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        abortLoading();

        getActivity().getContentResolver().unregisterContentObserver(observer);

        observer = null;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    /**
     * Loading data task
     */
    private class ImageLoaderRunnable implements Runnable {

        private void Imagen()
        {
            Message message;
            if (recyclerView.getAdapter() == null) {
                /*
                If the adapter is null, this is first time this activity's view is
                being shown, hence send FETCH_STARTED message to show progress bar
                while images are loaded from phone
                 */
                message = handler.obtainMessage();
                message.what = Constants.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            folders = new ArrayList<>();


            /*
            final String[] projectionPhotos2 = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Thumbnails.DATA

            };
            Cursor cursor2 = null;
            cursor2 = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos2, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");*/

            final String[] projectionPhotos2 = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Thumbnails.DATA

            };

            GalleryPickerActivity pick = (GalleryPickerActivity)GalleryPickerActivity.c;

            Cursor cursor2 = null;
            cursor2 = MediaStore.Images.Media.query(pick.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos2, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");


            File file2;
            ArrayList<Image> temp2 = new ArrayList<>(cursor2.getCount());

            if (cursor2 == null) {
                message = handler.obtainMessage();
                message.what = Constants.ERROR;
                message.sendToTarget();
                return;
            }


            if (cursor2 != null) {

                int bucketNameColumn = cursor2.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor2.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                while (cursor2.moveToNext()) {

                    if (Thread.interrupted()) {
                        return;
                    }

                    int bucketId = cursor2.getInt(bucketIdColumn);
                    String bucketName = cursor2.getString(bucketNameColumn);
                    final int dataColumn = cursor2.getColumnIndex(MediaStore.Images.Media.DATA);
                    final int imageIdColumn = cursor2.getColumnIndex(MediaStore.Images.Media._ID);
                    //int thumbImageColumn = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                    final int imageId = cursor2.getInt(imageIdColumn);
                    final String path = cursor2.getString(dataColumn);



                    file2 = new File(path);
                    if (file2.exists()) {

                        String filename = path.substring(path.lastIndexOf("/")+1);

                        Image image = new Image(imageId, filename, path, false);
                        temp2.add(image);

                        if (folderMode) {
                            Folder folder = getFolder(bucketName);
                            if (folder == null) {
                                folder = new Folder(bucketName);
                                folders.add(folder);
                            }

                            folder.getImages().add(image);
                        }
                    }
                }
            }

            cursor2.close();


            /*Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                message = handler.obtainMessage();
                message.what = Constants.ERROR;
                message.sendToTarget();
                return;
            }

            ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
            File file;


            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));

                    file = new File(path);
                    if (file.exists()) {
                        Image image = new Image(id, name, path, false);
                        temp.add(image);

                        if (folderMode) {
                            Folder folder = getFolder(bucket);
                            if (folder == null) {
                                folder = new Folder(bucket);
                                folders.add(folder);
                            }

                            folder.getImages().add(image);
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();
            */


            if (images == null) {
                images = new ArrayList<>();
            }
            images.clear();
            images.addAll(temp2);

            if (handler != null) {
                message = handler.obtainMessage();
                message.what = Constants.FETCH_COMPLETED;
                message.sendToTarget();
            }

            Thread.interrupted();
        }

        private void Video()
        {
            Message message;
            if (recyclerView.getAdapter() == null) {
                /*
                If the adapter is null, this is first time this activity's view is
                being shown, hence send FETCH_STARTED message to show progress bar
                while images are loaded from phone
                 */
                message = handler.obtainMessage();
                message.what = Constants.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            folders = new ArrayList<>();


            /*
            final String[] projectionPhotos2 = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Thumbnails.DATA

            };
            Cursor cursor2 = null;
            cursor2 = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos2, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");*/

            final String[] projectionPhotos2 = {
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.BUCKET_ID,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DATE_TAKEN,
                    //MediaStore.Video.Media.ORIENTATION,
                    MediaStore.Video.Thumbnails.DATA

            };

            GalleryPickerActivity pick = (GalleryPickerActivity)GalleryPickerActivity.c;

            Cursor cursor2 = null;
            cursor2 = MediaStore.Images.Media.query(pick.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos2, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");


            File file2;
            ArrayList<Image> temp2 = new ArrayList<>(cursor2.getCount());

            if (cursor2 == null) {
                message = handler.obtainMessage();
                message.what = Constants.ERROR;
                message.sendToTarget();
                return;
            }


            if (cursor2 != null) {

                int bucketNameColumn = cursor2.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor2.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                while (cursor2.moveToNext()) {

                    if (Thread.interrupted()) {
                        return;
                    }

                    int bucketId = cursor2.getInt(bucketIdColumn);
                    String bucketName = cursor2.getString(bucketNameColumn);
                    final int dataColumn = cursor2.getColumnIndex(MediaStore.Video.Media.DATA);
                    final int imageIdColumn = cursor2.getColumnIndex(MediaStore.Video.Media._ID);
                    //int thumbImageColumn = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                    final int imageId = cursor2.getInt(imageIdColumn);
                    final String path = cursor2.getString(dataColumn);



                    file2 = new File(path);
                    if (file2.exists()) {

                        String filename = path.substring(path.lastIndexOf("/")+1);

                        Image image = new Image(imageId, filename, path, false);
                        temp2.add(image);

                        if (folderMode) {
                            Folder folder = getFolder(bucketName);
                            if (folder == null) {
                                folder = new Folder(bucketName);
                                folders.add(folder);
                            }

                            folder.getImages().add(image);
                        }
                    }
                }
            }

            cursor2.close();


            /*Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                message = handler.obtainMessage();
                message.what = Constants.ERROR;
                message.sendToTarget();
                return;
            }

            ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
            File file;


            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));

                    file = new File(path);
                    if (file.exists()) {
                        Image image = new Image(id, name, path, false);
                        temp.add(image);

                        if (folderMode) {
                            Folder folder = getFolder(bucket);
                            if (folder == null) {
                                folder = new Folder(bucket);
                                folders.add(folder);
                            }

                            folder.getImages().add(image);
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();
            */


            if (images == null) {
                images = new ArrayList<>();
            }
            images.clear();
            images.addAll(temp2);

            if (handler != null) {
                message = handler.obtainMessage();
                message.what = Constants.FETCH_COMPLETED;
                message.sendToTarget();
            }

            Thread.interrupted();
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if(!showVideo) {
                Imagen();
            }else
            {
                Video();
            }

        }
    }

    /**
     * Return folder base on folder name
     */
    public Folder getFolder(String name) {
        for (Folder folder : folders) {
            if (folder.getFolderName().equals(name)) {
                return folder;
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            final Uri videoUri = data.getData();

            MediaScannerConnection.scanFile(GalleryPickerActivity.c,
                    new String[]{videoUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {

                            //getDataWithPermission();

                            path = getRealPathFromURI(videoUri);


                            String filename = path.substring(path.lastIndexOf("/")+1);

                            Image imagenCamara = new Image(-1999,filename,path,true);


                            selectedImages.add(imagenCamara);

                            Intent data = new Intent();
                            data.putParcelableArrayListExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES, selectedImages);

                            GalleryPickerActivity act =  (GalleryPickerActivity)GalleryPickerActivity.c;

                            act.setResult(RESULT_OK, data);
                            act.finish();

                        }
                    });

        }

        if (requestCode == Constants.REQUEST_CODE_CAPTURE) {
            if (resultCode == RESULT_OK && currentImagePath != null) {
                Uri imageUri = Uri.parse(currentImagePath);
                if (imageUri != null) {
                    MediaScannerConnection.scanFile(GalleryPickerActivity.c,
                            new String[]{imageUri.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {

                                    //getDataWithPermission();


                                    String filename = path.substring(path.lastIndexOf("/")+1);

                                    Image imagenCamara = new Image(-1999,filename,path,true);


                                    selectedImages.add(imagenCamara);

                                    Intent data = new Intent();
                                    data.putParcelableArrayListExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES, selectedImages);

                                    GalleryPickerActivity act =  (GalleryPickerActivity)GalleryPickerActivity.c;

                                    act.setResult(RESULT_OK, data);
                                    act.finish();
                                }
                            });
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_container, container, false);
        this.mView = view;



        mainLayout = (RelativeLayout) mView.findViewById(R.id.mainFragment);
        progressBar = (ProgressWheel) mView.findViewById(R.id.progress_bar);
        emptyTextView = (TextView) mView.findViewById(R.id.tv_empty_images);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);



        /** Init folder and image adapter */
        imageAdapter = new ImagePickerAdapter(GalleryPickerActivity.c, images, selectedImages, this);
        folderAdapter = new FolderPickerAdapter(GalleryPickerActivity.c, new OnFolderClickListener() {
            @Override
            public void onFolderClick(Folder bucket) {
                foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
                setImageAdapter(bucket.getImages());
            }
        });

        orientationBasedUI(getResources().getConfiguration().orientation);

        imageAdapter.removeAllSelectedSingleClick();



        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

