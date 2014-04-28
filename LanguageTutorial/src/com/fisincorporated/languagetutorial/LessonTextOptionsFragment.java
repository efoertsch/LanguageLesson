package com.fisincorporated.languagetutorial;

import android.app.Activity;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.languagetutorial.utility.LanguageSettings;

public class LessonTextOptionsFragment extends OptionsFragment {
//	private Button btnSave;
//	private Button btnCancel;
	private CheckBox cbxDisplayKnownPhraseAvailable;
	//private RadioGroup rgBeforeAfter;
	private RadioButton rbtnBeforeLearningPhrase;
	private RadioButton rbtnAfterLearningPhrase;
	private ImageButton imgbtnDecreaseTextSize;
	private ImageButton imgbtnIncreaseTextSize;
	private TextView tvSampleText;

	// to hold original values of options
	private boolean displayKnownPhraseAvailable;
	private boolean beforeLearningPhrase;
	private boolean afterLearningPhrase;
	private float sampleTextSize;

	private static OptionsFragment lessonTextOptionsFragment = null;

	public static OptionsFragment getInstance() {
		if (lessonTextOptionsFragment == null) {
			lessonTextOptionsFragment = new LessonTextOptionsFragment();
		}
		return lessonTextOptionsFragment;
	}


	@Override
	protected int getMainLayout() {
		return R.layout.text_options;
	}

	protected void getLayoutFields(View view) {
		cbxDisplayKnownPhraseAvailable = (CheckBox) view
				.findViewById(R.id.cbxDisplayKnownPhraseAvailable);
		cbxDisplayKnownPhraseAvailable.requestFocus();
		cbxDisplayKnownPhraseAvailable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				enableDisableTextOptions(cbxDisplayKnownPhraseAvailable.isChecked());
				displayKnownPhraseAvailable = cbxDisplayKnownPhraseAvailable.isChecked();
				notifyOptionsChanged();
			}
		});

		//rgBeforeAfter = (RadioGroup) view.findViewById(R.id.rgBeforeAfter);
		
		rbtnBeforeLearningPhrase = (RadioButton) view
				.findViewById(R.id.rbtnBeforeLearningPhrase);
		rbtnBeforeLearningPhrase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				beforeLearningPhrase = rbtnBeforeLearningPhrase.isSelected();
				afterLearningPhrase = !rbtnBeforeLearningPhrase.isSelected();
				notifyOptionsChanged();
			}
		});
		rbtnAfterLearningPhrase = (RadioButton) view
				.findViewById(R.id.rbtnAfterLearningPhrase);
		rbtnAfterLearningPhrase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				afterLearningPhrase = rbtnAfterLearningPhrase.isSelected();
				beforeLearningPhrase = !rbtnAfterLearningPhrase.isSelected();
				notifyOptionsChanged();
			}
		});

		imgbtnDecreaseTextSize = (ImageButton) view
				.findViewById(R.id.imgbtnDecreaseTextSize);
		imgbtnDecreaseTextSize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (tvSampleText.getTextSize() < 20f) {
					return;
				}
				tvSampleText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						tvSampleText.getTextSize() - 1f);
				if (ignoreTextChanges)
					return;
				sampleTextSize = tvSampleText.getTextSize() ;
				notifyOptionsChanged();

			}
		});
		imgbtnIncreaseTextSize = (ImageButton) view
				.findViewById(R.id.imgbtnIncreaseTextSize);
		imgbtnIncreaseTextSize.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				tvSampleText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						tvSampleText.getTextSize() + 1f);
				if (ignoreTextChanges)
					return;
				sampleTextSize = tvSampleText.getTextSize() ;
				notifyOptionsChanged();
			}
		});

//		btnSave = (Button) view.findViewById(R.id.btnSave);
//		btnSave.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				storeOptionSettings();
//				Toast.makeText(getActivity(), R.string.text_options_saved,
//						Toast.LENGTH_SHORT).show();
//				returnToCaller(Activity.RESULT_OK);
//				// getActivity().finish();
//			}
//		});
//		btnCancel = (Button) view.findViewById(R.id.btnCancel);
//		btnCancel.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				notifyOptionChangesCancelled();
//				returnToCaller(Activity.RESULT_CANCELED);
//				getActivity().finish();
//
//			}
//		});

		tvSampleText = (TextView) view.findViewById(R.id.tvSampleText);
		cbxDisplayKnownPhraseAvailable.setFocusableInTouchMode(true);

	}

	protected void checkForUpdates() {
		if (displayKnownPhraseAvailable != languageSettings
				.getDisplayKnownTextWhenAvailable()
				|| beforeLearningPhrase != languageSettings
						.getDisplayKnownTextBeforeLearningPhrase()
				|| afterLearningPhrase != languageSettings
						.getDisplayKnownTextAfterLearningPhrase()
				|| sampleTextSize != languageSettings.getPhraseTextSize())
			notifyOptionsChanged();

	}

	protected void getOptionValues() {
		displayKnownPhraseAvailable = languageSettings
				.getDisplayKnownTextWhenAvailable();
		beforeLearningPhrase = languageSettings
				.getDisplayKnownTextBeforeLearningPhrase();
		afterLearningPhrase = languageSettings
				.getDisplayKnownTextAfterLearningPhrase();
		sampleTextSize = languageSettings.getPhraseTextSize();
	}

	protected void setOptionDisplayValues() {
		ignoreTextChanges = true;
		cbxDisplayKnownPhraseAvailable.setChecked(displayKnownPhraseAvailable);
		enableDisableTextOptions(displayKnownPhraseAvailable);
		rbtnBeforeLearningPhrase.setChecked(beforeLearningPhrase);
		rbtnAfterLearningPhrase.setChecked(afterLearningPhrase);
		tvSampleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, sampleTextSize);
		ignoreTextChanges = false;
	}

	private void enableDisableTextOptions(boolean enable) {
		//rgBeforeAfter.setEnabled(enable);
		 rbtnBeforeLearningPhrase.setEnabled(enable);
		 rbtnAfterLearningPhrase.setEnabled(enable);
	}

//	private void returnToCaller(int returnResult) {
//		getActivity().setResult(returnResult, null);
//		// getActivity().getSupportFragmentManager().popBackStack();
//	}

	// keep audio, even if not turned on, in sync with display of known language
	// phrase (even if not turned on)
	public void storeOptionSettings() {
		languageSettings
				.setDisplayKnownTextWhenAvailable(
						cbxDisplayKnownPhraseAvailable.isChecked())
				.setDisplayKnownTextBeforeLearningPhrase(
						rbtnBeforeLearningPhrase.isChecked())
				.setPlayBeforeLearningPhrase(rbtnBeforeLearningPhrase.isChecked())
				.setDisplayKnownTextAfterLearningPhrase(
						rbtnAfterLearningPhrase.isChecked())
				.setPlayAfterLearningPhrase(rbtnAfterLearningPhrase.isChecked())
				.setPhraseTextSize(tvSampleText.getTextSize()).commit();
		notifyOptionsSaved();
	}


	@Override
	public boolean isValidInput() {
		return true;
	}

 
	
}
