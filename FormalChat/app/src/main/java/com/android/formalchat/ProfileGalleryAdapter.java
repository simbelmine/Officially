package com.android.formalchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

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
    private static final String OUT_VID_EXTENSION = "out_VID";
    private static final int OUT_VID_EXT_SHIFT = 7;
    private File dir = Environment.getExternalStorageDirectory();
    private String filePath = "/.formal_chat/";
    private String fileName;
    private File tmpFile;
    private ParseFile videoFile;
    private BroadcastReceiver broadcastReceiver;
    private Activity activity;
    private Bitmap thumbnail;
    private Uri videoUri;

    public ProfileGalleryAdapter(Activity activity, Context context, List<String> paths) {
        this.activity = activity;
        this.context = context;
        this.images = paths;
    }

    public void updateImages(List<String> paths) {
        this.images.clear();
        this.images.addAll(paths);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e("formalchat", "position....  " + position + "    " + images.get(position));

        // **************************//
        //**** To Do: Recycle grid views DOESN'T work ****//
        // *************************//
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.viewpager_item, parent, false);

        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        VideoView videoView = (VideoView) convertView.findViewById(R.id.video);
        ImageView img = (ImageView) convertView.findViewById(R.id.image);

        populateImages(convertView, img, progressBar, position);
        addImageOnClickListener(img, position);
        return convertView;
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

    private void populateImages(final View view, final ImageView img, final ProgressBar progressBar, final int position) {
        Picasso.with(context).load(images.get(position)).into(img, new Callback() {
            @Override
            public void onSuccess() {
                img.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                if (isVideo(position)) {
                    initBroadcastReceiver();
                    context.registerReceiver(broadcastReceiver, new IntentFilter(VideoDownloadService.NOTIFICATION));
                    downloadVideoIfNotExists();

                    float gallery_item_h = context.getResources().getDimension(R.dimen.gallery_item_h);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (int)gallery_item_h);
                    addTransparentPlayImage(view);
                    reCreateImageView(layoutParams, img);
                    setVideoImgOnClickListener(img);
                    progressBar.setVisibility(View.GONE);
                    context.unregisterReceiver(broadcastReceiver);
                }
            }
        });
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
        int startIdx = path.lastIndexOf("-")+1;
        int endIdx = startIdx + OUT_VID_EXT_SHIFT;
        return path.substring(startIdx, endIdx);
    }

    /////////////////////////////
    /// ***** For Video ***** ///
    /////////////////////////////

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if(bundle != null) {
                    int resultCode = bundle.getInt(VideoDownloadService.RESULT);
                    if(resultCode == RESULT_OK) {
                        thumbnail  = getVideoThumbnail();
                    }
                    else {
                        Log.e("formalchat", "DoWnLoAd Failed .... !!!");
                    }
                }
            }
        };
    }

    private void downloadVideoIfNotExists() {
        ParseUser user = ParseUser.getCurrentUser();
        videoFile = user.getParseFile("video");
        fileName = videoFile.getName();
        String shortName = getShortImageNameFromUri(fileName);
        videoUri = Uri.parse(dir + filePath + shortName);

        File targetFolder = new File(dir + filePath);
        if(!targetFolder.exists()) {
            targetFolder.mkdir();
        }

        tmpFile = new File(dir, filePath + shortName);
        if(!tmpFile.exists()) {
            startVideoDownloadService();
        }
        else {
            thumbnail  = getVideoThumbnail();
        }
    }

    private Bitmap getVideoThumbnail() {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(),
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        return thumb;
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

    private void addTransparentPlayImage(View itemView) {
        ImageView playImageView = (ImageView) itemView.findViewById(R.id.image_play);
        playImageView.setVisibility(View.VISIBLE);
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
