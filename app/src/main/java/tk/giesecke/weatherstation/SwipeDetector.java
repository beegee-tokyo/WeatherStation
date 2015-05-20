package tk.giesecke.weatherstation;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * SwipeDetector
 * handles swipes on the screen
 *
 * @author Bernd Giesecke
 * @version 0.1 beta May 5, 2015.
 */
public class SwipeDetector implements View.OnTouchListener{

	private float downX;
	private float downY;

	private onSwipeEvent swipeEventListener;



	public SwipeDetector(View v){
		v.setOnTouchListener(this);
	}

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


	private void onRightToLeftSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.RIGHT_TO_LEFT);
		else
			Log.e("SwipeDetector error","please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	private void onLeftToRightSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.LEFT_TO_RIGHT);
		else
			Log.e("SwipeDetector error","please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	private void onTopToBottomSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.TOP_TO_BOTTOM);
		else
			Log.e("SwipeDetector error","please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	private void onBottomToTopSwipe(){
		if(swipeEventListener!=null)
			swipeEventListener.SwipeEventDetected(SwipeTypeEnum.BOTTOM_TO_TOP);
		else
			Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN: {
				downX = event.getX();
				downY = event.getY();
				return true;
			}
			case MotionEvent.ACTION_UP: {
				float upX = event.getX();
				float upY = event.getY();

				float deltaX = downX - upX;
				float deltaY = downY - upY;

				//HORIZONTAL SCROLL
				int min_distance = 100;
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
	public interface onSwipeEvent
	{
		void SwipeEventDetected(SwipeTypeEnum SwipeType);
	}

	public enum SwipeTypeEnum
	{
		RIGHT_TO_LEFT,LEFT_TO_RIGHT,TOP_TO_BOTTOM,BOTTOM_TO_TOP
	}

}
