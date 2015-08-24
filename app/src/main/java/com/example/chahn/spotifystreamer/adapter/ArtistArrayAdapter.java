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
import com.example.chahn.spotifystreamer.model.SpotifyArtist;
import com.squareup.picasso.Picasso;

/**
 * Adapter for rendering Spotify artist info
 */
public class ArtistArrayAdapter extends ArrayAdapter<SpotifyArtist> {

  private Context context;
  private int rowResourceId;

  public ArtistArrayAdapter(Context context, int resource) {
    super(context, resource);
    this.context = context;
    this.rowResourceId = resource;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      convertView = inflater.inflate(rowResourceId, parent, false);

      ImageView artistImageView = (ImageView) convertView.findViewById(R.id.result_row_image);
      TextView artistTextView = (TextView) convertView.findViewById(R.id.result_row_text);

      ArtistViewHolder tempViewHolder = new ArtistViewHolder(artistImageView, artistTextView);
      convertView.setTag(tempViewHolder);
    }

    ArtistViewHolder artistViewHolder = (ArtistViewHolder) convertView.getTag();
    artistViewHolder.populate(context, getItem(position));

    return convertView;
  }

  //Note: ViewHolder code referenced from https://commonsware.com/ as a basis
  /**
   * View holder for artist info
   */
  private static class ArtistViewHolder {
    private ImageView artistIcon;
    private TextView artistName;

    public ArtistViewHolder(ImageView artistIcon, TextView artistName) {
      this.artistIcon = artistIcon;
      this.artistName = artistName;
    }

    public void populate(Context context, SpotifyArtist spotifyArtist) {
      //Render the proper artist icon or a default one if no images are found
      if (spotifyArtist.getSmallIconUrl().isPresent()) {
        Picasso.with(context).load(spotifyArtist.getSmallIconUrl().get()).into(artistIcon);
      } else {
        Picasso.with(context).load(R.drawable.default_icon_small).into(artistIcon);
      }

      artistName.setText(spotifyArtist.getArtistName());
    }
  }

}
