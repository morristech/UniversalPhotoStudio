/**
 * 
 */
package com.gmail.charleszq.picorner.ui.flickr;

import android.content.Context;
import android.view.View;

import com.gmail.charleszq.picorner.R;
import com.gmail.charleszq.picorner.task.IGeneralTaskDoneListener;
import com.gmail.charleszq.picorner.task.flickr.FetchPhotoSetsTask;
import com.gmail.charleszq.picorner.ui.command.ICommand;
import com.gmail.charleszq.picorner.ui.helper.AbstractHiddenListView;
import com.gmail.charleszq.picorner.ui.helper.PhotoCollectionItemAdapter;
import com.googlecode.flickrjandroid.photosets.Photosets;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public class MyPhotoSetsHiddenView extends AbstractHiddenListView {

	private FetchPhotoSetsTask mTask;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gmail.charleszq.picorner.ui.helper.AbstractHiddenListView#getContactList
	 * (android.content.Context)
	 */
	@Override
	protected void getData(Context ctx) {
		mTask = new FetchPhotoSetsTask(ctx);
		mTask.addTaskDoneListener(new IGeneralTaskDoneListener<Photosets>() {

			@Override
			public void onTaskDone(Photosets result) {
				if (result != null) {
					mAdapter.populateData(result.getPhotosets());
					mSpace.setVisibility(View.GONE);
				}
				//TODO what if user does not have photo set?
			}
		});
		mTask.execute();
	}

	@Override
	protected void initializeListViewAdapter(Context ctx, ICommand<?> command) {
		mAdapter = new PhotoCollectionItemAdapter(ctx, command);
		mLoadingMessage = ctx.getString(R.string.msg_loading_photo_sets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gmail.charleszq.picorner.ui.helper.AbstractHiddenListView#onCancel()
	 */
	@Override
	protected void onCancel() {
		if (mTask != null)
			mTask.cancel(true);
	}
}