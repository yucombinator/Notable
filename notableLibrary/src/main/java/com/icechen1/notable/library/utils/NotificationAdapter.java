package com.icechen1.notable.library.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icechen1.notable.library.R;

import java.util.Date;

import static humanize.Humanize.naturalTime;

public class NotificationAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder, Cursor> implements Filterable {
    private Context mContext;
    private int mSelectedColor;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTitle;
        public final TextView mSubTitle;
        public final TextView mAlarm;
        private final TextView mDate;
        private final RelativeLayout mLayout;
        private final ImageView mIcon;

        public Integer mId;
        public Boolean mDismissed;

        public ViewHolder(View itemView) {
            super(itemView);

            mLayout = (RelativeLayout) itemView.findViewById(R.id.layout);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mSubTitle = (TextView) itemView.findViewById(R.id.subtitle);
            mAlarm = (TextView) itemView.findViewById(R.id.alarm);
            mDate = (TextView) itemView.findViewById(R.id.date);
        }
    }

    public NotificationAdapter(Context context,Cursor cursor){
        super(context, cursor);
        mContext = context;
        Resources.Theme themes = context.getTheme();
        TypedValue storedValueInTheme = new TypedValue();
        if (themes.resolveAttribute(R.attr.selected, storedValueInTheme, true)) {
           mSelectedColor = storedValueInTheme.data;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final Cursor cursor) {
        final ViewHolder vh = (ViewHolder) holder;
        final int position = cursor.getPosition();
        final int id = cursor.getInt(0);
        final boolean dismissed = (cursor.getInt(6) == 1);
        final long alarm = cursor.getLong(5);

        vh.mTitle.setText(cursor.getString(1));
        vh.mSubTitle.setText(cursor.getString(2));
        vh.mDate.setText(String.valueOf(naturalTime(new Date(cursor.getLong(3)))));
        vh.mId = id;
        vh.mDismissed = dismissed;
        String icon = cursor.getString(4);

        switch (icon) {
            case "checkmark_gray":
                vh.mIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_checkmark_blue));
                break;
            case "checkmark_green":
                vh.mIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_checkmark_green));
                break;
            case "checkmark_orange":
                vh.mIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_checkmark_orange));
                break;
            case "checkmark_red":
                vh.mIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_checkmark_red));
                break;
        }

        if(alarm > 0){
            vh.mAlarm.setText(String.valueOf(naturalTime(new Date(alarm))));
            vh.mAlarm.setVisibility(View.VISIBLE);
        } else {
            vh.mAlarm.setText(mContext.getResources().getString(R.string.not_set));
            vh.mAlarm.setVisibility(View.INVISIBLE);
        }

        if(dismissed){
            vh.mLayout.setBackgroundColor(mSelectedColor);
            vh.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        else
        {
            vh.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, com.icechen1.notable.library.DetailActivity_.class);
                    Bundle iBundle = new Bundle();
                    iBundle.putInt("id", id);
                    i.putExtras(iBundle);
                    mContext.startActivity(i);
                }
            });
            vh.mLayout.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification_item, parent, false);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return false;

                }
            });
        }*/
        return new ViewHolder(view);
    }

    public void refreshCursor(Cursor c) {
        changeCursor(c);
    }


    @Override
    public Filter getFilter() {
        return new CursorFilter();
    }

    class CursorFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            //Here you have to implement filtering way
            final FilterResults results = new FilterResults();

            Cursor cursor = null; //TODO to implement
            //logic to filtering
            results.count = cursor.getCount();
            results.values = cursor;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // here you can use result - (f.e. set in in adapter list)
            NotificationAdapter.this.swapCursor((Cursor) results.values);
        }
    }

}