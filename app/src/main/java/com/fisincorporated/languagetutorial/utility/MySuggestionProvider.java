package com.fisincorporated.languagetutorial.utility;

import android.content.SearchRecentSuggestionsProvider;

 
public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
   public final static String AUTHORITY = "com.fisincorporated.languagetutorial.utility.MySuggestionProvider";
   public final static int MODE = DATABASE_MODE_QUERIES;

   public MySuggestionProvider() {
       setupSuggestions(AUTHORITY, MODE);
   }
}