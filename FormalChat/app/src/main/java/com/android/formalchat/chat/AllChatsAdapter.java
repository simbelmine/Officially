package com.android.formalchat.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.formalchat.FormalChatApplication;
import com.android.formalchat.R;
import com.android.formalchat.RoundedImageView;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sve on 8/21/15.
 */
public class AllChatsAdapter extends BaseAdapter {
    private Context context;
    private List<ChatMessage> chatMessages;
    private ArrayList<String> senderIds;
    private List<ParseObject> ids;

//    public AllChatsAdapter(Context context, List<ChatMessage> chatMessages) {
//        this.context = context;
//        this.chatMessages = chatMessages;
//    }

    public AllChatsAdapter(Context context, ArrayList<String> senderIds) {
        this.context = context;
        this.senderIds = senderIds;
    }

    public AllChatsAdapter(Context context, List<ParseObject> ids) {
        this.context = context;
        this.ids = ids;
    }


    @Override
    public int getCount() {
//        if(chatMessages != null) {
//            return chatMessages.size();
//        }
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
//        if(chatMessages != null) {
//            return chatMessages.get(position);
//        }

        if(ids != null) {
            return ids.get(position);
        }

        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
//        ChatMessage chatMessage = chatMessages.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.chats_all_item, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        //setMessageInfo(holder, chatMessage);

//        setMessageInfoById(holder, position);

        setMessageInfoToChatObj(holder, position);

        return convertView;
    }

    private void setMessageInfoToChatObj(final ViewHolder holder, int position) {
        String id;

        if(ParseUser.getCurrentUser().getObjectId().equals(ids.get(position).getString("senderId"))) {
            id = ids.get(position).getString("receiverId");
        }
        else {
            id = ids.get(position).getString("senderId");
        }

        ParseQuery<ParseUser> query = ParseQuery.getUserQuery();
        query.whereEqualTo("objectId", id);
        try {
            ParseUser pu = query.getFirst();

            holder.messageName.setText(pu.getString("username"));
            ParseFile image = pu.getParseFile("profileImg");
            Picasso.with(context).load(image.getUrl()).into(holder.messageImage);
        }
        catch (ParseException ex) {
            ex.printStackTrace();
        }


        holder.messageContent.setText(ids.get(position).getString("messageBody"));
    }

    private void setMessageInfoById(final ViewHolder holder, int position) {
        Log.e(FormalChatApplication.TAG, "# All Chats: IN ");

        ParseQuery<Message> parseQuery = ParseQuery.getQuery("Message");
        parseQuery.whereEqualTo("senderId", senderIds.get(position));
        parseQuery.whereEqualTo("receiverId", ParseUser.getCurrentUser().getObjectId());
        parseQuery.addDescendingOrder("timeSent");
        parseQuery.getFirstInBackground(new GetCallback<Message>() {
            @Override
            public void done(Message message, ParseException e) {
                if (e == null) {
                    holder.messageName.setText(message.getSenderId());
                } else {
                    Log.e(FormalChatApplication.TAG, "All Chats: no user Found!  " + e.getMessage());
                }
            }
        });
    }

    private void setMessageInfo(final ViewHolder holder, final ChatMessage chatMessage) {
        String senderId = chatMessage.getUserIdAsString();
        Log.e(FormalChatApplication.TAG, "# All Chats: senderId = " + senderId);
        if(senderId != null) {
            ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
            parseQuery.getInBackground(senderId, new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null && parseUser != null) {
                        holder.messageName.setText(parseUser.getUsername());
                        Log.v(FormalChatApplication.TAG, "# All Chats: messageName = " + parseUser.getUsername());
                        Log.v(FormalChatApplication.TAG, "# All Chats: chatMessage = " + chatMessage.getMessage());
                        if (chatMessage.getMessage() != null) {
                            holder.messageContent.setText(chatMessage.getMessage());
                        }
                        else {
                            holder.messageContent.setText(context.getResources().getString(R.string.none_txt));
                        }

                        ParseFile image = parseUser.getParseFile("profileImg");
                        image.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                if (e == null && bytes.length > 0) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                    if (bmp != null) {
                                        holder.messageImage.setImageBitmap(bmp);
                                    }
                                }
                            }
                        });
                    }
                    {
                        Log.e(FormalChatApplication.TAG, "All Chats: no user Found!");
                    }
                }
            });
        }
        else {
            holder.messageImage.setImageDrawable(context.getResources().getDrawable(R.drawable.profile_pic));
            holder.messageName.setText(context.getResources().getString(R.string.none_txt));
            holder.messageContent.setText(context.getResources().getString(R.string.none_txt));
        }
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    public void add(ParseObject conersation) {
        ids.add(conersation);
    }

    private static class ViewHolder {
        public RoundedImageView messageImage;
        public TextView messageName;
        public TextView messageContent;
    }

    private ViewHolder createViewHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.messageImage = (RoundedImageView) convertView.findViewById(R.id.chat_prof_pic);
        holder.messageName = (TextView) convertView.findViewById(R.id.chat_prof_name);
        holder.messageContent = (TextView) convertView.findViewById(R.id.chat_prof_message);

        return holder;
    }
}
