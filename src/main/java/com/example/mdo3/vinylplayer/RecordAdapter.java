package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.icu.text.AlphabeticIndex;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jose.medina on 3/24/2018.
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>
{
    public LayoutInflater inflater;
    public ArrayList<Record> records;

    public RecordAdapter(Context context, ArrayList<Record> records)
    {
        this.inflater = LayoutInflater.from(context);
        this.records = records;
    }

    // create new views (invoked in the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
    {
        View view = inflater.inflate(R.layout.recycler_row_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Record record = records.get(position);
        holder.artist_TextView.setText(record.getArtist());
        holder.album_TextView.setText((record.getAlbum()));
    }

    @Override
    public int getItemCount() { return records.size(); }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView artist_TextView;
        TextView album_TextView;
        ViewHolder(View view) {
            super(view);
            artist_TextView = view.findViewById(R.id.recycler_record_artist);
            album_TextView = view.findViewById(R.id.recycler_record_album);
        }

    }

}
