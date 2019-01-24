package com.lobxy.achs.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lobxy.achs.Model.UserComplains;
import com.lobxy.achs.R;

import java.util.List;

public class UserComplainAdapter extends ArrayAdapter<UserComplains> {

    private Activity context;
    private List<UserComplains> complaintList;

    public UserComplainAdapter(Activity context, List<UserComplains> complaintList) {
        super(context, R.layout.complaints_user_list_item, complaintList);
        this.context = context;
        this.complaintList = complaintList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.complaints_user_list_item, null, true);
        //type, requestId, happyCode, complaintInitTime, completionStatus;

        TextView happyCode = listViewItem.findViewById(R.id.happycode);
        TextView complaintDate = listViewItem.findViewById(R.id.complaintDate);
        TextView complaintType = listViewItem.findViewById(R.id.complaintType);
        TextView completionStatus = listViewItem.findViewById(R.id.completionStatus);

        UserComplains myComplaints = complaintList.get(position);

        happyCode.setText("Happy Code: " + myComplaints.getHappyCode());
        complaintDate.setText("Complaint Time: " + myComplaints.getComplaintInitTime());
        complaintType.setText("Type: " + myComplaints.getType());
        completionStatus.setText("Status: " + myComplaints.getCompletionStatus());

        return listViewItem;
    }

}