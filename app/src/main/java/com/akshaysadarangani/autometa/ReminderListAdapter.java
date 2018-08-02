package com.akshaysadarangani.autometa;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.MyViewHolder> {
    private Context context;
    private List<Reminder> cartList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView username, description, phone, email, distance, unit;    // location
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            username = view.findViewById(R.id.username);
            description = view.findViewById(R.id.description);
            phone = view.findViewById(R.id.phone);
            email = view.findViewById(R.id.email);
            distance = view.findViewById(R.id.distance);
        }
    }


    public ReminderListAdapter(Context context, List<Reminder> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trigger_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Reminder reminder = cartList.get(position);
        holder.username.setText("By You");// + reminder.getUserName());
        holder.description.setText(reminder.getDescription());
        if(reminder.getPhone() != null)
            holder.phone.setText(reminder.getPhone());
        if(reminder.getEmail() != null)
         holder.email.setText(reminder.getEmail());
        holder.distance.setText(reminder.getDistance() + " " + reminder.getUnit());

        /*Glide.with(context)
                .load(recipe.getThumbnail())
                .into(holder.thumbnail);*/
    }
    // recipe
    @Override
    public int getItemCount() {
        return cartList.size();
    }
}