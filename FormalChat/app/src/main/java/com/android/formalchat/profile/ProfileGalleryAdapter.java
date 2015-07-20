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
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
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

    public ProfileGalleryAdapter(Activity activity, Context context, List<String> paths, ParseUser user) {
        this.activity = activity;
        this.context = context;
        this.images = paths;
        this.user = user;
    }

    public void updateImages(List<String> paths) {
       // this.images.clear();
        //this.images.addAll(paths_);
        this.images = paths;
        this.notifyDataSetChanged();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        // **************************//
        //**** To Do: Recycle grid views DOESN'T work ****//
        // *************************//

        ViewHolder viewHolder;

        if(convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.viewpager_item, parent, false);

            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.videoView = (VideoView) convertView.findViewById(R.id.video);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        populateImages(viewHolder.img, viewHolder.progressBar, position);

        return convertView;
    }

    public static class ViewHolder {
        ProgressBar progressBar;
        VideoView videoView;
        ImageView img;
    }


    private void addImageOnClickListener(ImageView img, final int position) {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, FullImageActivity.class);
                i.putExtra("url", images.get(position));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });
    }

    private void populateImages(final ImageView img, final ProgressBar progressBar, final int position) {
        if(isVideo(position)) {
            downloadVideoIfNotExists();

            float gallery_item_h = context.getResources().getDimension(R.dimen.gallery_item_h);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) gallery_item_h);
            reCreateImageView(layoutParams, img);
            setVideoImgOnClickListener(img);
            progressBar.setVisibility(View.GONE);
        }
        else if(!isVideo(position) && position != 0){
            Picasso.with(context).load(images.get(position)).into(img, new Callback() {
                @Override
                public void onSuccess() {
                    addImageOnClickListener(img, position);
                    img.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {

                }
            });
        }
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

            File targetFolder = new File(dir + filePath);
            if (!targetFolder.exists()) {
                targetFolder.mkdir();
            }

            tmpFile = new File(dir, filePath + shortName);
            if (!tmpFile.exists()) {
                startVideoDownloadService();
            } else {
                thumbnail = getVideoThumbnail();
            }
        }
    }

    private Bitmap getVideoThumbnail() {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(),
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        Bitmap playImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.play_g_);

        return overlayThumbnailWithPlayIcon(thumb, playImage);
    }

    private Bitmap overlayThumbnailWithPlayIcon(Bitmap thumb, Bitmap playImage) {
        Bitmap overlayBmp = Bitmap.createBitmap(thumb.getWidth(), thumb.getHeight(), thumb.getConfig());
        float columnWidth = context.getResources().getDimension(R.dimen.grid_column_width);
        Bitmap play = Bitmap.createScaledBitmap(playImage, (int)columnWidth/2, (int)columnWidth/2, true);

        Canvas canvas = new Canvas(overlayBmp);
        canvas.drawBitmap(thumb, new Matrix(), null);
        canvas.drawBitmap(play, (int) columnWidth / 2 - (play.getHeight() / 3), (int) columnWidth / 2, null);

        return overlayBmp;
    }

    public String getShortImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-")+1);
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(context, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, filePath);

        context.startService(intent);
    }

    private void reCreateImageView(FrameLayout.LayoutParams layoutParams, ImageView imageView) {
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(thumbnail);
    }

    private void setVideoImgOnClickListener(ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoShowActivity();
            }
        });
    }

    private void openVideoShowActivity() {
        Intent intent = new Intent(activity, VideoShowActivity.class);
        intent.putExtra("videoUri", videoUri.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
