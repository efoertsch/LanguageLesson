package com.fisincorporated.languagetutorial;

import com.fisincorporated.languagetutorial.interfaces.IDialogResultListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
 
// can be created/called from either Activity(use interface) or Fragment (use getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,
//intent);
public class LanguageDialogFragment extends DialogFragment  { 
	
 public final static String DIALOG_RESPONSE = "com.fisincorporated.languagetutorial.dialog.response"; 
 public static final String TITLE = "Title";
	public static final String MESSAGE = "Message";
	public static final String DIALOG_VIEW_ID = "DialogViewId";
	public static final String DIALOG_TEXT_VIEW_ID = "DialogTextViewId";
	public static final String DEFAULT_TEXT = "DefaultText";
	public static final String MESSAGE_STRING = "Message_String";
	public static final String POSITIVE_BUTTON_MSG = "Positive_button_msg";
	public static final String NEGATIVE_BUTTON_MSG = "Negative_button_msg";
	public static final String NEUTRAL_BUTTON_MSG = "Neutral_button_msg";
	public static final String LANGUAGE_DIALOG_TEXT_ENTRY = "LanguageDialogTextEntry";
	
	private  IDialogResultListener  iDialogResultListener = null;
	private int requestCode = -1;
	private EditText textView = null;
		
/**
 * Pass in resource id's or -1 if to ignore
 * @param title
 * @param message
 * @param positiveButtonMsg
 * @param negativeButtonMsg
 * @param neutralButtonMsg
 * @return
 */
	public static LanguageDialogFragment newInstance( int title, int message,int  positiveButtonMsg, int negativeButtonMsg, int neutralButtonMsg ) {
		LanguageDialogFragment frag = new LanguageDialogFragment();
		Bundle args = new Bundle();
		args.putInt(TITLE, title);
		args.putInt(MESSAGE, message);
		args.putInt(POSITIVE_BUTTON_MSG, positiveButtonMsg);
		args.putInt(NEGATIVE_BUTTON_MSG, negativeButtonMsg);
		args.putInt(NEUTRAL_BUTTON_MSG, neutralButtonMsg);
		frag.setArguments(args);
		return frag;
	}
	
	public static LanguageDialogFragment newInstance( int title, String message,int  positiveButtonMsg, int negativeButtonMsg, int neutralButtonMsg ) {
		LanguageDialogFragment frag = new LanguageDialogFragment();
		Bundle args = new Bundle();
		args.putInt(TITLE, title);
		args.putString(MESSAGE_STRING, message);
		args.putInt(POSITIVE_BUTTON_MSG, positiveButtonMsg);
		args.putInt(NEGATIVE_BUTTON_MSG, negativeButtonMsg);
		args.putInt(NEUTRAL_BUTTON_MSG, neutralButtonMsg);
		frag.setArguments(args);
		return frag;
	}
	
	// this version will cause a custom view to be defined with input expected in the text view
	public static LanguageDialogFragment newInstance(int view, int textView, String defaultText, int title, String message,int  positiveButtonMsg, int negativeButtonMsg, int neutralButtonMsg ) {
		LanguageDialogFragment frag = new LanguageDialogFragment();
		Bundle args = new Bundle();
		args.putInt(DIALOG_VIEW_ID, view);
		args.putInt(DIALOG_TEXT_VIEW_ID, textView);
		args.putString(DEFAULT_TEXT, defaultText);
		args.putInt(TITLE, title);
		args.putString(MESSAGE_STRING, message);
		args.putInt(POSITIVE_BUTTON_MSG, positiveButtonMsg);
		args.putInt(NEGATIVE_BUTTON_MSG, negativeButtonMsg);
		args.putInt(NEUTRAL_BUTTON_MSG, neutralButtonMsg);
		frag.setArguments(args);
		return frag;
	}
	
	 

	public void setOnDialogResultListener(IDialogResultListener listener, int requestCode ) {
	    this.iDialogResultListener = listener;
	    this.requestCode = requestCode;
	}
	

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		int viewId = getArguments().getInt(DIALOG_VIEW_ID,-1);
		int textViewId = getArguments().getInt(DIALOG_TEXT_VIEW_ID,-1);
		int title = getArguments().getInt(TITLE, -1);
		int message = getArguments().getInt(MESSAGE, -1);
		String messageString = getArguments().getString(MESSAGE_STRING);
		int positiveMsg = getArguments().getInt(POSITIVE_BUTTON_MSG, -1);
		int negativeMsg = getArguments().getInt(NEGATIVE_BUTTON_MSG, -1);
		int neutralMsg = getArguments().getInt(NEUTRAL_BUTTON_MSG , -1);
		if (viewId != -1){
			 LayoutInflater inflater = getActivity().getLayoutInflater();
			 View view = inflater.inflate(viewId , null);
			 builder.setView(view);
			 textView  = (EditText) view.findViewById(textViewId);
			 String defaultText = getArguments().getString(DEFAULT_TEXT);
			 if (defaultText != null)
				 textView.setText(defaultText);
		}
		if (title != -1){
			builder.setTitle(title);
		}
		if (message != -1){
			builder.setMessage(message);
		}
		else if (messageString != null){
			builder.setMessage(messageString);
		}
	 
		if (positiveMsg != -1){
		// The OK button returns a (possibly) updated filter list)
		builder.setPositiveButton(positiveMsg,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//Log.i(LOG_TAG, "Positive Button Clicked");
						sendResult(Activity.RESULT_OK, id);
					}
				});
		}
		if (negativeMsg != -1){
		// Negative button is Cancel so return the original filter list
		builder.setNegativeButton(negativeMsg,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//Log.i(LOG_TAG, "NegativeButton Clicked");
						sendResult(Activity.RESULT_OK, id);
					}
				});
		}
		if (neutralMsg != -1){
		// set neutral is to clear filter (show all)
		builder.setNeutralButton(neutralMsg,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//Log.i(LOG_TAG, "Neutral Clicked");
						sendResult(Activity.RESULT_OK, id);
					}
				});
		}
		return builder.create();
	}

 
	private void sendResult(int resultCode, int button_pressed) {
		// first see if activity (or some other object that implemented interface
		
		 
		if (  iDialogResultListener != null) {
			Bundle bundle = new Bundle();
			if ( textView != null){
				bundle.putString(LANGUAGE_DIALOG_TEXT_ENTRY, textView.getText().toString());
			}
			iDialogResultListener.onDialogResult(requestCode, resultCode,button_pressed, bundle);
			iDialogResultListener = null;
			dismiss();
		}
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(LanguageDialogFragment.DIALOG_RESPONSE , button_pressed );
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,
				intent);
	}

	 
	 

}
