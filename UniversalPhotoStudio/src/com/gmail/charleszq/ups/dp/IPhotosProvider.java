/**
 * 
 */
package com.gmail.charleszq.ups.dp;

import com.gmail.charleszq.ups.model.MediaObject;
import com.gmail.charleszq.ups.model.MediaObjectCollection;

/**
 * 
 * @author Charles(charleszq@gmail.com)
 *
 */
public interface IPhotosProvider {
	
	/**
	 * Returns the total count.
	 * @return
	 */
	int getTotalCount();

	/**
	 * Returns the <code>MediaObject</code> at the given index.
	 * @param index
	 * @return
	 */
	MediaObject getMediaObject(int index);
	
	/**
	 * Returns the current size of this data provider.
	 * @return
	 */
	int getCurrentSize();
	
	/**
	 * 
	 * @param listener
	 */
	void addDataChangeListener(IDataChangedListener listener);
	
	/**
	 * 
	 */
	void notifyDataChanged();
	
	/**
	 * When the service/task/command is done, this method can be called to populate the data.
	 * @param list
	 * @param source.
	 */
	void loadData(MediaObjectCollection list, Object source);
	
	/**
	 * Whether the provider has more pages on the server.
	 * @return
	 */
	boolean hasMorePage();
	
	/**
	 * Returns the current page number, start from 0
	 * @return
	 */
	int getCurrentPage();

}
