package com.lobxy.achs.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lobxy.achs.R;
import com.lobxy.achs.Model.Complain;

import java.util.List;

public class ComplaintAdapter extends ArrayAdapter<Complain> {
    private Activity context;
    private List<Complain> requestList;

    public ComplaintAdapter(Activity context, List<Complain> requestList) {
        super(context, R.layout.complaint_listitem, requestList);
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.complaint_listitem, null, true);

        TextView textViewId = listViewItem.findViewById(R.id.ComplaintId);
        TextView textViewStatus = listViewItem.findViewById(R.id.status);
        TextView textViewInitTime = listViewItem.findViewById(R.id.initTime);

        Complain complaints = requestList.get(position);
        textViewId.setText(complaints.getName());
        textViewInitTime.setText(complaints.getComplaintInitTime());
        textViewStatus.setText(complaints.getCompletionStatus());

        return listViewItem;
    }
}
