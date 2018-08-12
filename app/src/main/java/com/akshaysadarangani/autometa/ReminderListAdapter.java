package com.akshaysadarangani.autometa;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;


public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.MyViewHolder> {
    private Context context;
    private View v;
    private List<Reminder> triggerList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView username, description, phone_email, distance, unit, location;
        public ImageView thumbnail, delete;

        public MyViewHolder(View view) {
            super(view);
            v = view;
            delete = view.findViewById(R.id.delete);
            username = view.findViewById(R.id.username);
            description = view.findViewById(R.id.description);
            phone_email = view.findViewById(R.id.phone_email);
            distance = view.findViewById(R.id.distance);
            location = view.findViewById(R.id.location);
        }
    }


    public ReminderListAdapter(Context context, List<Reminder> triggerList) {
        this.context = context;
        this.triggerList = triggerList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trigger_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Reminder reminder = triggerList.get(position);
        holder.username.setText("By " + reminder.getUserName());// + reminder.getUserName());
        holder.description.setText(reminder.getDescription());
        if(reminder.getPhone() != null)
            holder.phone_email.setText(reminder.getPhone());
        if(reminder.getEmail() != null)
         holder.phone_email.setText(reminder.getEmail());

        holder.distance.setText(reminder.getDistance() + " " + reminder.getUnit());
        holder.location.setText("Location: " + reminder.getPlaceName());
        if(reminder.isCompleted())
            v.setBackgroundColor(Color.parseColor("#88eb8d"));

        /*Glide.with(context)
                .load(recipe.getThumbnail())
                .into(holder.thumbnail);*/

        // Delete task
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder reminder = triggerList.get(position);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("tasks");
                String rid = reminder.getRid();
                myRef.child(rid).removeValue();
                triggerList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, triggerList.size());
            }
        });
    }
    // recipe
    @Override
    public int getItemCount() {
        return triggerList.size();
    }
}