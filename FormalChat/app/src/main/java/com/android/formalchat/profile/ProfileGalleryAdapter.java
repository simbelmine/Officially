package com.android.formalchat.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.android.formalchat.R;
import com.android.formalchat.VideoDownloadService;
import com.android.formalchat.VideoShowActivity;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 5/21/15.
 */
public class ProfileGalleryAdapter extends BaseAdapter {
    private List<String> images;
    private Context context;
    /////// ***** For the Video File *****
    private static final int RESULT_OK = -1;
    private static final String OUT_VID_EXTENSION = "mp4";
    private static final int OUT_VID_EXT_SHIFT = 3;
    public static final String ACTION_DELETE_ALL = "DeleteAllGalleryPics";
    private File dir = Environment.getExternalStorageDirectory();
    private String filePath = "/.formal_chat/";
    private String fileName;
    private File tmpFile;
    private ParseFile videoFile;
    private BroadcastReceiver broadcastReceiver;
    private Activity activity;
    private Bitmap thumbnail;
    private Uri videoUri;
    private ParseUser user;
    private ArrayList<Integer> selectedItems;
    private boolean atLeastOnePicSelected;

    public ProfileGalleryAdapter(Activity activity, Context context, List<String> paths, ParseUser user) {
        this.activity = activity;
        this.context = context;
        this.images = paths;
        this.user = user;

        atLeastOnePicSelected = false;
        selectedItems = new ArrayList<>();
    }

    public void updateImages(List<String> paths, ParseUser user) {
        atLeastOnePicSelected = false;
        selectedItems = new ArrayList<>();

        this.images = paths;
        this.user = user;
        this.notifyDataSetChanged();
    }

    public List<String> getImagePaths() {
        return images;
    }

    public ArrayList<Integer> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        // **************************//
        //**** To Do: Recycle grid views DOESN'T work ****//
        // *************************//

        final ViewHolder viewHolder;

        if(convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.profile_gallery_item, parent, false);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.videoView = (VideoView) convertView.findViewById(R.id.video);
        viewHolder.img = (ImageView) convertView.findViewById(R.id.image);
        viewHolder.multiSelectIcon = (ImageView) convertView.findViewById(R.id.multi_select_icon);

        populateImages(viewHolder.img, viewHolder.multiSelectIcon, viewHolder.progressBar, position);

        return convertView;
    }

    public static class ViewHolder {
        ProgressBar progressBar;
        VideoView videoView;
        ImageView img;
        ImageView multiSelectIcon;
    }


    private void addImageOnClickListener(ImageView img, final ImageView multiselectIcon, final int position) {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (atLeastOnePicSelected) {
                    if (isPositionSelected(position)) {
                        unselectImage(multiselectIcon, position);
                    }
                } else {
                    openOnFullScreen(position);
                }
            }
        });
    }

    private void addImageOnLongClickListener(ImageView img, final ImageView multiSelectIcon, final int position) {
        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                multiSelectIcon.setVisibility(View.VISIBLE);
                selectedItems.add(position);
                if (!atLeastOnePicSelected) {
                    atLeastOnePicSelected = true;
                    sendUpdateMessageToMenu();
                }
                return true;
            }
        });
    }

    private boolean isPositionSelected(int position) {
        if(selectedItems.contains(position)) {
            return true;
        }
        return false;
    }

    private void unselectImage(ImageView multiselectIcon, int position) {
        multiselectIcon.setVisibility(View.GONE);
        removeFromSelected(position);
    }

    private void removeFromSelected(int position) {
        Log.v("formalchat", "selectedItems : " + selectedItems);
        Log.v("formalchat", "position : " + position);

        for(int i = 0; i < selectedItems.size(); i++) {
            if(selectedItems.get(i) == position) {
                selectedItems.remove(i);
            }
        }

        if(selectedItems.size() == 0) {
            hideDeleteFromMenu();
        }

        Log.v("formalchat", "selectedItems AFTER: " + selectedItems);
    }

    private void hideDeleteFromMenu() {
        atLeastOnePicSelected = false;
        sendUpdateMessageToMenu();
    }

    private void openOnFullScreen(int position) {
        Intent i = new Intent(context, FullImageActivity.class);
        i.putExtra("url", images.get(position));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    private void populateImages(ImageView img, final ImageView multiSelectionIcon, final ProgressBar progressBar, final int position) {
        if (isVideo(position)) {

            downloadVideoIfNotExists();
            reCreateImageView(img);
            setVideoImgOnClickListener(img, position);
            progressBar.setVisibility(View.GONE);

        } else {
            String thumbnailPath = getImageThumbnailPath(images.get(position));

            if (thumbnailPath != null) {
                loadPictureToGrid(img, multiSelectionIcon, position, thumbnailPath, progressBar);
            } else {
                downloadPictureBeforeLoadToGrid(img, multiSelectionIcon, position, thumbnailPath, progressBar);
            }

            showSelectionIconIfSelected(multiSelectionIcon, position);

        }
    }

    private void showSelectionIconIfSelected(ImageView multiSelectionIcon, int position) {
        if (selectedItems.contains(position)) {
            multiSelectionIcon.setVisibility(View.VISIBLE);
        } else {
            multiSelectionIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void downloadPictureBeforeLoadToGrid(final ImageView img, final ImageView multiSelectionIcon, final int position, final String thumbnailPath, final ProgressBar progressBar) {
        ParseQuery<ParseObject> query = new ParseQuery<>("UserImages");
        query.whereEqualTo("userName", user.getUsername());
        query.whereEqualTo("photo", getLongerImageNameFromUri(images.get(position)));
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null && parseObject != null) {
                    ParseFile imageFile = parseObject.getParseFile("photo");
                    imageFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            saveImageToLocal(bytes, position);
                            loadPictureToGrid(img, multiSelectionIcon, position, thumbnailPath, progressBar);
                        }
                    });
                }
            }
        });
    }

    private void saveImageToLocal(byte[] fileBytes, int position) {
        String shortName = getShortImageNameFromUri(images.get(position));
        File file = new File(dir, filePath + shortName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(fileBytes);
            bos.flush();
            bos.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void loadPictureToGrid(final ImageView img, final ImageView multiSelectIcon, final int position, String thumbnailPath, final ProgressBar progressBar) {
        if(getBitmapFactoryOptions(thumbnailPath) != null) {
            int imageHeight = getBitmapFactoryOptions(thumbnailPath).outHeight;
            int imageWidth = getBitmapFactoryOptions(thumbnailPath).outWidth;

            if (imageHeight > 0 && imageWidth > 0) {

                Picasso.with(context).load("file://" + thumbnailPath).resize(imageWidth / 4,
                        imageHeight / 4).into(img, new Callback() {
                    @Override
                    public void onSuccess() {
                        addImageOnClickListener(img, multiSelectIcon, position);
                        addImageOnLongClickListener(img, multiSelectIcon, position);

                        img.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        }
    }

    private void sendUpdateMessageToMenu() {
        Intent intent = new Intent(ACTION_DELETE_ALL);
        intent.putExtra("showDeleteAll", atLeastOnePicSelected);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private BitmapFactory.Options getBitmapFactoryOptions(String thumbnailPath) {
        if(thumbnailPath!= null && !thumbnailPath.equals("")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(thumbnailPath, options);
            return options;
        }
        return null;
    }

    private String getImageThumbnailPath(String path) {
        String shortName = getShortImageNameFromUri(path);
        File tmpFile = new File(dir, filePath + shortName);
        if (tmpFile.exists()) {
            return tmpFile.getAbsolutePath();
        }

        return null;
    }

    private boolean isVideo(int position) {
        String path = images.get(position);
        String extension = getExtention(path);

        if(OUT_VID_EXTENSION.equals(extension)) {
            return true;
        }
        return false;
    }

    private String getExtention(String path) {
        int startIdx = path.lastIndexOf(".")+1;
        int endIdx = startIdx + OUT_VID_EXT_SHIFT;
        return path.substring(startIdx, endIdx);
    }

    /////////////////////////////
    /// ***** For Video ***** ///
    /////////////////////////////

    private void downloadVideoIfNotExists() {
        if(user != null) {
            videoFile = user.getParseFile("video");
            fileName = videoFile.getName();
            String shortName = getShortImageNameFromUri(fileName);
            videoUri = Uri.parse(dir + filePath + shortName);

            createTargetFolderIfNotExists();

            tmpFile = new File(dir, filePath + shortName);

            if (!tmpFile.exists()) {
                startVideoDownloadService();
            } else {
                thumbnail = getVideoThumbnail();
            }
        }
    }

    private void createTargetFolderIfNotExists() {
        File targetFolder = new File(dir + filePath);
        if (!targetFolder.exists()) {
            targetFolder.mkdir();
        }
    }

    private Bitmap getVideoThumbnail() {
        createTargetFolderIfNotExists();

        File videoThumbnail = new File(dir, filePath + "video_thumbnail.jpg");
        if(videoThumbnail.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(videoThumbnail.getAbsolutePath(), options);
            return bitmap;
        }
        else {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(),
                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            Bitmap playImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.play_g_);

            return overlayThumbnailWithPlayIcon(thumb, playImage);
        }
    }

    private String getVideoThumbnailPath(Bitmap videoThumbnail) {
        createTargetFolderIfNotExists();

        File thumb = new File(dir, filePath + "video_thumbnail.jpg");
        try {
            if (!thumb.exists()) {
                FileOutputStream outStream = new FileOutputStream(thumb);
                videoThumbnail.compress(Bitmap.CompressFormat.PNG, 0, outStream);
                outStream.close();
            }

            return thumb.getAbsolutePath();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private Bitmap overlayThumbnailWithPlayIcon(Bitmap thumb, Bitmap playImage) {
        Bitmap overlayBmp = Bitmap.createBitmap(thumb.getWidth(), thumb.getHeight(), thumb.getConfig());
        float columnWidth = context.getResources().getDimension(R.dimen.grid_column_width);
        Bitmap play = Bitmap.createScaledBitmap(playImage, (int)columnWidth/2, (int)columnWidth/2, true);

        Canvas canvas = new Canvas(overlayBmp);
        canvas.drawBitmap(thumb, new Matrix(), null);
        canvas.drawBitmap(play, (int) columnWidth / 2 - (play.getHeight() / 3), (int) columnWidth / 2, null);

        overlayBmp = compressBitmapImg(overlayBmp);

        return overlayBmp;
    }

    private Bitmap compressBitmapImg(Bitmap bitmapImg) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
        byte[] imageArray = outputStream.toByteArray();

        return BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
    }

    public String getShortImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-") + 1);
    }

    public String getLongerImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("/")+1);
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(context, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, filePath);

        context.startService(intent);
    }

    private void reCreateImageView(ImageView imageView) {
        float gallery_item_h = context.getResources().getDimension(R.dimen.gallery_item_h);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) gallery_item_h);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setVisibility(View.VISIBLE);
//        imageView.setImageBitmap(thumbnail);
//        Picasso.with(context).load("file://"+getVideoThumbnailPath(thumbnail)).into(imageView);

        Picasso.with(context).load(R.drawable.play_gimp).into(imageView);
    }

    private void setVideoImgOnClickListener(ImageView imageView, final int position) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoShowActivity(position);
            }
        });
    }

    private void openVideoShowActivity(int position) {
        Intent intent = new Intent(activity, VideoShowActivity.class);

        if(user != ParseUser.getCurrentUser()) {
            intent.putExtra("videoUri", images.get(position));
        }
        else {
            intent.putExtra("videoUri", videoUri.toString());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
