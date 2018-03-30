package ua.rsa.gd;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/** 
 * In order to do so, we must implement the ItemizedOverlay class, 
 * which can manage a whole set of Overlay (which are the individual items placed on the map).
 * @author Komarev Roman
 * Odessa, neo3da@mail.ru, +380503412392
 */
public class MapItemizedOverlay extends ItemizedOverlay
{
	/** 
	 * We need an OverlayItem ArrayList, in which we'll put each of the OverlayItem 
	 * objects we want on the map. 
	 */
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	/** To set up the ability to handle touch events on the overlay items. */
	Context mContext;
	
	/** 
	 * In order for the Drawable to actually get drawn, it must have its bounds defined. 
	 * Most commonly, you want the center-point at the bottom of the image to be the point at which it's attached to the map coordinates.
	 * @param defaultMarker
	 */
	public MapItemizedOverlay(Drawable defaultMarker)
	{
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	/** 
	 * In order to add new OverlayItems to the ArrayList, we need a new method.
	 * Each time you add a new OverlayItem to the ArrayList, you must call populate() 
	 * for the ItemizedOverlay, which will read each of the OverlayItems and prepare them to be drawn.
	 * @param overlay
	 */
	public void addOverlay(OverlayItem overlay) 
	{
	    mOverlays.add(overlay);
	    populate();
	}
	
	/** 
	 * When the populate() method executes, it will call createItem(int) 
	 * in the ItemizedOverlay to retrieve each OverlayItem. We must 
	 * override this method to properly read from the ArrayList and 
	 * return the OverlayItem from the position specified by the given integer. 
	 */
	@Override
	protected OverlayItem createItem(int i) 
	{
		return mOverlays.get(i);
	}

	/** 
	 * We must also override the size() method to return the current number of items in the ArrayList:
	 */
	@Override
	public int size() 
	{
		return mOverlays.size();
	}

	/** 
	 * This passes the defaultMarker up to the default constructor to bound its coordinates 
	 * and then initialize mContext with the given Context.
	 * @param defaultMarker
	 * @param context
	 */
	public MapItemizedOverlay(Drawable defaultMarker, Context context) 
	{
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}
	
	/** 
	 * Then override the onTap(int) callback method, which will handle 
	 * the event when an item is tapped by the user.
	 * This uses the member android.content.Context to create a new AlertDialog.Builder 
	 * and uses the tapped OverlayItem's title and snippet for the dialog's title and message text. 
	 * (We'll see the OverlayItem title and snippet defined when you create it below.)
	 */
	@Override
	protected boolean onTap(int index) 
	{
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}
}
