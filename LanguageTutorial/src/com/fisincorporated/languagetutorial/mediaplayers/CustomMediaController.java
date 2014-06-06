package com.fisincorporated.languagetutorial.mediaplayers;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;

public class CustomMediaController extends MediaController
{
   private FrameLayout anchorView;
   private boolean alwaysDisplay = false; 

   public CustomMediaController(Context context)
   {
       super(context);
             
   }
   public void setAnchorView(View view){
   	 this.anchorView = (FrameLayout)view;
   	 super.setAnchorView(view);
   }

   @Override
   protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
   {
       super.onSizeChanged(xNew, yNew, xOld, yOld);

       RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) anchorView.getLayoutParams();
       lp.setMargins(0, 0, 0, yNew);

       anchorView.setLayoutParams(lp);
       anchorView.requestLayout();
   }   
   
   
   @Override
   public void hide()
   {
  	 if (alwaysDisplay)
  		 show();
   }
   

	public void setAlwaysDisplay(boolean alwaysDisplay) {
		this.alwaysDisplay = alwaysDisplay;
	}


   
}
