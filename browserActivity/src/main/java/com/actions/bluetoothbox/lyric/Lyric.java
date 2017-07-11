package com.actions.bluetoothbox.lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lyric implements Serializable {

	private static final long serialVersionUID = 20071125L;
	List<Sentence> list = new ArrayList<Sentence>();
	private transient File file;
	private int offset = 0;
	private String name = "";
	private EncodingDetect ed = new EncodingDetect();
	private static final Pattern pattern = Pattern.compile("(?<=\\[).*?(?=\\])");

	public Lyric(File file, String musicName) {
		this.file = file;
		this.name = musicName;
		init(file);
	}

	public File getLyricFile() {
		return file;
	}

	private void init(File file) {
		if (!file.exists()) {
			list.add(new Sentence(name, 0, Integer.MAX_VALUE));
			return;
		}
		BufferedReader br = null;
		try {
			// Debug
			String fPath = file.getAbsolutePath();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), EncodingDetect.getJavaEncode(fPath)));// "UTF-8"
			StringBuilder sb = new StringBuilder();
			String temp = null;
			while ((temp = br.readLine()) != null) {

				sb.append(temp).append("\n");
			}
			init(sb.toString());
		} catch (Exception ex) {
			Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);

		} finally {
			try {
				br.close();
			} catch (Exception ex) {
				Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void init(String content) {
		if (content == null || content.trim().equals("")) {
			list.add(new Sentence(name, Integer.MIN_VALUE, Integer.MAX_VALUE));
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new StringReader(content));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				parseLine(temp.trim());
			}
			br.close();
			Collections.sort(list, new Comparator<Sentence>() {

				@Override
				public int compare(Sentence o1, Sentence o2) {
					return (int) (o1.getFromTime() - o2.getFromTime());
				}
			});
			if (list.size() == 0) {
				list.add(new Sentence(name, 0, Integer.MAX_VALUE));
				return;
			} else {
				Sentence first = list.get(0);
				list.add(0, new Sentence(name, 0, first.getFromTime()));
			}

			int size = list.size();
			for (int i = 0; i < size; i++) {
				Sentence next = null;
				if (i + 1 < size) {
					next = list.get(i + 1);
				}
				Sentence now = list.get(i);
				if (next != null) {
					now.setToTime(next.getFromTime() - 1);
				}
			}
			if (list.size() == 1) {
				list.get(0).setToTime(Integer.MAX_VALUE);
			} else {
				Sentence last = list.get(list.size() - 1);
				last.setToTime(Integer.MAX_VALUE);
			}
		} catch (Exception ex) {
			Logger.getLogger(Lyric.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private int parseOffset(String str) {
		String[] ss = str.split("\\:");
		if (ss.length == 2) {
			if (ss[0].equalsIgnoreCase("offset")) {
				int os = Integer.parseInt(ss[1]);
				return os;
			} else {
				return Integer.MAX_VALUE;
			}
		} else {
			return Integer.MAX_VALUE;
		}
	}

	private void parseLine(String line) {
		if (line.equals("")) {
			return;
		}
		Matcher matcher = pattern.matcher(line);
		List<String> temp = new ArrayList<String>();
		int lastIndex = -1;
		int lastLength = -1;
		while (matcher.find()) {
			String s = matcher.group();
			int index = line.indexOf("[" + s + "]");
			if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
				String content = line.substring(lastIndex + lastLength + 2, index);
				for (String str : temp) {
					long t = parseTime(str);
					if (t != -1) {
						list.add(new Sentence(content, t));
					}
				}
				temp.clear();
			}
			temp.add(s);
			lastIndex = index;
			lastLength = s.length();
		}
		if (temp.isEmpty()) {
			return;
		}
		try {
			int length = lastLength + 2 + lastIndex;
			String content = line.substring(length > line.length() ? line.length() : length);
			if (content.equals("") && offset == 0) {
				for (String s : temp) {
					int of = parseOffset(s);
					if (of != Integer.MAX_VALUE) {
						offset = of;
						break;
					}
				}
				return;
			}
			for (String s : temp) {
				long t = parseTime(s);
				if (t != -1) {
					list.add(new Sentence(content, t));
				}
			}
		} catch (Exception exe) {
		}
	}

	private long parseTime(String time) {
		String[] ss = time.split("\\:|\\.");
		if (ss.length < 2) {
			return -1;
		} else if (ss.length == 2) {
			try {
				if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
					offset = Integer.parseInt(ss[1]);
					return -1;
				}
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				if (min < 0 || sec < 0 || sec >= 60) {
					throw new RuntimeException("Number not allow!");
				}
				return (min * 60 + sec) * 1000L;
			} catch (Exception exe) {
				return -1;
			}
		} else if (ss.length == 3) {
			try {
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				int mm = Integer.parseInt(ss[2]);
				if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
					throw new RuntimeException("Number not allow!");
				}
				return (min * 60 + sec) * 1000L + mm * 10;
			} catch (Exception exe) {
				return -1;
			}
		} else {
			return -1;
		}
	}

	int getNowSentenceIndex(long t) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isInTime(t)) {
				return i;
			}
		}
		return -1;
	}

	public int getSongSize() {
		return list.size();
	}

}