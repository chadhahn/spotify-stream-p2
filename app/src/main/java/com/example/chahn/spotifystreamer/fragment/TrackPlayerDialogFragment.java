package com.example.chahn.spotifystreamer.fragment;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.chahn.spotifystreamer.R;
import com.example.chahn.spotifystreamer.model.SpotifyArtist;
import com.example.chahn.spotifystreamer.model.SpotifyTrack;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * TODO: this is a bit busted on rotation. Need to save/restore state vs handle orientation
 * manually. Also play track where it leaves off and not from the start again
 */
public class TrackPlayerDialogFragment extends DialogFragment
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private Drawable playImg;
    private Drawable pauseImg;

    private SpotifyArtist spotifyArtist;
    private ArrayList<SpotifyTrack> spotifyTracksList;
    private SpotifyTrack spotifyTrack;
    private int currentTrack;

    private TextView artistName;
    private TextView albumName;
    private ImageView trackIcon;
    private TextView trackName;

    private ImageButton playPauseButton;

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler durationHandler = new Handler();
    private double timeElapsed = 0;
    private TextView trackHighText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            spotifyArtist = (SpotifyArtist) arguments.getSerializable(SpotifyArtist.LOOKUP_TAG);
            spotifyTracksList = (ArrayList<SpotifyTrack>) arguments.getSerializable(SpotifyTrack.LOOKUP_TAG);
            currentTrack = arguments.getInt(SpotifyTrack.CURRENT_TRACK);
            spotifyTrack = spotifyTracksList.get(currentTrack);
        }

        View root = inflater.inflate(R.layout.track_player, container, false);

        artistName = (TextView) root.findViewById(R.id.track_player_artist);
        albumName = (TextView) root.findViewById(R.id.track_player_album);
        trackIcon = (ImageView) root.findViewById(R.id.track_player_image);
        trackName = (TextView) root.findViewById(R.id.track_player_track_name);

        playImg = ContextCompat.getDrawable(this.getActivity(), android.R.drawable.ic_media_play);
        pauseImg = ContextCompat.getDrawable(this.getActivity(), android.R.drawable.ic_media_pause);

        playPauseButton = (ImageButton) root.findViewById(R.id.track_player_play_button);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setImageDrawable(playImg);
                } else {
                    mediaPlayer.start();
                    playPauseButton.setImageDrawable(pauseImg);
                }
            }
        });

        final ImageButton prevButton = (ImageButton) root.findViewById(R.id.track_player_prev_button);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousTrackIfFound();
            }
        });

        final ImageButton nextButton = (ImageButton) root.findViewById(R.id.track_player_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextTrackIfFound();
            }
        });

        seekBar = (SeekBar) root.findViewById(R.id.track_player_seek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        trackHighText = (TextView) root.findViewById(R.id.track_player_high_text);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        playTrack();
    }

    //Note: borrowed this from http://examples.javacodegeeks.com/android/android-mediaplayer-example/
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            if (mediaPlayer != null) {
                timeElapsed = mediaPlayer.getCurrentPosition();
                seekBar.setProgress((int) timeElapsed);
                durationHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * When a track is ready to play, start the music and set up the other metadata.
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(this);
        playPauseButton.setImageDrawable(pauseImg);
        seekBar.setMax(mediaPlayer.getDuration());
        durationHandler.postDelayed(updateSeekBarTime, 100);

        //todo: this is pretty weak. Is the preview ever over 30seconds? Fix if minutes needed
        trackHighText.setText(String.format("0:%02d", mediaPlayer.getDuration() / 1000));
    }

    /**
     * Once a song stops, play the next one if found for a generic autoplay
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!playNextTrackIfFound()) {
            playPauseButton.setImageDrawable(playImg);
        }
    }

    /**
     * Play the previous track if found
     */
    private void playPreviousTrackIfFound() {
        if (currentTrack > 0) {
            currentTrack--;
            playTrack();
        }
    }

    /**
     * Play the next track and return true if found else false
     */
    private boolean playNextTrackIfFound() {
        if (currentTrack < spotifyTracksList.size() - 1) {
            currentTrack++;
            playTrack();
            return true;
        }
        return false;
    }

    /**
     * Play the current track. Sets the onCompleteListener to null, which allows a generic
     * autoplay (the next track will start playing). Without removing and resetting the listener,
     * it loops when stop() is called
     */
    private void playTrack() {
        spotifyTrack = spotifyTracksList.get(currentTrack);
        mediaPlayer.setOnCompletionListener(null);
        mediaPlayer.reset();
        mediaPlayer.stop();
        setDataSource();
        setTrackMetaData();
        mediaPlayer.prepareAsync();
    }

    /**
     * Set the track preview url. This will fire the onPrepared callback when finished
     */
    private void setDataSource() {
        try {
            mediaPlayer.setDataSource(spotifyTrack.getPreviewUrl());
        } catch (IOException ignore) {
        }
    }

    /**
     * Set the metadata (image, text, etc.) for the track being played
     */
    private void setTrackMetaData() {
        artistName.setText(spotifyArtist.getArtistName());
        albumName.setText(spotifyTrack.getAlbumName());

        if (spotifyTrack.getLargeTrackIconUrl().isPresent()) {
            Picasso.with(getActivity()).load(spotifyTrack.getLargeTrackIconUrl().get()).into(trackIcon);
        } else {
            Picasso.with(getActivity()).load(R.drawable.default_icon_large).into(trackIcon);
        }

        trackName.setText(spotifyTrack.getTrackName());
    }

}
