package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by jose.medina on 3/24/2018.
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>
{
    public LayoutInflater inflater;
    public ArrayList<Record> records;
    public Context context;
    // private final OnClickListener listener;


    // interface for onClickListener
    private OnItemClicked onClick;
    public interface OnItemClicked { void onItemClick(int position); }

    public RecordAdapter(Context context, ArrayList<Record> records)
    {
        this.inflater = LayoutInflater.from(context);
        this.records = records;
        this.context = context;
    }

    // create new views (invoked in the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
    {
        View view = inflater.inflate(R.layout.recycler_row_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        final Record record = records.get(position);
        holder.artist_TextView.setText(record.getArtist());
        holder.album_TextView.setText((record.getAlbum()));
        holder.artist_TextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, RecordInfo.class);
                intent.putExtra("artist", record.getArtist());
                intent.putExtra("album", record.getAlbum());
                context.startActivity(intent);
            }
        });
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

        public void bind(final Record record, final AdapterView.OnItemClickListener listener)
        {
        }
    }

}
