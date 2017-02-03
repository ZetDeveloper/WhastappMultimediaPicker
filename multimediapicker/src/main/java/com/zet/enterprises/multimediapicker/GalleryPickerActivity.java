package com.zet.enterprises.multimediapicker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.zet.enterprises.multimediapicker.model.Folder;
import com.zet.enterprises.multimediapicker.model.Image;
import com.zet.enterprises.multimediapicker.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryPickerActivity extends AppCompatActivity implements ContainerFragment.OnFragmentInteractionListener  {

    @Override
    public void onFragmentInteraction(Uri uri) {
    }



    public static MenuItem menuDone, menuCamera;
    public static final int menuDoneId = 100;
    public static final int menuCameraId = 101;

    private static final String TAG = "ImagePickerActivity";
    private ActionBar actionBar;

    private TabLayout tabGallery;

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public static List<Folder> folders;

    public static String currentImagePath;
    public static String imageDirectory;



    public static boolean showCamera;
    public static int mode;
    public static boolean folderMode;
    public static int limit;
    public static String folderTitle, imageTitle;
    public static boolean onlyVideos = false;
    public static boolean onlyImages = false;

    public static Context c;


    public static final int MODE_SINGLE = 1;
    public static final int MODE_MULTIPLE = 2;

    public static final String INTENT_EXTRA_ONLY_VIDEO = "onlyVideos";
    public static final String INTENT_EXTRA_ONLY_IMAGES = "onlyImages";
    public static final String INTENT_EXTRA_SELECTED_IMAGES = "selectedImages";
    public static final String INTENT_EXTRA_LIMIT = "limit";
    public static final String INTENT_EXTRA_SHOW_CAMERA = "showCamera";
    public static final String INTENT_EXTRA_MODE = "mode";
    public static final String INTENT_EXTRA_FOLDER_MODE = "folderMode";
    public static final String INTENT_EXTRA_FOLDER_TITLE = "folderTitle";
    public static final String INTENT_EXTRA_IMAGE_TITLE = "imageTitle";
    public static final String INTENT_EXTRA_IMAGE_DIRECTORY = "imageDirectory";

    ContainerFragment imagen;
    ContainerFragment video;
    private int indexTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_picker);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }

        c = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_g);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        /** Set activity title */
        if (actionBar != null) {
            actionBar.setTitle(folderMode ? folderTitle : imageTitle);
        }


        /** Get extras */
        onlyImages = intent.getBooleanExtra(GalleryPickerActivity.INTENT_EXTRA_ONLY_IMAGES, false);
        onlyVideos = intent.getBooleanExtra(GalleryPickerActivity.INTENT_EXTRA_ONLY_VIDEO, false);

        limit = intent.getIntExtra(GalleryPickerActivity.INTENT_EXTRA_LIMIT, Constants.MAX_LIMIT);
        mode = intent.getIntExtra(GalleryPickerActivity.INTENT_EXTRA_MODE, GalleryPickerActivity.MODE_MULTIPLE);
        folderMode = intent.getBooleanExtra(GalleryPickerActivity.INTENT_EXTRA_FOLDER_MODE, false);

        if (intent.hasExtra(INTENT_EXTRA_FOLDER_TITLE)) {
            folderTitle = intent.getStringExtra(GalleryPickerActivity.INTENT_EXTRA_FOLDER_TITLE);
        } else {
            folderTitle = getString(R.string.title_folder);
        }

        if (intent.hasExtra(INTENT_EXTRA_IMAGE_TITLE)) {
            imageTitle = intent.getStringExtra(GalleryPickerActivity.INTENT_EXTRA_IMAGE_TITLE);
        } else {
            imageTitle = getString(R.string.title_select_image);
        }

        imageDirectory = intent.getStringExtra(GalleryPickerActivity.INTENT_EXTRA_IMAGE_DIRECTORY);
        if (imageDirectory == null || TextUtils.isEmpty(imageDirectory)) {
            imageDirectory = getString(R.string.image_directory);
        }

        showCamera = intent.getBooleanExtra(GalleryPickerActivity.INTENT_EXTRA_SHOW_CAMERA, true);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        imagen = ContainerFragment.newInstance(false,10);
        video = ContainerFragment.newInstance(true,1);

        if(onlyVideos)
        {
            adapter.addFragment(video, "Videos");
        }

        if(onlyImages)
        {
            adapter.addFragment(imagen, "Imagenes");
        }

        if(onlyVideos == false && onlyImages == false) {
            adapter.addFragment(imagen, "Imagenes");
            adapter.addFragment(video, "Videos");
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        tabGallery = (TabLayout)findViewById(R.id.tabs);
        tabGallery.setupWithViewPager(viewPager);
        tabGallery.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorAccent));

        if (mode == GalleryPickerActivity.MODE_MULTIPLE && intent.hasExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES)) {
            imagen.selectedImages = intent.getParcelableArrayListExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
            video.selectedImages = intent.getParcelableArrayListExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
        }
        if (imagen.selectedImages == null)
            imagen.selectedImages = new ArrayList<>();
        imagen.images = new ArrayList<>();

        if (video.selectedImages == null)
            video.selectedImages = new ArrayList<>();
        video.images = new ArrayList<>();

        tabGallery.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int i = (tab.getPosition());
                indexTab = i;

                if(i==0)
                {

                    imagen.showVideo = false;
                    menuCamera.setIcon(R.drawable.ic_camera_white);
                    mode = MODE_MULTIPLE;
                    imagen.setFolderAdapter();
                    video.setFolderAdapter();
                    //imagen.reload();
                }else
                {
                    video.showVideo = true;
                    menuCamera.setIcon(R.drawable.ic_videocam_white_24dp);
                    mode = MODE_SINGLE;
                    imagen.setFolderAdapter();
                    video.setFolderAdapter();
                    //video.reload();
                }


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (menu.findItem(menuCameraId) == null) {
            menuCamera = menu.add(Menu.NONE, menuCameraId, 1, getString(R.string.camera));
            menuCamera.setIcon(R.drawable.ic_camera_white);
            menuCamera.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuCamera.setVisible(showCamera);
        }

        if (menu.findItem(menuDoneId) == null) {
            menuDone = menu.add(Menu.NONE, menuDoneId, 2, getString(R.string.done));
            menuDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        updateTitle(imageTitle);

        return true;
    }

    public static void updateTitle2(String titulo)
    {
        GalleryPickerActivity tmp = (GalleryPickerActivity)c;
        tmp.updateTitle(titulo);
    }

    private void updateTitle(String titulo) {
        if(indexTab==0) {

            if (menuDone != null && menuCamera != null) {
                if (imagen.isDisplayingFolderView()) {
                    actionBar.setTitle(folderTitle);
                    menuDone.setVisible(false);
                } else {
                    if (imagen.selectedImages.size() == 0) {
                        actionBar.setTitle(titulo);
                        if (menuDone != null)
                            menuDone.setVisible(false);
                    } else {
                        if (mode == GalleryPickerActivity.MODE_MULTIPLE) {
                            if (limit == Constants.MAX_LIMIT)
                                actionBar.setTitle(String.format(getString(R.string.selected), imagen.selectedImages.size()));
                            else
                                actionBar.setTitle(titulo + " " + String.format(getString(R.string.selected_with_limit), imagen.selectedImages.size(), limit));
                        }
                        if (menuDone != null)
                            menuDone.setVisible(true);
                    }
                }
            }
        }else
        {
            if (menuDone != null && menuCamera != null) {
                if (video.isDisplayingFolderView()) {
                    actionBar.setTitle(folderTitle);
                    menuDone.setVisible(false);
                } else {
                    if (video.selectedImages.size() == 0) {
                        actionBar.setTitle(titulo);
                        if (menuDone != null)
                            menuDone.setVisible(false);
                    } else {
                        if (mode == GalleryPickerActivity.MODE_MULTIPLE) {
                            if (limit == Constants.MAX_LIMIT)
                                actionBar.setTitle(String.format(getString(R.string.selected), video.selectedImages.size()));
                            else
                                actionBar.setTitle(titulo + " " + String.format(getString(R.string.selected_with_limit), video.selectedImages.size(), limit));
                        }
                        if (menuDone != null)
                            menuDone.setVisible(true);
                    }
                }
            }
        }
    }

    /**
     * Handle option menu's click event
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == menuDoneId) {

            if(indexTab==0) {
                if (imagen.selectedImages != null && imagen.selectedImages.size() > 0) {

                    /** Scan selected images which not existed */
                    for (int i = 0; i < imagen.selectedImages.size(); i++) {
                        Image image = imagen.selectedImages.get(i);
                        File file = new File(image.getPath());
                        if (!file.exists()) {
                            imagen.selectedImages.remove(i);
                            i--;
                        }
                    }

                    Intent data = new Intent();
                    data.putParcelableArrayListExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES, imagen.selectedImages);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
            else
            {
                if (video.selectedImages != null && video.selectedImages.size() > 0) {

                    /** Scan selected images which not existed */
                    for (int i = 0; i < video.selectedImages.size(); i++) {
                        Image image = video.selectedImages.get(i);
                        File file = new File(image.getPath());
                        if (!file.exists()) {
                            video.selectedImages.remove(i);
                            i--;
                        }
                    }

                    Intent data = new Intent();
                    data.putParcelableArrayListExtra(GalleryPickerActivity.INTENT_EXTRA_SELECTED_IMAGES, video.selectedImages);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
            return true;
        }

        if (id == menuCameraId) {
            //captureImage();
            if(indexTab == 0) {

                imagen.captureImageWithPermission();
            }
            else
            {
                video.captureImageWithPermission();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {

        if(indexTab == 0) {
            if (GalleryPickerActivity.folderMode && !imagen.isDisplayingFolderView()) {
                imagen.onBackPressed();
                return;
            } else {
                imagen.onBackPressed();

                super.onBackPressed();
            }
        }else
        {
            if (GalleryPickerActivity.folderMode && !video.isDisplayingFolderView()) {
                video.onBackPressed();
                return;
            } else {
                video.onBackPressed();

                super.onBackPressed();
            }
        }
    }


}
