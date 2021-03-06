/**
 * 
 */
package com.gmail.charleszq.picorner.service.ig;

import org.jinstagram.entity.common.Pagination;

import com.gmail.charleszq.picorner.service.IPhotoService;

/**
 * @author charles(charleszq@gmail.com)
 *
 */
public abstract class AbstractInstagramPhotoListService implements
		IPhotoService {
	
	protected String TAG = getClass().getName();
	
	/**
	 * Record the pagination information so when we want to load next page, we
	 * can do from it.
	 */
	protected Pagination mPagination;

}
