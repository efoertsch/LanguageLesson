package com.fisincorporated.languagetutorial;

import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

public class OptionsLessonTextFragment extends OptionsFragment {
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
			lessonTextOptionsFragment = new OptionsLessonTextFragment();
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
				beforeLearningPhrase = rbtnBeforeLearningPhrase.isChecked();
				afterLearningPhrase = !rbtnBeforeLearningPhrase.isChecked();
				notifyOptionsChanged();
				notifytBeforeAfterOptionChanged( 1);
			}
		});
		rbtnAfterLearningPhrase = (RadioButton) view
				.findViewById(R.id.rbtnAfterLearningPhrase);
		rbtnAfterLearningPhrase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				afterLearningPhrase = rbtnAfterLearningPhrase.isChecked();
				beforeLearningPhrase = !rbtnAfterLearningPhrase.isChecked();
				notifyOptionsChanged();
				notifytBeforeAfterOptionChanged(2);
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

 		tvSampleText = (TextView) view.findViewById(R.id.tvSampleText);
		cbxDisplayKnownPhraseAvailable.setFocusableInTouchMode(true);

	}
	
	protected void syncBeforeAfter(int beforeAfter){
		if (beforeAfter == 1){
			rbtnBeforeLearningPhrase.setChecked(true);
			rbtnAfterLearningPhrase.setChecked(false);
		}
		else {
			rbtnBeforeLearningPhrase.setChecked(false);
			rbtnAfterLearningPhrase.setChecked(true);
		}
		beforeLearningPhrase = rbtnBeforeLearningPhrase.isChecked();
		afterLearningPhrase = !rbtnBeforeLearningPhrase.isChecked();
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
