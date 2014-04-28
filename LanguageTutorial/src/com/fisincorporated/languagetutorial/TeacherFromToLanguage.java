package com.fisincorporated.languagetutorial;
 
import com.fisincorporated.languagetutorial.utility.DomainObject;

import android.os.Parcel;
import android.os.Parcelable;

public class TeacherFromToLanguage implements Parcelable,DomainObject {
	//!! remember to add any new fields to readFromParcel/writeToParcel
	//private Long teacherLanguageId = -1l;
	// id is the TeacherLanguage.id
	private Long id;
	private String teacherLanguageTitle="";
	private Long learningLanguageId = -1l;
	private String learningLanguageName = "";
	private Long knownLanguageId = -1l;
	private String knownLanguageName = "";
	private Long teacherId = -1l;
	private String teacherName = "";
	
	
	public TeacherFromToLanguage() {
	}
	
	  public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
	
	public TeacherFromToLanguage(Parcel src) {
		readFromParcel(src);
	}
	

	public TeacherFromToLanguage(Long teacherLanguageId, String teacherLanguageTitle ) {
		 this.id = teacherLanguageId;
		 this.teacherLanguageTitle = teacherLanguageTitle;
	}

	public TeacherFromToLanguage(Long teacherLanguageId, String teacherLanguageTitle, Long learningLanguageId, String learningLanguageName
			, Long knownLanguageId, String knownLanguageName, Long teacherId, String teacherName ) {
		 this.id = teacherLanguageId;
		 this.teacherLanguageTitle = teacherLanguageTitle;
		 this.learningLanguageId = learningLanguageId;
		 this.learningLanguageName = learningLanguageName;
		 this.knownLanguageId = knownLanguageId; 
		 this.knownLanguageName = knownLanguageName; 
		 this.teacherId = teacherId;
		 this.teacherName = teacherName;
	}

//	public Long getTeacherLanguageId() {
//		return teacherLanguageId;
//	}
//
//	public void setTeacherLanguageId(Long teacherLanguageId) {
//		this.teacherLanguageId = teacherLanguageId;
//	}

	public String getTeacherLanguageTitle() {
		return teacherLanguageTitle;
	}

	public void setTeacherLanguageTitle(String teacherLanguageTitle) {
		this.teacherLanguageTitle = teacherLanguageTitle;
	}
	
	public String toString(){
		return  teacherLanguageTitle;
	}

	public Long getLearningLanguageId() {
		return learningLanguageId;
	}

	public void setLearningLanguageId(Long learningLanguageId) {
		this.learningLanguageId = learningLanguageId;
	}

	public String getLearningLanguageName() {
		return learningLanguageName;
	}

	public void setLearningLanguageName(String learningLanguageName) {
		this.learningLanguageName = learningLanguageName;
	}

	public Long getKnownLanguageId() {
		return knownLanguageId;
	}

	public void setKnownLanguageId(Long knownLanguageId) {
		this.knownLanguageId = knownLanguageId;
	}

	public String getKnownLanguageName() {
		return knownLanguageName;
	}

	public void setKnownLanguageName(String knownLanguageName) {
		this.knownLanguageName = knownLanguageName;
	}
	
	public static final Parcelable.Creator<TeacherFromToLanguage> CREATOR = new Parcelable.Creator<TeacherFromToLanguage>(){
		public TeacherFromToLanguage createFromParcel(Parcel src){
			return new TeacherFromToLanguage(src);
		}
		public TeacherFromToLanguage[] newArray(int size){
			return new TeacherFromToLanguage[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//dest.writeLong(teacherLanguageId);
		dest.writeLong(id);
		dest.writeString(teacherLanguageTitle);
		dest.writeLong(learningLanguageId);
		dest.writeString(learningLanguageName);
		dest.writeLong(knownLanguageId);
		dest.writeString(knownLanguageName);
		dest.writeLong(teacherId);
		dest.writeString(teacherName);
	}
	

	private void readFromParcel(Parcel src) {
		//teacherLanguageId = src.readLong();
		id = src.readLong();
		teacherLanguageTitle = src.readString();
		learningLanguageId = src.readLong();
		learningLanguageName = src.readString();
		knownLanguageId = src.readLong();
		knownLanguageName = src.readString();
		teacherId = src.readLong();
		teacherName = src.readString();
	}

	public Long getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	
}