package com.example.chahn.spotifystreamer.model;

import com.example.chahn.spotifystreamer.R;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.util.List;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Helper abstract transformer to help converting Spotify model classes into something more usable
 */
public abstract class AbstractTransformer {

  protected final int SMALL_ICON_WIDTH = R.integer.small_icon_width;
  protected final int SMALL_ICON_HEIGHT = R.integer.small_icon_height;
  protected final int LARGE_ICON_WIDTH = R.integer.large_icon_width;
  protected final int LARGE_ICON_HEIGHT = R.integer.large_icon_height;

  /**
   * Attempt to locate the proper small image from the given collection
   *
   * @param images List of the given Spotify images of various sizes
   * @return Optional String of the found (or not) small image url
   */
  protected Optional<String> findSmallImageUrl(List<Image> images) {
    return findClosestImageUrl(images, SMALL_ICON_WIDTH, SMALL_ICON_HEIGHT);
  }

  /**
   * Attempt to locate the proper large image from the given collection
   *
   * @param images List of the given Spotify images of various sizes
   * @return Optional String of the found (or not) large image url
   */
  protected Optional<String> findLargeImageUrl(List<Image> images) {
    return findClosestImageUrl(images, LARGE_ICON_WIDTH, LARGE_ICON_HEIGHT);
  }

  /**
   * Attempt to located the closest size-matched image from the given collection and the request
   * params. If a sized match can't be found, default to whatever image is available. If not images
   * can be found, return an empty Optional.
   *
   * @param images List of the given Spotify images of various sizes sorted from largest to smallest
   * @param reqWidth Request width to attempt match on
   * @param reqHeight Request height to attempt match on
   * @return Optional String of the found (or not) image url
   */
  protected Optional<String> findClosestImageUrl(List<Image> images, int reqWidth, int reqHeight) {
    if (hasImages(images)) {
      //loop over the images in reverse order of size (smallest first)
      for (Image image : Lists.reverse(images)) {
        if (image.width >= reqWidth && image.height >= reqHeight) {
          //return the closest match
          return Optional.of(image.url);
        }
      }
      //return whatever we have
      return Optional.of(images.get(0).url);
    }
    //no images found for this artist
    return Optional.absent();
  }

  /**
   * Determine if any images are available in this collection
   * @param images List of the given Spotify images
   * @return
   */
  protected boolean hasImages(List<Image> images) {
    return images != null && images.size() > 1;
  }

}
