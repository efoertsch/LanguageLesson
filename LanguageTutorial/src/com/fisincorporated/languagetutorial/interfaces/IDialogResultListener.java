package com.fisincorporated.languagetutorial.interfaces;

 import android.os.Bundle;

public interface IDialogResultListener  {
   public abstract void onDialogResult(int requestCode, int resultCode, int buttonPressed, Bundle  bundle);
}