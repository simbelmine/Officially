package com.android.formalchat.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.formalchat.R;

import java.util.List;

/**
 * Created by Sve on 8/18/15.
 */
public class ChatAdapter extends BaseAdapter {
    private List<ChatMessage> chatMessages;
    private Context context;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if(chatMessages != null) {
            return chatMessages.size();
        }

        return 0;
    }

    @Override
    public ChatMessage getItem(int position) {
        if(chatMessages != null) {
            return chatMessages.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage chatMessage = chatMessages.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.chat_bubble_layout, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }


        boolean myMsg = chatMessage.getIsme() ;//Just a dummy check
        //to simulate whether it me or other sender

        setAlignment(holder, myMsg);
        holder.txtMessage.setText(chatMessage.getMessage());
        holder.txtInfo.setText(chatMessage.getDate());

        return convertView;
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout contentLayout;
        public LinearLayout contentWithBG;
    }

    private ViewHolder createViewHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) convertView.findViewById(R.id.textMessage);
        holder.txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);
        holder.contentLayout = (LinearLayout) convertView.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) convertView.findViewById(R.id.contentWithBackground);

        return holder;
    }

    private void setAlignment(ViewHolder holder, boolean isMe) {
        if(!isMe) {
            holder.contentWithBG.setBackgroundResource(R.drawable.chat_bubble_out);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams rlLayoutParams = (RelativeLayout.LayoutParams) holder.contentLayout.getLayoutParams();
            rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.contentLayout.setLayoutParams(rlLayoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);
            holder.txtMessage.setTextColor(context.getResources().getColor(R.color.white));

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
        else {
            holder.contentWithBG.setBackgroundResource(R.drawable.chat_bubble_in);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.contentLayout.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.contentLayout.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);
            holder.txtMessage.setTextColor(context.getResources().getColor(R.color.white));

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    public void add(List<ChatMessage> messages) {
        chatMessages.addAll(messages);
    }
}
