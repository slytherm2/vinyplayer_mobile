package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jr on 3/29/2018.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>
{
    public LayoutInflater inflater;
    public ArrayList<Song> tracklist;
    public Context context;

    public SongAdapter(Context context, ArrayList<Song> tracklist)
    {
        this.inflater = LayoutInflater.from(context);
        this.tracklist = tracklist;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_row_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        final Song song = tracklist.get(position);
        holder.title_TextView.setText(song.getTitle());
        holder.artist_TextView.setText(song.getPosition());
        holder.duration_TextView.setText(song.getDuration());

        //removing because we want to pass more than song info, we want album, artist, and song list
        /*
        holder.song_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MusicPlayer.class);
                intent.putExtra("song", song);
                context.startActivity(intent);
            }
        });
        */
    }

    @Override
    public int getItemCount()
    {
        if(tracklist != null)
            return tracklist.size();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView artist_TextView;
        TextView title_TextView;
        TextView duration_TextView;
        LinearLayout song_Layout;

        ViewHolder(View view)
        {
            super(view);
            this.artist_TextView = view.findViewById(R.id.recycler_song_artist);
            this.title_TextView = view.findViewById(R.id.recycler_song_title);
            this.duration_TextView = view.findViewById(R.id.recycler_song_duration);
            this.song_Layout = (LinearLayout) view.findViewById(R.id.recycler_song);
        }

    }
}
