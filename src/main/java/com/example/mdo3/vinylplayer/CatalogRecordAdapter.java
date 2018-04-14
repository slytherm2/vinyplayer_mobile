package com.example.mdo3.vinylplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        if(!imageFilePath.equalsIgnoreCase("null"))
        {
            System.out.println("DEBUG : IMage isn't NULL");
            System.out.println("DEBUG: " + imageFilePath);
            ImageView image = (ImageView) listItem.findViewById(R.id.album_pic);
            /*Bitmap bitmap = Utils.LoadImageFromGallery(mContext, record.getFilePath());
            if(bitmap != null)
            {
                image.setImageBitmap(bitmap);
            }*/
        }
        else
        {
            System.out.println("DEBUG : IMage was NULL....using default picture");
            ImageView image = (ImageView) listItem.findViewById(R.id.album_pic);
            image.setImageResource(R.drawable.ic_menu_gallery);
        }

        TextView albumName = (TextView) listItem.findViewById(R.id.album_name);
        albumName.setText(record.getAlbum());

        TextView artistName = (TextView) listItem.findViewById(R.id.artist_name);
        artistName.setText(record.getArtist());

        return listItem;
    }
}
