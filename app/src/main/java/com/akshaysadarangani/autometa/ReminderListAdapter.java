package com.akshaysadarangani.autometa;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.MyViewHolder> implements OnCompleteListener<Void> {
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
        else
            v.setBackgroundColor(Color.WHITE);

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
                String key;
                switch(reminder.getType()) {
                    case "REMINDER":
                        key = rid+"&&"+reminder.getDescription()+"&&"+reminder.getPlaceName()+"&&null";
                        break;
                    case "SMS":
                        key = rid+"&&"+reminder.getDescription()+"&&"+reminder.getPlaceName()+"&&"+reminder.getPhone();
                        break;
                    case "EMAIL":
                        key = rid+"&&"+reminder.getDescription()+"&&"+reminder.getPlaceName()+"&&"+reminder.getEmail();
                        break;
                    default: key = "";
                }
                removeGeofence(key);
                notifyDataSetChanged();
            }
        });
    }

    private void removeGeofence(String requestID) {
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        triggeringGeofencesIdsList.add(requestID);
        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(context);
        mGeofencingClient.removeGeofences(triggeringGeofencesIdsList).addOnCompleteListener(this);
        SharedPreferences.Editor editor = context.getSharedPreferences("GEOFENCES_DB", MODE_PRIVATE).edit();
        editor.remove(requestID);
        editor.apply();
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Log.e("SERVICE", "DELETED GEOFENCE");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(context, task.getException());
            Log.w("ReminderList", errorMessage);
        }
    }

    @Override
    public int getItemCount() {
        return triggerList.size();
    }
}