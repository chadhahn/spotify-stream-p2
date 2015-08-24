package com.example.chahn.spotifystreamer.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chahn.spotifystreamer.R;
import com.example.chahn.spotifystreamer.adapter.TrackArrayAdapter;
import com.example.chahn.spotifystreamer.model.SpotifyArtist;
import com.example.chahn.spotifystreamer.model.SpotifyTrack;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Tracks;

public class DetailFragment extends Fragment {

    /**
     * Hard-coded country code map required by Spotify API
     */
    public static final ImmutableMap<String, Object> COUNTRY_MAP =
            ImmutableMap.<String, Object>of("country", "US");

    private boolean isLargeLayout;

    private TrackArrayAdapter trackArrayAdapter;
    private ListView detailListView;
    private TextView noTracksText;

    private SpotifyArtist spotifyArtist;
    private ArrayList<SpotifyTrack> spotifyTrackList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        isLargeLayout = getResources().getBoolean(R.bool.large_layout);

        Bundle arguments = getArguments();
        if (arguments != null) {
            spotifyArtist = (SpotifyArtist) arguments.getSerializable(SpotifyArtist.LOOKUP_TAG);
            new SpotifyTrackTask().execute(spotifyArtist);
        }

        View root = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        trackArrayAdapter = new TrackArrayAdapter(this.getActivity(), R.layout.detail_row);
        noTracksText = (TextView) root.findViewById(R.id.no_track_results_text);
        detailListView = (ListView) root.findViewById(R.id.detail_list_view);
        detailListView.setAdapter(trackArrayAdapter);
        detailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                args.putSerializable(SpotifyArtist.LOOKUP_TAG, spotifyArtist);
                args.putSerializable(SpotifyTrack.LOOKUP_TAG, spotifyTrackList);
                args.putInt(SpotifyTrack.CURRENT_TRACK, position);


                TrackPlayerDialogFragment playerDialogFragment = new TrackPlayerDialogFragment();
                playerDialogFragment.setArguments(args);

                FragmentManager fragmentManager = getFragmentManager();

                if (isLargeLayout) {
                    playerDialogFragment.show(fragmentManager, "dialog");
                } else {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(android.R.id.content, playerDialogFragment)
                            .addToBackStack(null).commit();
                }
            }
        });

        return root;
    }

    /**
     * Task to load tracks for the given artist
     */
    public class SpotifyTrackTask extends AsyncTask<SpotifyArtist, Void, List<SpotifyTrack>> {

        //Holder
        private Exception taskException = null;

        @Override
        protected List<SpotifyTrack> doInBackground(SpotifyArtist... params) {
            try {
                SpotifyApi api = new SpotifyApi();
                Tracks tracks = api.getService().getArtistTopTrack(params[0].getId(), COUNTRY_MAP);
                return new SpotifyTrack.TrackTransformer().transform(tracks.tracks);
            } catch (Exception e) {
                taskException = e;
            }
            return Collections.emptyList();
        }

        @Override
        protected void onPostExecute(List<SpotifyTrack> spotifyTracks) {
            super.onPostExecute(spotifyTracks);

            if (taskException != null) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.loading_error_msg),
                        Toast.LENGTH_SHORT).show();
            }

            //Show results if found or show nothing found text
            if (spotifyTracks.isEmpty()) {
                noTracksText.setVisibility(View.VISIBLE);
            } else {
                spotifyTrackList = new ArrayList<>(spotifyTracks);
                trackArrayAdapter.addAll(spotifyTracks);
                trackArrayAdapter.notifyDataSetChanged();
            }
        }
    }

}
