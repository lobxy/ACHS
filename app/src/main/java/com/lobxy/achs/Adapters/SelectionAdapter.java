package com.lobxy.achs.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lobxy.achs.R;
import com.lobxy.achs.Model.Supervisor;

import java.util.List;

public class SelectionAdapter extends ArrayAdapter<Supervisor> {

    private List<Supervisor> list;
    Context mContext;

    public SelectionAdapter(List<Supervisor> list, Context context) {
        super(context, R.layout.selection_list_item, list);
        this.list = list;
        this.mContext = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.selection_list_item, parent, false);
        }

        Supervisor supervisor = getItem(position);

        TextView countryView = view.findViewById(R.id.select_name);
        countryView.setText(supervisor.getName());

        TextView isdView = view.findViewById(R.id.select_complaintNumbers);
        isdView.setText(supervisor.getSite());

        return view;
    }
}
