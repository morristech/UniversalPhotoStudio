/**
 * 
 */
package com.gmail.charleszq.picorner.ui.command.px500;

import android.app.Activity;
import android.content.Context;

import com.gmail.charleszq.picorner.PicornerApplication;
import com.gmail.charleszq.picorner.R;
import com.gmail.charleszq.picorner.model.Author;
import com.gmail.charleszq.picorner.service.IPhotoService;
import com.gmail.charleszq.picorner.service.px500.PxMyFlowService;

/**
 * @author charles(charleszq@gmail.com)
 *
 */
public class PxMyFlowCommand extends AbstractPx500PhotoListCommand {

	/**
	 * @param context
	 */
	public PxMyFlowCommand(Context context) {
		super(context);
	}

	@Override
	public String getLabel() {
		return mContext.getString(R.string.px_my_flow);
	}

	@Override
	public String getDescription() {
		return mContext.getString(R.string.cd_500px_my_flow);
	}

	@Override
	public int getIconResourceId() {
		return R.drawable.ic_action_500px_myflow;
	}
	
	@Override
	public Object getAdapter(Class<?> adapterClass) {
		if (adapterClass == IPhotoService.class) {
			PxMyFlowService s = new PxMyFlowService(getAuthToken(),
					getAuthTokenSecret(), getUserId());
			s.setPhotoCategory(mPhotoCategory);
			return s;
		}
		return super.getAdapter(adapterClass);
	}
	
	@Override
	public boolean execute(Object... params) {
		// first need to check if my 500px user id is saved or not.
		PicornerApplication app = (PicornerApplication) ((Activity) mContext)
				.getApplication();
		Author a = app.getPxUserProfile();
		if (a == null) {
			fetchUserProfile(params);
			return true;
		} else {
			return super.execute(params);
		}
	}

}
