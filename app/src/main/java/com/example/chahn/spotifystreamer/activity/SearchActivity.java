package com.example.chahn.spotifystreamer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.chahn.spotifystreamer.R;
import com.example.chahn.spotifystreamer.adapter.ArtistArrayAdapter;
import com.example.chahn.spotifystreamer.fragment.DetailFragment;
import com.example.chahn.spotifystreamer.model.SpotifyArtist;
import java.util.Collections;
import java.util.List;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Main activity for searching Spotify artists
 */
public class SearchActivity extends AppCompatActivity {

  /**
   * Minimum number of chars before searching Spotify
   */
  private static final int MIN_SEARCH_CHAR = 2;

  private EditText searchEditText;
  private ListView searchListView;
  private FrameLayout progressOverlayView;
  private ArtistArrayAdapter searchResultArrayAdapter;
  private TextView noResultsText;

  private boolean twoPaneView = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    if (findViewById(R.id.search_detail_container) != null) {

      twoPaneView = true;

      if (savedInstanceState == null) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.search_detail_container, new DetailFragment())
                .commit();
      }
    }

    searchEditText = (EditText) findViewById(R.id.edit_text_search);
    progressOverlayView = (FrameLayout) findViewById(R.id.progress_overlay);
    noResultsText = (TextView) findViewById(R.id.no_results_text);

    searchResultArrayAdapter = new ArtistArrayAdapter(this, R.layout.search_result_row);

    searchListView = (ListView) findViewById(R.id.list_view_search);
    searchListView.setAdapter(searchResultArrayAdapter);
    searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      //Start the detail activity passing in extracted spotify artist info
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SpotifyArtist spotifyArtist = searchResultArrayAdapter.getItem(position);

        if (twoPaneView) {
          Bundle args = new Bundle();
          args.putSerializable(SpotifyArtist.LOOKUP_TAG, spotifyArtist);

          DetailFragment detailFragment = new DetailFragment();
          detailFragment.setArguments(args);

          getSupportFragmentManager().beginTransaction()
                  .replace(R.id.search_detail_container, detailFragment)
                  .commit();
        } else {
          Intent detailIntent =
                  new Intent(parent.getContext(), DetailActivity.class).putExtra(SpotifyArtist.LOOKUP_TAG,
                          spotifyArtist);
          startActivity(detailIntent);
        }
      }
    });

    searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (hasDataAndHitEnter(v, actionId, event)) {
          new SpotifySearchTask().execute(searchEditText.getText().toString());
        }
        return true;
      }
    });
  }

  private boolean hasDataAndHitEnter(TextView v, int actionId, KeyEvent event) {
    return v.getText().length() > MIN_SEARCH_CHAR && (actionId == EditorInfo.IME_ACTION_DONE
        || event != null
        && event.getAction() == KeyEvent.ACTION_UP
        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
  }

  /**
   * Task to load Spotify artist info
   */
  public class SpotifySearchTask extends AsyncTask<String, Void, List<SpotifyArtist>> {

    //Holder
    private Exception taskException = null;

    @Override protected void onPreExecute() {
      super.onPreExecute();

      //http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard for hiding keyboard
      InputMethodManager inputMethodManager =
          (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

      searchResultArrayAdapter.clear();
      searchResultArrayAdapter.notifyDataSetChanged();

      //Before execute, disable the search text, hide no results text, and bring up progress bar
      searchEditText.setEnabled(false);
      noResultsText.setVisibility(View.GONE);
      progressOverlayView.setVisibility(View.VISIBLE);
    }

    /**
     * Query Spotify and capture any exception for processing by the UI-thread
     */
    @Override protected List<SpotifyArtist> doInBackground(String... params) {
      try {
        SpotifyApi api = new SpotifyApi();
        ArtistsPager artistsPager = api.getService().searchArtists(params[0]);
        return new SpotifyArtist.ArtistTransformer().transform(artistsPager.artists.items);
      } catch (Exception e) {
        taskException = e;
      }
      return Collections.emptyList();
    }

    @Override protected void onPostExecute(List<SpotifyArtist> artists) {
      super.onPostExecute(artists);

      //After execute, hide the progress bar and enable the search text
      progressOverlayView.setVisibility(View.GONE);
      searchEditText.setEnabled(true);

      if (taskException != null) {
        Toast.makeText(getApplicationContext(), getString(R.string.loading_error_msg),
            Toast.LENGTH_SHORT).show();
      }

      //Show results if found or show nothing found text
      if (artists.isEmpty()) {
        noResultsText.setVisibility(View.VISIBLE);
      } else {
        searchResultArrayAdapter.addAll(artists);
        searchResultArrayAdapter.notifyDataSetChanged();
      }
    }
  }

}
