/**
 * 
 */
package com.gmail.charleszq.picorner.service.flickr;

import android.util.Log;

import com.gmail.charleszq.picorner.model.MediaObjectCollection;
import com.gmail.charleszq.picorner.utils.FlickrHelper;
import com.gmail.charleszq.picorner.utils.ModelUtils;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.galleries.GalleriesInterface;
import com.googlecode.flickrjandroid.photos.PhotoList;

/**
 * @author Charles(charleszq@gmail.com)
 * 
 */
public class FlickrGalleryPhotosService extends FlickrAuthPhotoService {

	private String mGalleryId;

	/**
	 * @param userId
	 * @param token
	 * @param secret
	 */
	public FlickrGalleryPhotosService(String userId, String token,
			String secret, String gid) {
		super(userId, token, secret);
		this.mGalleryId = gid;
	}

	@Override
	public MediaObjectCollection getPhotos(int pageSize, int pageNo)
			throws Exception {
		Log.d(TAG, String.format("page size %s and page# %s", pageSize, pageNo)); //$NON-NLS-1$
		if (pageNo > 0) {
			// for flickr gallery, each one has max 18 photos in it, so, we
			// don't need pagination here.
			return null;
		}
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(mAuthToken,
				mTokenSecret);
		GalleriesInterface si = f.getGalleriesInterface();
		PhotoList list = si
				.getPhotos(mGalleryId, mExtras, pageSize, pageNo + 1);
		return ModelUtils.convertFlickrPhotoList(list);
	}

}
