package com.lobxy.achs.User;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lobxy.achs.R;

public class CustomListview extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] labels;
    private final Integer[] images;

    public CustomListview(Activity context, String[] labels, Integer[] images) {
        super(context, R.layout.complain_listitem, labels);
        this.context = context;
        this.images = images;
        this.labels = labels;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.complain_listitem, null, true);
        }

        TextView textView = view.findViewById(R.id.complainName);
        ImageView imageView = view.findViewById(R.id.complainImage);

        textView.setText(labels[position]);
        imageView.setImageResource(images[position]);

        return view;
    }

}
