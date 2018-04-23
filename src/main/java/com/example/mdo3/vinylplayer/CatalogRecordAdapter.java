package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mdo3.vinylplayer.asyncTask.DownloadImageTask;
import com.example.mdo3.vinylplayer.asyncTask.ImageFromGalleryTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mdo3 on 4/11/2018.
 * This is for the MainScreen.xml listview
 * This is to display the number of albums that exist in the local and remote catalog
 * Displays the album picture, album name, artist name
 */

public class CatalogRecordAdapter extends ArrayAdapter<Record>
{
    private Context mContext;
    private List<Record> recordList;

    private final int DEFAULTSIZEWIDTH = 150;
    private final int DEFAULTSIZEHEIGHT = 150;

    public CatalogRecordAdapter(@NonNull Context context, ArrayList<Record> list)
    {
        super(context, 0 , list);
        this.mContext = context;
        if(list != null)
            this.recordList = list;
        else
            this.recordList = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.mainscreen_listview,
                    parent,
                    false);

        Record record = recordList.get(position);
        String imageFilePath = record.getFilePath();
        Boolean foundPath = false;

        ImageView image = (ImageView) listItem.findViewById(R.id.album_pic);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(DEFAULTSIZEWIDTH,
                DEFAULTSIZEHEIGHT);
        image.setLayoutParams(lParams);
        AsyncTaskFactory factory = new AsyncTaskFactory();

        //If filepath != null
        if(imageFilePath != null && !imageFilePath.equalsIgnoreCase("null") && !imageFilePath.isEmpty())
        {
            //System.out.println("DEBUG : FilePath isn't NULL");
            ImageFromGalleryTask ifgt = (ImageFromGalleryTask) factory.generateAsyncTask("Image", mContext);
            String[] params = {imageFilePath};
            try
            {
                image.setImageBitmap((Bitmap) ifgt.execute(params).get());
                foundPath = true;
            }
            catch(Exception e)
            {
                Log.d("Exception", e.getMessage());
            }
        }
        //if url != null
        imageFilePath = record.getUrl();
        if(imageFilePath != null && !imageFilePath.equalsIgnoreCase("null") && !foundPath && !imageFilePath.isEmpty())
        {
            //System.out.println("DEBUG : URL isn't NULL");
           DownloadImageTask dit = (DownloadImageTask) factory.generateAsyncTask("Download");
            String[] params = {imageFilePath};
            try
            {
                image.setImageBitmap((Bitmap) dit.execute(params).get());
                foundPath = true;
            }
            catch(Exception e)
            {
                Log.d("Exception", e.getMessage());
            }
        }

        if(!foundPath)
        {
            //System.out.println("DEBUG : IMage was NULL....using default picture");
            image = (ImageView) listItem.findViewById(R.id.album_pic);
            image.setImageResource(R.drawable.warp_150);
        }

        TextView albumName = (TextView) listItem.findViewById(R.id.album_name);
        albumName.setText(record.getAlbum());

        TextView artistName = (TextView) listItem.findViewById(R.id.artist_name);
        artistName.setText(record.getArtist());

        return listItem;
    }
}
