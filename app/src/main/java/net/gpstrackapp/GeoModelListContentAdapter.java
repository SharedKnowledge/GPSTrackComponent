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
import net.gpstrackapp.geomodel.GeoModelManager;
import net.gpstrackapp.geomodel.track.TrackManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeoModelListContentAdapter extends
        RecyclerView.Adapter<GeoModelListContentAdapter.MyViewHolder>
        implements View.OnClickListener {

    private final Context ctx;
    protected final SelectableGeoModelListContentAdapterHelper helper;
    private View.OnClickListener clickListener;
    private boolean firstClick = true;
    private RequestGeoModelsCommand requestGeoModelsCommand;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView GeoModelName,
                GeoModelDate,
                GeoModelCreator,
                GeoModelSelected;

        public MyViewHolder(View view) {
            super(view);
            GeoModelName = view.findViewById(R.id.gpstracker_list_geomodels_row_name);
            GeoModelDate = view.findViewById(R.id.gpstracker_list_geomodels_row_date);
            GeoModelCreator = view.findViewById(R.id.gpstracker_list_geomodels_row_creator);
            GeoModelSelected = view.findViewById(R.id.gpstracker_list_geomodels_row_selected);

            view.setOnClickListener(clickListener);
        }
    }

    public GeoModelListContentAdapter(Context ctx, SelectableGeoModelListContentAdapterHelper helper, RequestGeoModelsCommand requestGeoModelsCommand) {
        Log.d(this.getLogStart(), "constructor");
        this.ctx = ctx;
        this.clickListener = this;
        this.helper = helper;
        this.requestGeoModelsCommand = requestGeoModelsCommand;
    }

    @NonNull
    @Override
    public GeoModelListContentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(this.getLogStart(), "onCreateViewHolder");
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.gpstracker_list_geomodel_row, parent, false);
        return new GeoModelListContentAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GeoModelListContentAdapter.MyViewHolder holder, int position) {
        Log.d(this.getLogStart(), "onBindViewHolder with position: " + position);
        GeoModel geoModel = requestGeoModelsCommand.getGeoModels().get(position);

        CharSequence geoModelID = geoModel.getObjectId();
        helper.setSelectedText(Integer.toString(position), geoModelID,
                holder.itemView, holder.GeoModelSelected);

        CharSequence geoModelName = geoModel.getObjectName();
        String date = geoModel.getDateOfCreationAsFormattedString();
        CharSequence creator = geoModel.getCreator();

        holder.itemView.setTag(R.id.geomodel_id_tag, geoModelID);
        holder.GeoModelName.setText(geoModelName);
        holder.GeoModelDate.setText(date);
        holder.GeoModelCreator.setText(creator);
    }

    //TODO
    @Override
    public int getItemCount() {
        return requestGeoModelsCommand.getNumberOfGeoModels();
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
