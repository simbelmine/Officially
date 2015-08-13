package com.android.formalchat.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.android.formalchat.R;
import com.android.formalchat.VideoDownloadService;
import com.android.formalchat.VideoShowActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;
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
    private String videoThumbName = "thumbnail_video.jpg";
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
    private ProgressBar progressBar;

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

        ViewHolder viewHolder;

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
        this.progressBar = viewHolder.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        viewHolder.videoView = (VideoView) convertView.findViewById(R.id.video);
        viewHolder.img = (ImageView) convertView.findViewById(R.id.image);
        viewHolder.multiSelectIcon = (ImageView) convertView.findViewById(R.id.multi_select_icon);

        showSelectionIconIfSelected(viewHolder.multiSelectIcon, position);
        loadPictureToGrid(viewHolder.img, viewHolder.multiSelectIcon, position);

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
        for(int i = 0; i < selectedItems.size(); i++) {
            if(selectedItems.get(i) == position) {
                selectedItems.remove(i);
            }
        }

        if(selectedItems.size() == 0) {
            hideDeleteFromMenu();
        }
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


    private void showSelectionIconIfSelected(ImageView multiSelectionIcon, int position) {
        if (selectedItems.contains(position)) {
            multiSelectionIcon.setVisibility(View.VISIBLE);
        } else {
            multiSelectionIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void saveImageToLocal(byte[] fileBytes, String shortName) {
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

//    22:18:45.828  26631-27583/com.android.formalchat E/pix﹕ 1920 1080 2073600
//            08-12 22:18:47.767  26631-27583/com.android.formalchat E/pix﹕ 1920 1080 2073600

    private void loadPictureToGrid(final ImageView img, final ImageView multiSelectIcon, final int position) {
//        if(getAfterLastSlashUriName(thumbnailPath).equals(videoThumbName)){
        if(getShortImageNameFromUri(images.get(position)).equals(videoThumbName)){
            img.setVisibility(View.VISIBLE);
            setVideoImgOnClickListener(img, position);
//            Picasso.with(context).load("file://" + thumbnailPath).into(img);
            Picasso.with(context).load(images.get(position))
                    .into(img);
            progressBar.setVisibility(View.GONE);
        }
        else {
//            if (getBitmapFactoryOptions(thumbnailPath) != null) {
//                BitmapFactory.Options options = getBitmapFactoryOptions(thumbnailPath);
//                int imageHeight = options.outHeight;
//                int imageWidth = options.outWidth;
//
//                if (imageHeight > 0 && imageWidth > 0) {

            addImageOnClickListener(img, multiSelectIcon, position);
            addImageOnLongClickListener(img, multiSelectIcon, position);
//                    Picasso.with(context).load("file://" + thumbnailPath)
//                            //.resize(imageWidth / 4, imageHeight / 4)
//                            .resize(20, 20)
//                            .into(img);


            Picasso.with(context).load(images.get(position))
                    //.resize(imageWidth / 4, imageHeight / 4)
                    .resize(200, 200)
                    .into(img);


            progressBar.setVisibility(View.GONE);
//                }
//            }
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
                // thumbnail = getVideoThumbnail();
            }
        }
    }

    private void createTargetFolderIfNotExists() {
        File targetFolder = new File(dir + filePath);
        if (!targetFolder.exists()) {
            targetFolder.mkdir();
        }
    }

    public String getShortImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-") + 1);
    }

    public String getAfterLastSlashUriName(String url) {
        return url.substring(url.lastIndexOf("/")+1);
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(context, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, filePath);

        context.startService(intent);
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
