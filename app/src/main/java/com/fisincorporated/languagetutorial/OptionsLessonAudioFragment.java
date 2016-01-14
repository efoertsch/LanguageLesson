package com.fisincorporated.languagetutorial;

import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import com.fisincorporated.languagetutorial.utility.InputFilterMinMax;
import com.fisincorporated.languagetutorial.utility.InputFloatFilterMinMax;

public class OptionsLessonAudioFragment extends OptionsFragment {
	private static final String TAG = "OptionsLessonAudioFragment";
	// private Button btnSave;
	// private Button btnCancel;
	private CheckBox cbxPlayAudioWhenAvailable;
	private CheckBox cbxRepeatPhrase;
	private EditText etRepeatXTimes;
	private EditText etPhraseDelayByDuration;
	private EditText etWaitXSeconds;
	private CheckBox cbxPlayKnownPhrase;
	// private RadioGroup rgBeforeAfter;
	private RadioButton rbtnBeforeLearningPhrase;
	private RadioButton rbtnAfterLearningPhrase;
	private CheckBox cbxStepAutomatically;
	private int minRepeat = 1;
	private int maxRepeat = 10;
	private float minWaitXSeconds = 0;
	private float maxWaitXSeconds = 10;
	private float minPauseTimesDuration = 0;
	private float maxPauseTimesDuration = 0;
	private static LanguageDialogFragment dialog;
	private static OptionsFragment lessonAudioOptionsFragment = null;

	// original options settings
	private boolean playAudioWhenAvailable;
	private boolean repeatPhrase;
	private int repeatXTimes;
	private float phraseDelayByDuration;
	private float waitXSeconds;
	private boolean playKnownPhrase;
	private boolean beforeLearningPhrase;
	private boolean afterLearningPhrase;
	private boolean stepAutomatically;

	// Requestcodes for Dialog fragment
	private static final int VALIDATION_ERROR = 1;

	public static OptionsFragment getInstance() {
		if (lessonAudioOptionsFragment == null) {
			lessonAudioOptionsFragment = new OptionsLessonAudioFragment();
		}
		return lessonAudioOptionsFragment;
	}

	protected int getMainLayout() {
		return R.layout.audio_options;
	}

	@Override
	public void onResume() {
		// TextWatchers fired during orientation change - when no data changes
		// have occurred and cause the change flag to be
		// set when it should not, so add TextWatchers here after data assigned (I
		// hope)
		// and remove on onPause()
		ignoreTextChanges = false;
		etRepeatXTimes
				.addTextChangedListener(new DataTextWatcher(etRepeatXTimes));
		etPhraseDelayByDuration.addTextChangedListener(new DataTextWatcher(
				etPhraseDelayByDuration));
		etWaitXSeconds
				.addTextChangedListener(new DataTextWatcher(etWaitXSeconds));
		Log.i(TAG, "onResume() ignoreTextChanges set to" + ignoreTextChanges
				+ "  and added textwatchers");
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		etRepeatXTimes.addTextChangedListener(null);
		etPhraseDelayByDuration.addTextChangedListener(null);
		etWaitXSeconds.addTextChangedListener(null);
		ignoreTextChanges = true;
		Log.i(TAG, "onPause() ignoreTextChanges set to" + ignoreTextChanges
				+ " and removed textwatchers");
	}

	protected void getLayoutFields(View view) {
		cbxPlayAudioWhenAvailable = (CheckBox) view
				.findViewById(R.id.cbxPlayAudioWhenAvailable);
		cbxPlayAudioWhenAvailable.requestFocus();
		cbxPlayAudioWhenAvailable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				enableDisableAudioOptions(cbxPlayAudioWhenAvailable.isChecked());
				playAudioWhenAvailable = cbxPlayAudioWhenAvailable.isChecked();
				notifyOptionsChanged();
			}
		});
		cbxRepeatPhrase = (CheckBox) view.findViewById(R.id.cbxRepeatPhrase);
		cbxRepeatPhrase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				enableDisablePhraseRepeat(cbxRepeatPhrase.isChecked());
				repeatPhrase = cbxRepeatPhrase.isChecked();
				notifyOptionsChanged();
			}
		});

		etRepeatXTimes = (EditText) view.findViewById(R.id.etRepeatXTimes);
		etPhraseDelayByDuration = (EditText) view
				.findViewById(R.id.etPhraseDelayByDuration);

		etWaitXSeconds = (EditText) view.findViewById(R.id.etWaitXSeconds);

		cbxPlayKnownPhrase = (CheckBox) view
				.findViewById(R.id.cbxPlayKnownPhrase);
		cbxPlayKnownPhrase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				enableDisableBeforeAfter(cbxPlayKnownPhrase.isChecked());
				playKnownPhrase = cbxPlayKnownPhrase.isChecked();
				notifyOptionsChanged();
			}
		});

		// rgBeforeAfter = (RadioGroup) view.findViewById(R.id.rgBeforeAfter);

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
				notifytBeforeAfterOptionChanged( 2);
			}
		});

		cbxStepAutomatically = (CheckBox) view
				.findViewById(R.id.cbxStepAutomatically);
		cbxStepAutomatically.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ignoreTextChanges)
					return;
				stepAutomatically = cbxStepAutomatically.isSelected();
				notifyOptionsChanged();
			}
		});

		cbxPlayAudioWhenAvailable.setFocusableInTouchMode(true);

	}

//	private void returnToCaller(int returnResult) {
//		getActivity().setResult(returnResult, null);
//		// getActivity().getSupportFragmentManager().popBackStack();
//	}
	
	// before = 1, after = 2
	protected void syncBeforeAfter(int beforeAfter){
		if (beforeAfter == 1){
			rbtnBeforeLearningPhrase.setChecked(true);
			rbtnAfterLearningPhrase.setChecked(false);
		}
		else {
			rbtnBeforeLearningPhrase.setChecked(false);
			rbtnAfterLearningPhrase.setChecked(true);
		}
		beforeLearningPhrase = rbtnBeforeLearningPhrase.isSelected();
		afterLearningPhrase = !rbtnBeforeLearningPhrase.isSelected();
	}

	protected void checkForUpdates() {
		if (playAudioWhenAvailable != languageSettings
				.getPlayAudioWhenAvailable()
				|| repeatPhrase != languageSettings.getRepeatPhrase()
				|| repeatXTimes != languageSettings.getRepeatXTimes()
				|| phraseDelayByDuration != languageSettings
						.getPhraseDelayByDuration()
				|| waitXSeconds != languageSettings.getWaitXSeconds()
				|| playKnownPhrase != languageSettings.getPlayKnownPhrase()
				|| beforeLearningPhrase != languageSettings
						.getPlayBeforeLearningPhrase()
				|| afterLearningPhrase != languageSettings
						.getPlayAfterLearningPhrase()
				|| stepAutomatically != languageSettings
						.getStepThroughLessonAutomatically())
			notifyOptionsChanged();
	}

	protected void getOptionValues() {
		playAudioWhenAvailable = languageSettings.getPlayAudioWhenAvailable();
		repeatPhrase = languageSettings.getRepeatPhrase();
		repeatXTimes = languageSettings.getRepeatXTimes();
		phraseDelayByDuration = languageSettings.getPhraseDelayByDuration();
		waitXSeconds = languageSettings.getWaitXSeconds();
		playKnownPhrase = languageSettings.getPlayKnownPhrase();
		beforeLearningPhrase = languageSettings.getPlayBeforeLearningPhrase();
		afterLearningPhrase = languageSettings.getPlayAfterLearningPhrase();
		stepAutomatically = languageSettings.getStepThroughLessonAutomatically();
	}

	protected void setOptionDisplayValues() {
		ignoreTextChanges = true;
		cbxPlayAudioWhenAvailable.setChecked(playAudioWhenAvailable);
		cbxRepeatPhrase.setChecked(repeatPhrase);
		etRepeatXTimes.setText(repeatXTimes + "");
		etRepeatXTimes.setFilters(new InputFilter[] { new InputFilterMinMax(
				minRepeat, maxRepeat) });
		etPhraseDelayByDuration.setText(phraseDelayByDuration + "");
		etPhraseDelayByDuration
				.setFilters(new InputFilter[] { new InputFloatFilterMinMax(0f, 10f) });
		etWaitXSeconds.setText(waitXSeconds + "");
		etWaitXSeconds.setFilters(new InputFilter[] { new InputFloatFilterMinMax(
				0f, 10f) });
		cbxPlayKnownPhrase.setChecked(playKnownPhrase);
		rbtnBeforeLearningPhrase.setChecked(playKnownPhrase);
		rbtnAfterLearningPhrase.setChecked(afterLearningPhrase);
		cbxStepAutomatically.setChecked(stepAutomatically);

		enableDisablePhraseRepeat(repeatPhrase);
		// do next 2 in reverse order
		enableDisableBeforeAfter(playKnownPhrase);
		enableDisableAudioOptions(playAudioWhenAvailable);
		ignoreTextChanges = false;
	}

	private void enableDisableAudioOptions(boolean enable) {
		cbxRepeatPhrase.setEnabled(enable);
		// enableDisablePhraseRepeat(enable);
		cbxPlayKnownPhrase.setEnabled(enable);
		// enableDisableBeforeAfter(enable);
	}

	private void enableDisablePhraseRepeat(boolean enable) {
		etRepeatXTimes.setEnabled(enable);
		etPhraseDelayByDuration.setEnabled(enable);
		etWaitXSeconds.setEnabled(enable);
	}

	private void enableDisableBeforeAfter(boolean enable) {
		// rgBeforeAfter.setEnabled(enable);
		rbtnBeforeLearningPhrase.setEnabled(enable);
		rbtnAfterLearningPhrase.setEnabled(enable);
	}

	// public boolean validEntries() {
	// boolean validEntry = true;
	// if (cbxRepeatPhrase.isChecked()) {
	// if (!validRepeatXTimes()) return false;
	// if (!validWaitXSeconds() || !validPhraseDelayByDuration()) return false;
	// validEntry = validDurationValues();
	// }
	// return validEntry;
	// }

	private boolean validRepeatXTimes() {
		try {
			repeatXTimes = Integer.parseInt(etRepeatXTimes.getText().toString()
					.trim());
			if (repeatXTimes != 0)
				waitXSeconds = 0;
		} catch (NumberFormatException nfe) {
			showErrorDialog(res.getString(R.string.invalid_number_repeatXTimes,
					minRepeat, maxRepeat));
			return false;
		}
		return true;
	}

	private boolean validWaitXSeconds() {
		try {
			waitXSeconds = Float.parseFloat(etWaitXSeconds.getText().toString()
					.trim());
			if (waitXSeconds != 0)
				repeatXTimes = 0;
		} catch (NumberFormatException nfe) {
			showErrorDialog(res.getString(R.string.invalid_number_waitXSeconds,
					minWaitXSeconds, maxWaitXSeconds,
					res.getString(R.string.based_on_phrase_duration)));
			return false;
		}
		return true;
	}

	private boolean validPhraseDelayByDuration() {
		try {
			phraseDelayByDuration = Float.parseFloat(etPhraseDelayByDuration
					.getText().toString().trim());
		} catch (NumberFormatException nfe) {
			showErrorDialog(res.getString(R.string.invalid_pause_times_duration,
					minPauseTimesDuration, maxPauseTimesDuration,
					res.getString(R.string.wait_this_many_seconds)));
			return false;
		}
		return true;
	}

	private boolean validDurationValues() {
		if (waitXSeconds != 0 && phraseDelayByDuration != 0) {
			showErrorDialog(res.getString(
					R.string.one_or_other_values_must_be_zero,
					res.getString(R.string.by_phrase_duration_times_this_number),
					res.getString(R.string.wait_this_many_seconds)));
			return false;
		}
		if (waitXSeconds == 0 && phraseDelayByDuration == 0) {
			showErrorDialog(res.getString(
					R.string.one_or_other_values_must_be_non_zero,
					res.getString(R.string.based_on_phrase_duration),
					res.getString(R.string.wait_this_many_seconds)));
			return false;
		}
		return true;
	}

	private void showErrorDialog(String message) {
		showDialog(message, VALIDATION_ERROR, R.string.ok, -1, -1);
	}

	public void showDialog(String loadMsg, int requestCode, int yesResource,
			int noResource, int cancelResource) {
		dialog = LanguageDialogFragment.newInstance(-1, loadMsg, yesResource,
				noResource, cancelResource);
		dialog.setTargetFragment(OptionsLessonAudioFragment.this, requestCode);
		dialog.show(getActivity().getSupportFragmentManager(), "errorDialog");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == VALIDATION_ERROR) {
			// doesn't matter what is pressed, just acknowledging the error
		}
	}

	// keep display of known text, even if not turned on, in sync with audio play
	// of known language phrase (even if not turned on)
	public void storeOptionSettings() {
		languageSettings
				.setPlayAudioWhenAvailable(cbxPlayAudioWhenAvailable.isChecked())
				.setRepeatPhrase(cbxRepeatPhrase.isChecked())
				.setRepeatXTimes(
						Integer.parseInt(etRepeatXTimes.getText().toString().trim()))
				.setPhraseDelayByDuration(
						Float.parseFloat(etPhraseDelayByDuration.getText().toString()
								.trim()))
				.setWaitXSeconds(
						Float.parseFloat(etWaitXSeconds.getText().toString().trim()))
				.setPlayKnownPhrase(cbxPlayKnownPhrase.isChecked())
				.setPlayBeforeLearningPhrase(rbtnBeforeLearningPhrase.isChecked())
				.setDisplayKnownTextBeforeLearningPhrase(
						rbtnBeforeLearningPhrase.isChecked())
				.setPlayAfterLearningPhrase(rbtnAfterLearningPhrase.isChecked())
				.setDisplayKnownTextAfterLearningPhrase(
						rbtnAfterLearningPhrase.isChecked())
				.setStepThroughLessonAutomatically(cbxStepAutomatically.isChecked())
				.commit();
		notifyOptionsSaved();

	}

	private class DataTextWatcher implements TextWatcher {
		private View view;

		private DataTextWatcher(View view) {
			this.view = view;
		}

		@Override
		public void afterTextChanged(Editable editable) {
			switch (view.getId()) {
			case R.id.etRepeatXTimes:
				validRepeatXTimes();
				break;
			case R.id.etPhraseDelayByDuration:
				if (validPhraseDelayByDuration())
					validDurationValues();
				break;
			case R.id.etWaitXSeconds:
				if (validWaitXSeconds())
					validDurationValues();
				break;
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// nothing to do here, ignore
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (ignoreTextChanges)
				return;
			notifyOptionsChanged();
		}

	}

	@Override
	public boolean isValidInput() {
		boolean validEntry = true;
		if (cbxRepeatPhrase.isChecked()) {
			if (!validRepeatXTimes())
				return false;
			if (!validWaitXSeconds() || !validPhraseDelayByDuration())
				return false;
			validEntry = validDurationValues();
		}
		return validEntry;
	}

}
