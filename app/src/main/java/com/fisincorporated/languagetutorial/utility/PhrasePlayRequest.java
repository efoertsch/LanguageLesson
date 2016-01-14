package com.fisincorporated.languagetutorial.utility;

public class PhrasePlayRequest {
	// directory media is in
	private String directory;
	// media file name
	private String filename;
	// delay playing the file till after x times audio duration (time to let user repeat phrase)
	// the prior media has played;
	private float playDelayFor = 0;
	// delay playing the file for x millisecs after the last file played
	private long playDelayMillis = 0;

	public PhrasePlayRequest(String directory, String filename,
			float delayPlayFor, long delayPlayForMillis) {
		this.directory = directory;
		this.filename = filename;
		this.playDelayFor = delayPlayFor;
		this.playDelayMillis = delayPlayForMillis;
		}
	 

	public String getDirectory() {
		return directory;
	}

	public String getFilename() {
		return filename;
	}

	public float getPlayDelayFor() {
		return playDelayFor;
	}

 
	public long getPlayDelayMillis() {
		return playDelayMillis;
	}

	 

	

}
