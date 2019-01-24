package com.lobxy.achs.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.lobxy.achs.R;

public class ComplaintHolder extends RecyclerView.ViewHolder {
    public TextView id;
    public TextView init;
    public TextView status;

    public ComplaintHolder(View itemView) {
        super(itemView);
        id = itemView.findViewById(R.id.ComplaintId);
        init = itemView.findViewById(R.id.initTime);
        status = itemView.findViewById(R.id.status);
    }

    public void setId(String d) {
        id.setText(d);
    }

    public void setInit(String i) {
        init.setText(i);
    }

    public void setStatus(String s) {
        status.setText(s);
    }

    public interface listener {
        void onItemClicked(ComplaintHolder item);
    }
}

