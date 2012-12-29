/**
 * 
 */
package com.gmail.charleszq.picorner.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.gmail.charleszq.picorner.PicornerApplication;
import com.gmail.charleszq.picorner.R;
import com.gmail.charleszq.picorner.model.Author;
import com.gmail.charleszq.picorner.model.MediaSourceType;
import com.gmail.charleszq.picorner.task.IGeneralTaskDoneListener;
import com.gmail.charleszq.picorner.task.ig.InstagramCheckRelationshipTask;
import com.gmail.charleszq.picorner.task.ig.InstagramFollowUserTask;
import com.gmail.charleszq.picorner.ui.command.flickr.FlickrUserPhotosCommand;
import com.gmail.charleszq.picorner.ui.command.ig.InstagramUserPhotosCommand;
import com.gmail.charleszq.picorner.ui.command.px500.PxUserPhotosCommand;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public class UserPhotoListFragment extends AbstractPhotoGridFragment {

	/**
	 * The current user of the photos to be shown.
	 */
	private Author mCurrentUser;

	/**
	 * the ordinal of <code>MediaSourceType</code>
	 */
	private int mMedisSourceType = 0;

	/**
	 * The marker to show the follow menu item or not, when this fragment is
	 * attached on activity, we need to check the relationship with the photo
	 * owner, if it's instagram photo, we will show the menu item, and according
	 * the current relationship, we change the menu item title.
	 */
	private boolean mShowFollowMenu = false;

	/**
	 * 0: not ready yet, we don't know the relaitonship now; 1: following 2: not
	 * following
	 */
	private int mFollowing = 0;

	/**
	 * Constructor
	 */
	public UserPhotoListFragment() {
	}

	@Override
	protected void loadFirstPage() {
		if (mMedisSourceType == MediaSourceType.FLICKR.ordinal()) {
			mCurrentCommand = new FlickrUserPhotosCommand(getActivity(),
					mCurrentUser);
		} else if (mMedisSourceType == MediaSourceType.INSTAGRAM.ordinal()) {
			mCurrentCommand = new InstagramUserPhotosCommand(getActivity(),
					mCurrentUser);
		} else {
			// 500px
			mCurrentCommand = new PxUserPhotosCommand(getActivity(),
					mCurrentUser.getUserId());
		}
		mCurrentCommand.setCommndDoneListener(mCommandDoneListener);
		mCurrentCommand.execute();
		if (getActivity() != null) {
			getActivity().getActionBar().setSubtitle(
					mCurrentCommand.getDescription());
		}
	}

	@Override
	protected void initialIntentData(Intent intent) {
		mMedisSourceType = intent.getIntExtra(
				UserPhotoListActivity.MD_TYPE_KEY, 0);
		mCurrentUser = (Author) intent
				.getSerializableExtra(UserPhotoListActivity.USER_KEY);
	}

	@Override
	protected String getLoadingMessage() {
		return getString(R.string.msg_loading_more_photo_of_user);
	}

	@Override
	protected void bindData() {
		if (mCurrentUser != null && mLoadingMessageText != null) {
			String s = String.format(mLoadingMessage,
					mCurrentUser.getUserName());
			mLoadingMessageText.setText(s);
			if (mCurrentCommand != null) {
				// configuraton change
				mLoadingMessageText.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_ig_follow, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_follow) {
			final ProgressDialog dialog1 = ProgressDialog.show(getActivity(),
					"", //$NON-NLS-1$
					getString(R.string.msg_working));
			dialog1.setCanceledOnTouchOutside(true);
			IGeneralTaskDoneListener<Boolean> relationshipListener = new IGeneralTaskDoneListener<Boolean>() {

				@Override
				public void onTaskDone(Boolean result) {
					if (dialog1 != null && dialog1.isShowing()) {
						dialog1.dismiss();
					}
					if (result) {
						mFollowing = mFollowing == 1 ? 2 : 1;
						getActivity().invalidateOptionsMenu();
					} else {
						Toast.makeText(
								getActivity(),
								getString(R.string.msg_ig_chg_relationship_failed),
								Toast.LENGTH_SHORT).show();
					}
				}
			};
			InstagramFollowUserTask followTask = new InstagramFollowUserTask(
					getActivity());
			followTask.addTaskDoneListener(relationshipListener);
			followTask.execute(
					mCurrentUser.getUserId(),
					mFollowing == 1 ? Boolean.FALSE.toString() : Boolean.TRUE
							.toString());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menu_item_follow);
		if (!mShowFollowMenu) {
			item.setVisible(false);
		}

		switch (mFollowing) {
		case 0:
			item.setEnabled(false);
			break;
		case 1:
			item.setEnabled(true);
			item.setTitle(getString(R.string.menu_item_ig_unfollow_user));
			break;
		case 2:
			item.setEnabled(true);
			item.setTitle(getString(R.string.menu_item_ig_follow_user));
			break;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// now we have the user information, we need to check the relationship.
		if (mMedisSourceType == MediaSourceType.INSTAGRAM.ordinal()) {
			PicornerApplication app = (PicornerApplication) getActivity()
					.getApplication();
			if (app.getInstagramUserId() == null) {
				mShowFollowMenu = false;
				getActivity().invalidateOptionsMenu();
			} else {
				mShowFollowMenu = true;
				InstagramCheckRelationshipTask task = new InstagramCheckRelationshipTask(
						getActivity());
				task.addTaskDoneListener(new IGeneralTaskDoneListener<Boolean>() {

					@Override
					public void onTaskDone(Boolean result) {
						mFollowing = result ? 1 : 2;
						mShowFollowMenu = true;
						if (getActivity() != null)
							getActivity().invalidateOptionsMenu();

					}
				});
				task.execute(mCurrentUser.getUserId());
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
		i.putExtra(ImageDetailActivity.DP_KEY, mPhotosProvider);
		i.putExtra(ImageDetailActivity.LARGE_IMAGE_POSITION, position);
		startActivity(i);
	}

}
