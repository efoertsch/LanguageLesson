package com.fisincorporated.languagetutorial.utility;

public class LanguagePhraseRequest {

	private Long lessonId;
	private int lessonOrder;
	private Long languagePhraseXrefId;
	private boolean currentDisplayRequest;
	
	
//	public LanguagePhraseRequest(Long lessonId, int lessonOrder, Long languagePhraseXrefId, boolean currentDisplayRequest ) {
	public LanguagePhraseRequest(Long lessonId, int lessonOrder, Long languagePhraseXrefId ) {
		this.lessonId = lessonId;
		this.lessonOrder = lessonOrder;
		this.languagePhraseXrefId = languagePhraseXrefId;
		// this.currentDisplayRequest = currentDisplayRequest;
	}

	public Long getLanguagePhraseXrefId() {
		return languagePhraseXrefId;
	}


	public Long getLessonId() {
		return lessonId;
	}

	public void setLessonId(Long lessonId) {
		this.lessonId = lessonId;
	}

	public int getLessonOrder() {
		return lessonOrder;
	}

	public void setLessonOrder(int lessonOrder) {
		this.lessonOrder = lessonOrder;
	}

	public void setLanguagePhraseXrefId(Long languagePhraseXrefId) {
		this.languagePhraseXrefId = languagePhraseXrefId;
	}

//	public void setCurrentDisplayRequest(boolean currentDisplayRequest) {
//		this.currentDisplayRequest = currentDisplayRequest;
//	}
//	public boolean isCurrentDisplayRequest() {
//		return currentDisplayRequest;
//	}
	 

}
