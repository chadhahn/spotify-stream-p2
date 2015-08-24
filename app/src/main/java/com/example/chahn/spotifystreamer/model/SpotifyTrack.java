package com.example.chahn.spotifystreamer.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Spotify tracks model. Simplified model from the standard Spotify Android API
 */
public class SpotifyTrack implements Serializable {

  public static final String LOOKUP_TAG = "spotify_track";
  public static final String CURRENT_TRACK = "spotify_current_track";

  private final String albumName;
  private final String trackName;
  private final Optional<String> smallTrackIconUrl;
  private final Optional<String> largeTrackIconUrl;
  private final String previewUrl;

  public SpotifyTrack(String albumName, String trackName, Optional<String> smallTrackIconUrl,
      Optional<String> largeTrackIconUrl, String previewUrl) {
    this.albumName = albumName;
    this.trackName = trackName;
    this.smallTrackIconUrl = smallTrackIconUrl;
    this.largeTrackIconUrl = largeTrackIconUrl;
    this.previewUrl = previewUrl;
  }

  public String getAlbumName() {
    return albumName;
  }

  public String getTrackName() {
    return trackName;
  }

  public Optional<String> getSmallTrackIconUrl() {
    return smallTrackIconUrl;
  }

  public Optional<String> getLargeTrackIconUrl() {
    return largeTrackIconUrl;
  }

  public String getPreviewUrl() {
    return previewUrl;
  }

  @Override public String toString() {
    return "SpotifyTrack{" +
        "albumName='" + albumName + '\'' +
        ", trackName='" + trackName + '\'' +
        ", smallTrackIconUrl=" + smallTrackIconUrl +
        ", largeTrackIconUrl=" + largeTrackIconUrl +
        ", previewUrl='" + previewUrl + '\'' +
        '}';
  }

  /**
   * Transformer to convert Spotify Android API model to simplified model used by our app
   */
  public static class TrackTransformer extends AbstractTransformer {

    /**
     * Function to convert 'kaaes.spotify.webapi.android.models.Track' -> SpotifyTrack
     */
    private final Function<Track, SpotifyTrack> SPOT_TO_TRACK_FUNCTION =
        new Function<Track, SpotifyTrack>() {
          @Override public SpotifyTrack apply(Track input) {
            Optional<String> sml = findSmallImageUrl(input.album.images);
            Optional<String> lrg = findLargeImageUrl(input.album.images);
            return new SpotifyTrack(input.album.name, input.name, sml, lrg, input.preview_url);
          }
        };

    public List<SpotifyTrack> transform(List<Track> tracks) {
      return ImmutableList.copyOf(Collections2.transform(tracks, SPOT_TO_TRACK_FUNCTION));
    }
  }

}
