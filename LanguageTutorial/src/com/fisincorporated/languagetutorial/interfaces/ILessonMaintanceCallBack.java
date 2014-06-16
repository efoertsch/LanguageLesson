package com.fisincorporated.languagetutorial.interfaces;

public interface ILessonMaintanceCallBack {
	public void passbackMessage(String message, boolean error); 
	public void passbackPercentComplete(int percentComplete, String message);
	public void completedProcess(int completionCode, String message);
	
}
