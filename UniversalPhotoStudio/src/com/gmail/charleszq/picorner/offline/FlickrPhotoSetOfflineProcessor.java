/**
 * 
 */
package com.gmail.charleszq.picorner.offline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.gmail.charleszq.picorner.PicornerApplication;
import com.gmail.charleszq.picorner.model.MediaObject;
import com.gmail.charleszq.picorner.model.MediaObjectCollection;
import com.gmail.charleszq.picorner.utils.FlickrHelper;
import com.gmail.charleszq.picorner.utils.IConstants;
import com.gmail.charleszq.picorner.utils.ImageUtils;
import com.gmail.charleszq.picorner.utils.ModelUtils;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Extras;
import com.googlecode.flickrjandroid.photosets.Photoset;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public class FlickrPhotoSetOfflineProcessor implements
		IOfflinePhotoCollectionProcessor {

	private static final String TAG = FlickrPhotoSetOfflineProcessor.class
			.getSimpleName();
	private static final int PAGE_SIZE = 100;

	/**
	 * The extras.
	 */
	private Set<String> mExtras = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gmail.charleszq.picorner.offline.IOfflinePhotoCollectionService#process
	 * (com.gmail.charleszq.picorner.offline.IOfflineViewParameter)
	 */
	@Override
	public void process(Context ctx, IOfflineViewParameter param) {
		File offlineFolder = createFlickrFolders();
		if (offlineFolder == null) {
			return; // should not happen
		}

		File controlFile = new File(offlineFolder, param.getControlFileName());
		if (controlFile.exists()) {
			List<MediaObject> read = readPhotos(param);
			if (read != null) {
				Log.d(TAG, read.size() + " photos saved in file before."); //$NON-NLS-1$
				saveDeltaHandle(ctx, param, read);
			}
		} else {
			firstTimeHandle(ctx, param);
		}

		// starts the download
		List<MediaObject> photos = readPhotos(param);
		if (photos == null) {
			Log.e(TAG, "error to read cache photo collection file."); //$NON-NLS-1$
		}

		File parentFolder = this.createFlickrFolders();
		File imageFolder = new File(parentFolder,
				IOfflineViewParameter.OFFLINE_IMAGE_FOLDER_NAME);
		for (MediaObject photo : photos) {
			File destFile = new File(imageFolder, photo.getId() + ".png"); //$NON-NLS-1$
			if (destFile.exists()) {
				Log.d(TAG, String.format(
						"photo %s was downloaded before.", photo.getId())); //$NON-NLS-1$
				continue;
			}

			String url = photo.getLargeUrl();
			Bitmap bmp = ImageUtils.downloadImage(url);
			if (bmp != null) {
				ImageUtils.saveImageToFile(destFile, bmp);
				Log.d(TAG,
						String.format(
								"photo %s saved for offline view later.", photo.getId())); //$NON-NLS-1$
			} else {
				Log.w(TAG, "unable to download the image: " + url); //$NON-NLS-1$
			}
		}
	}

	private void saveDeltaHandle(Context ctx, IOfflineViewParameter param,
			List<MediaObject> photos) {
		int serverPhotoCount = getCurrentCollectionPhotoCount(ctx, param);
		if (serverPhotoCount <= photos.size()) {
			// no addition on server for this photo set.
			Log.d(TAG, "no update for this photo set."); //$NON-NLS-1$
			return;
		}

		Log.d(TAG, String.format("before, there are %d photos", photos.size())); //$NON-NLS-1$
		int delta = serverPhotoCount - photos.size();
		int lastPage = getLastPage(serverPhotoCount, delta);
		boolean duplicateFound = false;
		while (lastPage > 0) {
			MediaObjectCollection col = getPhotoForPage(ctx, param, lastPage,
					delta);
			if (col != null) {
				for (MediaObject p : col.getPhotos()) {
					if (photos.contains(p)) {
						duplicateFound = true;
						break;
					} else {
						photos.add(0, p);
					}
				}
			}

			if (duplicateFound)
				break;
			lastPage--;
		}
		Log.d(TAG, String.format("after,  there are %d photos.", photos.size())); //$NON-NLS-1$

		// if exceeded the limit, remove some old photos.
		if (photos.size() > IConstants.DEF_MAX_TOTAL_PHOTOS) {
			photos = photos.subList(0, IConstants.DEF_MAX_TOTAL_PHOTOS);
		}

		savePhotoList(param, photos);
	}

	/**
	 * Saves the whole photo information.
	 * 
	 * @param ctx
	 * @param param
	 */
	private void firstTimeHandle(Context ctx, IOfflineViewParameter param) {
		int serverPhotoCount = getCurrentCollectionPhotoCount(ctx, param);
		if (serverPhotoCount == -1) {
			return;
		}

		int lastPageNo = getLastPage(serverPhotoCount, PAGE_SIZE);
		List<MediaObject> photos = new ArrayList<MediaObject>();

		int pageIndex = 0;
		while (lastPageNo > 0) {
			MediaObjectCollection col = getPhotoForPage(ctx, param, lastPageNo,
					PAGE_SIZE);

			if (col != null) {
				for (MediaObject p : col.getPhotos()) {
					photos.add(pageIndex, p);
					if (photos.size() >= IConstants.DEF_MAX_TOTAL_PHOTOS)
						break;
				}
			} else {
				break;
			}
			pageIndex = photos.size();
			if (photos.size() >= IConstants.DEF_MAX_TOTAL_PHOTOS)
				break;
			lastPageNo--;
		}

		if (!photos.isEmpty()) {
			savePhotoList(param, photos);
		}
	}

	private MediaObjectCollection getPhotoForPage(Context ctx,
			IOfflineViewParameter param, int pageNo, int pageSize) {
		PicornerApplication app = (PicornerApplication) ((Service) ctx)
				.getApplication();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
				app.getFlickrToken(), app.getFlickrTokenSecret());
		if (mExtras == null) {
			prepareExtras();
		}
		try {
			Photoset ps = f.getPhotosetsInterface().getPhotos(
					param.getPhotoCollectionId(), mExtras,
					Flickr.PRIVACY_LEVEL_NO_FILTER, pageSize, pageNo);
			User user = ps.getOwner();
			MediaObjectCollection col = ModelUtils.convertFlickrPhotoList(
					ps.getPhotoList(), user);
			return col;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the last page which contains the most recent photos.
	 * 
	 * @param serverPhotoCount
	 * @return
	 */
	private int getLastPage(int serverPhotoCount, int pageSize) {
		int maxPage = serverPhotoCount / pageSize;
		int remain = serverPhotoCount % pageSize;
		if (remain > 0) {
			maxPage++;
		}
		return maxPage;
	}

	private int getCurrentCollectionPhotoCount(Context ctx,
			IOfflineViewParameter param) {
		PicornerApplication app = (PicornerApplication) ((Service) ctx)
				.getApplication();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
				app.getFlickrToken(), app.getFlickrTokenSecret());
		try {
			Photoset ps = f.getPhotosetsInterface().getInfo(
					param.getPhotoCollectionId());
			Log.d(TAG, "offline photo set photo count: " + ps.getPhotoCount()); //$NON-NLS-1$
			return ps.getPhotoCount();
		} catch (Exception e) {
			Log.w(TAG, "unable to get the photo set information: " //$NON-NLS-1$
					+ e.getMessage());
			return -1;
		}
	}

	private void prepareExtras() {
		mExtras = new HashSet<String>();
		mExtras.add(Extras.URL_S);
		mExtras.add(Extras.URL_L);
		mExtras.add(Extras.OWNER_NAME);
		mExtras.add(Extras.GEO);
		mExtras.add(Extras.TAGS);
		mExtras.add(Extras.VIEWS);
		mExtras.add(Extras.DESCRIPTION);
	}

	private void savePhotoList(IOfflineViewParameter param,
			List<MediaObject> photos) {
		File offlineFolder = createFlickrFolders();
		if (offlineFolder == null) {
			return; // should not happen
		}

		File controlFile = new File(offlineFolder, param.getControlFileName());
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(controlFile));
			oos.writeObject(photos);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (oos != null) {
				try {
					oos.flush();
					oos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<MediaObject> readPhotos(IOfflineViewParameter param) {
		File offlineFolder = createFlickrFolders();
		if (offlineFolder == null) {
			return null; // should not happen
		}

		File controlFile = new File(offlineFolder, param.getControlFileName());
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(controlFile));
			List<MediaObject> photos = (List<MediaObject>) ois.readObject();
			return photos;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private File createFlickrFolders() {
		File offlineFolder = OfflineControlFileUtil
				.createOfflineFolderIfNeccessary();
		if (offlineFolder == null) {
			return null;
		}

		File ffolder = new File(offlineFolder,
				IOfflineViewParameter.OFFLINE_FLICKR_FOLDER_NAME);
		if (!ffolder.exists() && !ffolder.mkdir()) {
			return null;
		}

		File imageFolder = new File(ffolder,
				IOfflineViewParameter.OFFLINE_IMAGE_FOLDER_NAME);
		if (!imageFolder.exists()) {
			imageFolder.mkdir();
		}

		return ffolder;
	}

	@Override
	public List<MediaObject> getCachedPhotos(IOfflineViewParameter param) {
		return readPhotos(param);
	}
}