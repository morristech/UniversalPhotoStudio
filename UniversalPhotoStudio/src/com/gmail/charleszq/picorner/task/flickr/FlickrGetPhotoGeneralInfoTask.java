/**
 * 
 */
package com.gmail.charleszq.picorner.task.flickr;

import com.gmail.charleszq.picorner.task.AbstractGeneralTask;
import com.gmail.charleszq.picorner.utils.FlickrHelper;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotosInterface;

/**
 * Represents the task to fetch the general informaiton of flickr photo.
 * 
 * @author charles(charleszq@gmail.com)
 * 
 */
public class FlickrGetPhotoGeneralInfoTask extends
		AbstractGeneralTask<String, Integer, Photo> {

	@Override
	protected Photo doInBackground(String... params) {
		String id = params[0];
		String secret = null;
		if (params.length > 1) {
			secret = params[1];
		}

		Flickr f = FlickrHelper.getInstance().getFlickr();
		PhotosInterface pi = f.getPhotosInterface();
		try {
			Photo p = pi.getInfo(id, secret);
			return p;
		} catch (Exception e) {
		}
		return null;
	}

}
