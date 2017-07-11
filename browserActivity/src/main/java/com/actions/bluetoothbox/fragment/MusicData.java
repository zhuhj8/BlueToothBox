package com.actions.bluetoothbox.fragment;

public class MusicData {
	// music id
	private int musicID;
	// file name
	private String fileName;
	// music name
	private String musicName;
	// music total duration
	private int musicDuration;
	// music artist
	private String musicArtist;
	// music album
	private String musicAlbum;
	// music year
	private String musicYear;
	// file type
	private String fileType;
	// file size
	private String fileSize;
	// file path
	private String filePath;

	public int getMusicID() {
		return musicID;
	}

	public void setMusicID(int musicID) {
		this.musicID = musicID;
	}

	// get file name
	public String getFileName() {
		if (fileName == null) {
			return "unknow";
		} else {
			return fileName;
		}
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	// get music name
	public String getMusicName() {
		if (musicName == null) {
			return "unknow";
		} else {
			return musicName;
		}
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	// get music duration
	public int getMusicDuration() {
		return musicDuration;
	}

	public void setMusicDuration(int musicDuration) {
		this.musicDuration = musicDuration;
	}

	// get music artist
	public String getMusicArtist() {
		if (musicArtist == null) {
			return "unknow";
		} else {
			return musicArtist;
		}
	}

	public void setMusicArtist(String musicArtist) {
		this.musicArtist = musicArtist;
	}

	public String getMusicAlbum() {
		if (musicAlbum == null) {
			return "unknow";
		} else {
			return musicAlbum;
		}
	}

	public void setMusicAlbum(String musicAlbum) {
		this.musicAlbum = musicAlbum;
	}

	public String getMusicYear() {
		if (musicYear == null) {
			return "unknow";
		} else {
			return musicYear;
		}
	}

	public void setMusicYear(String musicYear) {
		this.musicYear = musicYear;
	}

	public String getFileType() {
		if (fileType == null) {
			return "unknow";
		} else {
			return fileType;
		}
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileSize() {
		if (fileSize == null) {
			return "unknow";
		} else {
			return fileSize;
		}
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getFilePath() {
		if (filePath == null) {
			return "unknow";
		} else {
			return filePath;
		}
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
