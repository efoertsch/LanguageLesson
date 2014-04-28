package com.fisincorporated.languagetutorial.utility;

import android.text.InputFilter;
import android.text.Spanned;

// from
//http://stackoverflow.com/questions/14212518/is-there-any-way-to-define-a-min-and-max-value-for-edittext-in-android
//http://tech.chitgoks.com/2011/06/27/android-set-min-max-value-an-edittext-accepts/
// use via (example assumes range of 1 to 12
//EditText et = (EditText) findViewById(R.id.myEditText);
//et.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "12")});

public class InputFilterMinMax implements InputFilter {

	private int min, max;

   public InputFilterMinMax(int min, int max) {
       this.min = min;
       this.max = max;
   }

   public InputFilterMinMax(String min, String max) {
       this.min = Integer.parseInt(min);
       this.max = Integer.parseInt(max);
   }
   

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            // Remove the string out of destination that is to be replaced
            String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
            // Add the new string in
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
            String startString = dest.toString().substring(0, dstart);
            String insert = source.toString();
            String endString = dest.toString().substring(dend);
            String parseThis = startString+insert+endString;
            int input = Integer.parseInt(parseThis);
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}