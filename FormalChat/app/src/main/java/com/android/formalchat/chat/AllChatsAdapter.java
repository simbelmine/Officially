package com.android.formalchat.chat;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.formalchat.R;
import com.android.formalchat.RoundedImageView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sve on 8/21/15.
 */
public class AllChatsAdapter extends BaseAdapter {
    private Context context;
    private List<ParseObject> ids;

    public AllChatsAdapter(Context context, List<ParseObject> ids) {
        this.context = context;
        this.ids = ids;
    }


    @Override
    public int getCount() {
        if(ids != null) {
            return ids.size();
        }

        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        if(ids != null) {
            return ids.get(position);
        }

        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.chats_all_item, null);
            setViewOnClickListener(convertView, position);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        setMessageInfoToChatObj(holder, position);

        return convertView;
    }

    private void setViewOnClickListener(View convertView, final int position) {
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String senderId;
                String senderIdFromConversation = ids.get(position).getString("senderId");
                String receiverIdFromConversation = ids.get(position).getString("receiverId");

                if(ParseUser.getCurrentUser().getObjectId().equals(ids.get(position).getString("senderId"))) {
                    senderId = receiverIdFromConversation;
                }
                else {
                    senderId = senderIdFromConversation;
                }

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("senderId", senderId);
                context.startActivity(intent);
            }
        });
    }

    private void setMessageInfoToChatObj(final ViewHolder holder, int position) {
        String id;
        holder.position = position;

        holder.messageContent.setText(ids.get(position).getString("messageText"));

        if(ParseUser.getCurrentUser().getObjectId().equals(ids.get(position).getString("senderId"))) {
            id = ids.get(position).getString("receiverId");
            holder.messageName.setText(ids.get(position).getString("receiverName"));
        }
        else {
            id = ids.get(position).getString("senderId");
            holder.messageName.setText(ids.get(position).getString("senderName"));
        }

        new DownloadConversationImage(context, holder, position, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class DownloadConversationImage extends AsyncTask<ParseFile, Void, ParseFile> {
        private Context context;
        private ViewHolder viewHolder;
        private int position;
        private String id;

        public DownloadConversationImage(Context context, ViewHolder viewHolder, int position, String id) {
            this.context = context;
            this.viewHolder = viewHolder;
            this.position = position;
            this.id = id;
        }

        @Override
        protected ParseFile doInBackground(ParseFile... params) {
            ParseQuery<ParseUser> query = ParseQuery.getUserQuery();
            query.whereEqualTo("objectId", id);
            try {
                ParseUser pu = query.getFirst();
                return pu.getParseFile("profileImg");
            }
            catch (ParseException ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ParseFile parseFile) {
            if(viewHolder.position == position) {
                if(parseFile != null) {
                    Picasso.with(context).load(parseFile.getUrl()).into(viewHolder.messageImage);
                }
            }
        }
    }

    public void add(ParseObject conversation) {
        ids.add(conversation);
    }

    private static class ViewHolder {
        public RoundedImageView messageImage;
        public TextView messageName;
        public TextView messageContent;

        public int position;
    }

    private ViewHolder createViewHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.messageImage = (RoundedImageView) convertView.findViewById(R.id.chat_prof_pic);
        holder.messageName = (TextView) convertView.findViewById(R.id.chat_prof_name);
        holder.messageContent = (TextView) convertView.findViewById(R.id.chat_prof_message);

        return holder;
    }
}
