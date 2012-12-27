/**
 * 
 */
package com.gmail.charleszq.picorner.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.gmail.charleszq.picorner.R;
import com.gmail.charleszq.picorner.dp.IPhotosProvider;
import com.gmail.charleszq.picorner.dp.SinglePagePhotosProvider;
import com.gmail.charleszq.picorner.model.MediaObjectCollection;
import com.gmail.charleszq.picorner.ui.command.ICommand;
import com.gmail.charleszq.picorner.ui.command.ICommandDoneListener;
import com.gmail.charleszq.picorner.ui.command.PhotoListCommand;
import com.gmail.charleszq.picorner.ui.helper.OneTimeScrollListener;
import com.gmail.charleszq.picorner.ui.helper.PhotoGridAdapter;
import com.gmail.charleszq.picorner.utils.IConstants;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public abstract class AbstractPhotoGridFragment extends
		AbstractFragmentWithImageFetcher implements OnItemClickListener {

	private static String TAG = AbstractPhotoGridFragment.class.getName();

	/**
	 * UI controls
	 */
	protected GridView mGridView;
	protected TextView mLoadingMessageText;
	protected TextView mGridTitleText;

	/**
	 * Photo grid size information.
	 */
	protected int mImageThumbSize;
	protected int mImageThumbSpacing;

	/**
	 * The data provider to store photos.
	 */
	protected IPhotosProvider mPhotosProvider = new SinglePagePhotosProvider(
			new MediaObjectCollection());

	/**
	 * The photo grid adapter
	 */
	protected PhotoGridAdapter mAdapter;

	/**
	 * Save the current command, so we can load more data later.
	 */
	protected PhotoListCommand mCurrentCommand;

	/**
	 * m1: 'Photo of user' m2: 'Loading photos of user..."
	 */
	protected String mLoadingMessage;

	/**
	 * The marker to say no more data, then we don't do loading more.
	 */
	protected boolean mNoMoreData = false;

	protected OneTimeScrollListener mScrollListener = null;

	ICommandDoneListener<MediaObjectCollection> mCommandDoneListener = new ICommandDoneListener<MediaObjectCollection>() {
		@Override
		public void onCommandDone(ICommand<MediaObjectCollection> command,
				MediaObjectCollection t) {
			if (t == null || t.getPhotos().isEmpty()) {
				mNoMoreData = true;
			} else {
				mPhotosProvider.loadData(t, command);
				mAdapter.notifyDataSetChanged();
			}
			if (mLoadingMessageText != null) {
				mLoadingMessageText.setVisibility(View.GONE);
			}
		}
	};

	/**
	 * 
	 */
	public AbstractPhotoGridFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		initialIntentData(getActivity().getIntent());

		mLoadingMessage = getLoadingMessage();

		if (mGridView != null) {
			mGridView.setOnScrollListener(null);
		}
		if (mCurrentCommand != null) {
			mCurrentCommand.attacheContext(getActivity());
		}
		this.setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_photo_list_fragment, null);
		// layout ui controls
		mGridView = (GridView) v.findViewById(R.id.grid_user_photos);
		mLoadingMessageText = (TextView) v.findViewById(R.id.txt_user_info);
		mGridTitleText = (TextView) v.findViewById(R.id.txt_photo_grid_title);

		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		if (mAdapter == null) {
			mAdapter = new PhotoGridAdapter(getActivity(), mPhotosProvider,
					mImageFetcher);
		}
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mScrollListener = new GridOnScrollListener(this);
		mGridView.setOnScrollListener(mScrollListener);

		// This listener is used to get the final width of the GridView and then
		// calculate the
		// number of columns and the width of each column. The width of each
		// column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used
		// to set the height
		// of each view so we get nice square thumbnails.
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(mGridView
									.getWidth()
									/ (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth = (mGridView.getWidth() / numColumns)
										- mImageThumbSpacing;
								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth);
							}
						}
					}
				});

		bindData();
		if (mCurrentCommand == null) {
			loadFirstPage();
		}
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	/**
	 * The sub-classes need to handle that and hide/show message if any.
	 */
	protected void loadMoreData() {

		int currentPhotoSize = mPhotosProvider.getCurrentSize();
		Log.d(TAG, String.format(
				"When loading more, there are %s photos currently", //$NON-NLS-1$
				currentPhotoSize));
		boolean noMoreData = mNoMoreData;
		noMoreData = noMoreData
				| currentPhotoSize > IConstants.DEF_MAX_TOTAL_PHOTOS;
		if (currentPhotoSize > 0) {
			noMoreData = noMoreData
					| currentPhotoSize < IConstants.DEF_MIN_PAGE_SIZE;
		}
		if (noMoreData) {
			Log.d(TAG, "There is no more data."); //$NON-NLS-1$
			mLoadingMessageText.setVisibility(View.GONE);
			return;
		}

		Log.d(TAG, "Loading more..."); //$NON-NLS-1$
		mLoadingMessageText.setVisibility(View.VISIBLE);
		if (mCurrentCommand != null)
			mCurrentCommand.loadNextPage();
	}

	/**
	 * Loads the first page
	 */
	abstract protected void loadFirstPage();

	/**
	 * Initializes the intent data
	 * 
	 * @param intent
	 */
	abstract protected void initialIntentData(Intent intent);

	/**
	 * Gets the load message
	 * 
	 * @return
	 */
	abstract protected String getLoadingMessage();

	/**
	 * Bind data to UI, the data usually comes from the intent
	 */
	abstract protected void bindData();

	protected static class GridOnScrollListener extends OneTimeScrollListener {

		private AbstractPhotoGridFragment mFragment;

		protected GridOnScrollListener(AbstractPhotoGridFragment fragment) {
			this.mFragment = fragment;
		}

		@Override
		protected void loadMoreData() {
			mFragment.loadMoreData();
		}

		@Override
		protected void showGridTitle(boolean show) {
			mFragment.showGridTitle(show);
			
		}
	}

	private void showGridTitle(boolean show) {
		if( mGridTitleText == null ) {
			return;
		}
		if( mCurrentCommand == null ) {
			mGridTitleText.setVisibility(View.INVISIBLE);
			return;
		}
		String s = mCurrentCommand.getDescription();
		mGridTitleText.setText(s);
		mGridTitleText.setVisibility( show ? View.VISIBLE : View.INVISIBLE);
	}
}