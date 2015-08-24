package com.example.chahn.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.chahn.spotifystreamer.R;
import com.example.chahn.spotifystreamer.fragment.DetailFragment;
import com.example.chahn.spotifystreamer.model.SpotifyArtist;

/**
 * Detail activity for displaying top tracks from selected album
 */
public class DetailActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.artist_detail);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(SpotifyArtist.LOOKUP_TAG)) {
      SpotifyArtist spotifyArtist = (SpotifyArtist) intent.getSerializableExtra(SpotifyArtist.LOOKUP_TAG);

      Bundle args = new Bundle();
      args.putSerializable(SpotifyArtist.LOOKUP_TAG, spotifyArtist);

      DetailFragment detailFragment = new DetailFragment();
      detailFragment.setArguments(args);

      getSupportFragmentManager().beginTransaction()
              .replace(R.id.search_detail_container, detailFragment)
              .commit();
    }
  }

}
