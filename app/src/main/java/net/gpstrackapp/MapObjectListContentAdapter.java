package net.gpstrackapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MapObjectListContentAdapter extends
        RecyclerView.Adapter<MapObjectListContentAdapter.MyViewHolder>
        implements View.OnClickListener {

    private final Context ctx;
    private View.OnClickListener clickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mapObjectName,
                mapObjectDate,
                mapObjectCreator,
                mapObjectSelected;

        public MyViewHolder(View view) {
            super(view);
            mapObjectName = view.findViewById(R.id.gpstracker_list_geoobjects_row_name);
            mapObjectDate = view.findViewById(R.id.gpstracker_list_geoobjects_row_date);
            mapObjectCreator = view.findViewById(R.id.gpstracker_list_geoobjects_row_creator);
            mapObjectSelected = view.findViewById(R.id.gpstracker_list_geoobjects_row_selected);

            view.setOnClickListener(clickListener);
        }
    }

    public MapObjectListContentAdapter(Context ctx) {
        Log.d(this.getLogStart(), "constructor");
        this.ctx = ctx;
        this.clickListener = this;
    }

    @NonNull
    @Override
    public MapObjectListContentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(this.getLogStart(), "onCreateViewHolder");
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.gpstracker_list_geoobjects_row, parent, false);
        return new MapObjectListContentAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MapObjectListContentAdapter.MyViewHolder holder, int position) {
        Log.d(this.getLogStart(), "onBindViewHolder with position: " + position);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onClick(View v) {

    }

    protected String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
