package com.example.chahn.spotifystreamer.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Spotify artist model. Simplified model from the standard Spotify Android API
 */
public class SpotifyArtist implements Serializable {

  public static final String LOOKUP_TAG = "spotify_artist";

  private final String id;
  private final String artistName;
  private final Optional<String> smallIconUrl;

  public SpotifyArtist(String id, String artistName, Optional<String> smallIconUrl) {
    this.id = id;
    this.artistName = artistName;
    this.smallIconUrl = smallIconUrl;
  }

  public String getId() {
    return id;
  }

  public String getArtistName() {
    return artistName;
  }

  public Optional<String> getSmallIconUrl() {
    return smallIconUrl;
  }

  @Override public String toString() {
    return "SpotifyArtist{" +
        "id='" + id + '\'' +
        ", artistName='" + artistName + '\'' +
        ", smallIconUrl=" + smallIconUrl +
        '}';
  }

  /**
   * Transformer to convert Spotify Android API model to simplified model used by our app
   */
  public static class ArtistTransformer extends AbstractTransformer {

    /**
     * Function to convert 'kaaes.spotify.webapi.android.models.Artist' -> SpotifyArtist
     */
    private final Function<Artist, SpotifyArtist> SPOT_TO_ARTIST_FUNCTION =
        new Function<Artist, SpotifyArtist>() {
          @Override public SpotifyArtist apply(Artist input) {
            Optional<String> smallUrl = findSmallImageUrl(input.images);
            return new SpotifyArtist(input.id, input.name, smallUrl);
          }
        };

    public List<SpotifyArtist> transform(List<Artist> artists) {
      return ImmutableList.copyOf(Collections2.transform(artists, SPOT_TO_ARTIST_FUNCTION));
    }
  }
}
