package com.fisincorporated.languagetutorial.interfaces;

public interface IAsyncCallBacks {
	public void onPreExecute();
	public void onProgressUpdate(int percent);
	public void onPostExecute(int completionCode, String errorMsg);
 
}
