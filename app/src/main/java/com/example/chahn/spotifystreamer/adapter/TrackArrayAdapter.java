package com.example.chahn.spotifystreamer.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.chahn.spotifystreamer.R;
import com.example.chahn.spotifystreamer.model.SpotifyTrack;
import com.squareup.picasso.Picasso;

/**
 * Adapter for rendering Spotify track info
 */
public class TrackArrayAdapter extends ArrayAdapter<SpotifyTrack> {

  private Context context;
  private int rowResourceId;

  public TrackArrayAdapter(Context context, int resource) {
    super(context, resource);
    this.context = context;
    this.rowResourceId = resource;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      convertView = inflater.inflate(rowResourceId, parent, false);

      ImageView detailIcon = (ImageView) convertView.findViewById(R.id.detail_row_image);
      TextView albumName = (TextView) convertView.findViewById(R.id.detail_row_album_text);
      TextView trackName = (TextView) convertView.findViewById(R.id.detail_row_track_text);

      TrackViewHolder tempViewHolder = new TrackViewHolder(detailIcon, albumName, trackName);
      convertView.setTag(tempViewHolder);
    }

    TrackViewHolder artistViewHolder = (TrackViewHolder) convertView.getTag();
    artistViewHolder.populate(context, getItem(position));

    return convertView;
  }

  //Note: ViewHolder code referenced from https://commonsware.com/ as a basis
  /**
   * View holder for track info
   */
  private static class TrackViewHolder {
    private ImageView trackIcon;
    private TextView albumName;
    private TextView trackName;

    public TrackViewHolder(ImageView trackIcon, TextView albumName, TextView trackName) {
      this.trackIcon = trackIcon;
      this.albumName = albumName;
      this.trackName = trackName;
    }

    public void populate(Context context, SpotifyTrack spotifyTrack) {
      //Render the proper track icon or a default one if no images are found
      if (spotifyTrack.getSmallTrackIconUrl().isPresent()) {
        Picasso.with(context).load(spotifyTrack.getSmallTrackIconUrl().get()).into(trackIcon);
      } else {
        Picasso.with(context).load(R.drawable.default_icon_small).into(trackIcon);
      }

      albumName.setText(spotifyTrack.getAlbumName());
      trackName.setText(spotifyTrack.getTrackName());
    }
  }

}
