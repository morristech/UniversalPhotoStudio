/**
 * 
 */
package com.gmail.charleszq.ups.ui.command.px500;

import android.content.Context;

import com.gmail.charleszq.ups.R;
import com.gmail.charleszq.ups.service.IPhotoService;
import com.gmail.charleszq.ups.service.px500.Px500PopularPhotosService;
import com.gmail.charleszq.ups.ui.command.PhotoListCommand;

/**
 * @author charles(charleszq@gmail.com)
 * 
 */
public class PxPopularPhotosCommand extends PhotoListCommand {

	/**
	 * @param context
	 */
	public PxPopularPhotosCommand(Context context) {
		super(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gmail.charleszq.ups.ui.command.ICommand#getIconResourceId()
	 */
	@Override
	public int getIconResourceId() {
		return R.drawable.ic_action_px500_popular;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gmail.charleszq.ups.ui.command.ICommand#getLabel()
	 */
	@Override
	public String getLabel() {
		return mContext.getString(R.string.ig_popular);
	}

	@Override
	public Object getAdapter(Class<?> adapterClass) {
		if (adapterClass == IPhotoService.class) {
			return new Px500PopularPhotosService();
		}
		return super.getAdapter(adapterClass);
	}

}
