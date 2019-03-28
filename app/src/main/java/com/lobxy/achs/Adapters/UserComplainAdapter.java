package com.lobxy.achs.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lobxy.achs.Model.UserComplaints;
import com.lobxy.achs.R;

import java.util.List;

public class UserComplainAdapter extends ArrayAdapter<UserComplaints> {

    private Activity context;
    private List<UserComplaints> complaintList;

    public UserComplainAdapter(Activity context, List<UserComplaints> complaintList) {
        super(context, R.layout.complaints_user_list_item, complaintList);
        this.context = context;
        this.complaintList = complaintList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = convertView;

        if (listViewItem == null) {
            listViewItem = inflater.inflate(R.layout.complaints_user_list_item, null, true);
        }

        TextView complaintDate = listViewItem.findViewById(R.id.complaintDate);
        TextView complaintType = listViewItem.findViewById(R.id.complaintType);
        TextView completionStatus = listViewItem.findViewById(R.id.completionStatus);
        TextView supervisorName = listViewItem.findViewById(R.id.supervisorName);

        UserComplaints myComplaints = complaintList.get(position);

        complaintDate.setText("Complaint Time: " + myComplaints.getComplaintInitTime());
        complaintType.setText("Type: " + myComplaints.getType());
        completionStatus.setText("Status: " + myComplaints.getCompletionStatus());
        supervisorName.setText("Supervisor Name: " + myComplaints.getSupervisorName());

        return listViewItem;

    }

}