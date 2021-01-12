package net.gpstrackapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.track.TrackManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MapObjectListContentAdapter extends
        RecyclerView.Adapter<MapObjectListContentAdapter.MyViewHolder>
        implements View.OnClickListener {

    private final Context ctx;
    protected final SelectableMapObjectListContentAdapterHelper helper;
    private View.OnClickListener clickListener;
    private boolean firstClick = true;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mapObjectName,
                mapObjectDate,
                mapObjectCreator,
                mapObjectSelected;

        public MyViewHolder(View view) {
            super(view);
            mapObjectName = view.findViewById(R.id.gpstracker_list_geomodels_row_name);
            mapObjectDate = view.findViewById(R.id.gpstracker_list_geomodels_row_date);
            mapObjectCreator = view.findViewById(R.id.gpstracker_list_geomodels_row_creator);
            mapObjectSelected = view.findViewById(R.id.gpstracker_list_geomodels_row_selected);

            view.setOnClickListener(clickListener);
        }
    }

    //TODO zusaetzlich GeoModelManager uebergeben
    public MapObjectListContentAdapter(Context ctx, SelectableMapObjectListContentAdapterHelper helper) {
        Log.d(this.getLogStart(), "constructor");
        this.ctx = ctx;
        this.clickListener = this;
        this.helper = helper;
    }

    @NonNull
    @Override
    public MapObjectListContentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(this.getLogStart(), "onCreateViewHolder");
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.gpstracker_list_geomodel_row, parent, false);
        return new MapObjectListContentAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MapObjectListContentAdapter.MyViewHolder holder, int position) {
        Log.d(this.getLogStart(), "onBindViewHolder with position: " + position);

        //TODO im Konstruktor zu uebergebenden GeoModelManager stattdessen nutzen
        GeoModel geoModel = TrackManager.getTrackByPosition(position);

        CharSequence geoModelID = geoModel.getObjectId();
        helper.setSelectedText(Integer.toString(position), geoModelID,
                holder.itemView, holder.mapObjectSelected);

        CharSequence geoModelName = geoModel.getObjectName();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(geoModel.getDateOfCreation());
        CharSequence creator = geoModel.getCreator();

        holder.itemView.setTag(R.id.geomodel_id_tag, geoModelID);
        holder.mapObjectName.setText(geoModelName);
        holder.mapObjectDate.setText(date);
        holder.mapObjectCreator.setText(creator);
    }

    //TODO
    @Override
    public int getItemCount() {
        return TrackManager.getNumberOfTracks();
    }

    //TODO onLongClick to edit GeoModel?

    @Override
    public void onClick(View view) {
        if (firstClick) {
            firstClick = false;
        }
        CharSequence geoModelID = (CharSequence) view.getTag(R.id.geomodel_id_tag);
        helper.onAction(this, view, geoModelID);
    }

    protected String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
