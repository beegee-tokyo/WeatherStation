package tk.giesecke.weatherstation;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * SwipeDetector
 * handles swipes on the screen
 *
 * @see <a href="http://stackoverflow.com/a/25051413/4407948">
 * How to detect the swipe left or Right in Android?</a>
 * @author Gal ROM - http://stackoverflow.com/a/25051413/4407948
 * @version 1.3 August 23, 2015
 */
public class SwipeDetector implements View.OnTouchListener{

	/** Float containing down movement in x axis */
	private float downX;
	/** Float containing down movement in y axis */
	private float downY;

	private onSwipeEvent swipeEventListener;



	public SwipeDetector(View v){
		v.setOnTouchListener(this);
	}

	/**
	 * Register listener for swipe events
	 *
	 * @param listener
	 *            Sensor sensor.
	 */
	public void setOnSwipeListener(onSwipeEvent listener)
	{
		try{
			swipeEventListener=listener;
		}
		catch(ClassCastException e)
		{
			Log.e("ClassCastException","please pass SwipeDetector.onSwipeEvent Interface instance",e);
		}
	}


	/**
	 * Reports swipes from right to left
	 */
	private void onRightToLeftSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.RIGHT_TO_LEFT);
		else
			Log.e("SwipeDetector error","please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	/**
	 * Reports swipes from left to right
	 */
	private void onLeftToRightSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.LEFT_TO_RIGHT);
		else
			Log.e("SwipeDetector error","please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	/**
	 * Reports swipes from top to bottom
	 */
	private void onTopToBottomSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.TOP_TO_BOTTOM);
		else
			Log.e("SwipeDetector error","please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	/**
	 * Reports swipes from bottom to top
	 */
	private void onBottomToTopSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.BOTTOM_TO_TOP);
		else
			Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	/**
	 * Receiver for touch events
	 * Calculates direction and length of swipe
	 *
	 * @param v
	 *          View where the touch/swipe happened
	 * @param event
	 *          Motion event info
	 * @return <code>boolean</code>
	 *          True if swipe is detected
	 *          False if no swipe is detected
	 */
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN: {
				downX = event.getX();
				downY = event.getY();
				return true;
			}
			case MotionEvent.ACTION_UP: {
				/** Float containing up movement in x axis */
				float upX = event.getX();
				/** Float containing up movement in y axis */
				float upY = event.getY();

				/** Float containing movement delta in x axis */
				float deltaX = downX - upX;
				/** Float containing movement delta in y axis */
				float deltaY = downY - upY;

				//HORIZONTAL SCROLL
				/** Minimum distance to accept as swipe */
				int min_distance = 150;
				if(Math.abs(deltaX) > Math.abs(deltaY))
				{
					if(Math.abs(deltaX) > min_distance){
						// left or right
						if(deltaX < 0)
						{
							this.onLeftToRightSwipe();
							return true;
						}
						if(deltaX > 0) {
							this.onRightToLeftSwipe();
							return true;
						}
					}
					else {
						//not long enough swipe...
						return false;
					}
				}
				//VERTICAL SCROLL
				else
				{
					if(Math.abs(deltaY) > min_distance){
						// top or down
						if(deltaY < 0)
						{ this.onTopToBottomSwipe();
							return true;
						}
						if(deltaY > 0)
						{ this.onBottomToTopSwipe();
							return true;
						}
					}
					else {
						//not long enough swipe...
						return false;
					}
				}

				return true;
			}
		}
		return false;
	}

	/**
	 * App interface for swipe events
	 */
	public interface onSwipeEvent
	{
		void SwipeEventDetected(SwipeTypeEnum SwipeType);
	}

	/**
	 * Definitions for swipe directions
	 */
	public enum SwipeTypeEnum
	{
		RIGHT_TO_LEFT,LEFT_TO_RIGHT,TOP_TO_BOTTOM,BOTTOM_TO_TOP
	}

}
