package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mdo3.vinylplayer.asyncTask.DownloadImageTask;
import com.example.mdo3.vinylplayer.asyncTask.GetTrackListTask;
import com.example.mdo3.vinylplayer.asyncTask.SearchTask;

import org.json.JSONArray;

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
        holder.record_Layout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                AsyncTaskFactory factory = new AsyncTaskFactory();
                GetTrackListTask task = (GetTrackListTask) factory.generateAsyncTask("GetTracklist");
                try
                {
                    String[] params = {record.getId(), context.getResources().getString(R.string.https_url_getTracklist)};
                    String tracklist_String = task.execute(params).get();
                    ArrayList<Song> tracklist = new ArrayList<Song>();
                    JSONArray tracklist_JSON = new JSONArray(tracklist_String);
                    for(int i = 0; i < tracklist_JSON.length(); i++)
                    {
                        // Duration duration = null;
                        String title = tracklist_JSON.getJSONObject(i).getString("title");
                        String duration = tracklist_JSON.getJSONObject(i).getString("duration");
                        String position = tracklist_JSON.getJSONObject(i).getString("position");
                        Song song = new Song(title, position, duration);
                        tracklist.add(song);
                    }
                    record.setTrackList(tracklist);
                }
                catch (Exception e)
                {
                    Log.d("Exception", e.getMessage());
                }
                Intent intent = new Intent(context, RecordInfo.class);
                intent.putExtra("record", record);
                context.startActivity(intent);
            }
        });


        if(record.getUrl() != null || !record.getUrl().isEmpty())
        {
            AsyncTaskFactory factory = new AsyncTaskFactory();
            DownloadImageTask downloadTask = (DownloadImageTask) factory.generateAsyncTask("Download");
            try
            {
                holder.cover_ImageView.setImageBitmap(downloadTask.execute(record.getUrl()).get());
            }
            catch (Exception e)
            {
                Log.d("Exception", e.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() { return records.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView artist_TextView;
        TextView album_TextView;
        ImageView cover_ImageView;
        LinearLayout record_Layout;

        ViewHolder(View view)
        {
            super(view);
            artist_TextView = view.findViewById(R.id.recycler_record_artist);
            album_TextView = view.findViewById(R.id.recycler_record_album);
            cover_ImageView = view.findViewById(R.id.recycler_record_cover);
            record_Layout = (LinearLayout) view.findViewById(R.id.recycler_record);
        }
    }
}
