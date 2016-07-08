package com.patr.radix.adapter;

import java.util.List;

import com.patr.radix.R;
import com.patr.radix.bean.Notice;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NoticeAdapter extends AbsListAdapter<Notice> {

    public NoticeAdapter(Context context, List<Notice> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_notice, null);
            holder.pic = (ImageView) convertView.findViewById(R.id.item_notice_iv);
            holder.title = (TextView) convertView.findViewById(R.id.item_notice_title_tv);
            holder.content = (TextView) convertView.findViewById(R.id.item_notice_content_tv);
            holder.from = (TextView) convertView.findViewById(R.id.item_notice_from_tv);
            holder.sentDate = (TextView) convertView.findViewById(R.id.item_notice_datetime_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Notice notice = getItem(position);
        String imgUrl = notice.getImgUrl();
        if (!TextUtils.isEmpty(imgUrl)) {
            holder.pic.setVisibility(View.VISIBLE);
        }
        holder.title.setText(notice.getTitle());
        holder.content.setText(notice.getContent());
        holder.from.setText(notice.getFrom());
        holder.sentDate.setText(notice.getSentDate());
        return convertView;
        
    }
    
    class ViewHolder {
        ImageView pic;
        TextView title;
        TextView content;
        TextView from;
        TextView sentDate;
    }
}
