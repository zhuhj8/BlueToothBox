package com.actions.bluetoothbox.util;

import java.util.ArrayList;

import com.actions.bluetoothbox.fragment.MusicData;

public interface Constant {

	/*
	 * 切换模块信息
	 */
	// public class Module
	// {
	// //是否当前功能模块标识，默认为0 module:1.蓝牙推送 2.卡歌播放 3.收音机
	// public static int CURRENT_MODULE = 0;
	// //保存前一个功能模块标签
	// public static int OLD_MODULE=0;
	// //区分控件控制标签 ------false模式为actionbar控制切换，true为slidingmenu控制切换 现默认为action控制
	// public static boolean CHOOSE_MODULE=false;
	//
	// }

	/*
	 * 歌曲信息
	 */
	public class MusicPlayData {
		// 歌曲信息列表
		public static ArrayList<MusicData> myMusicList = new ArrayList<MusicData>();
		// 当前播放歌曲索引号
		public static int CURRENT_PLAY_INDEX = 0;
		// 当前播放歌曲进度
		public static int CURRENT_PLAY_POSITION = 0;
		// 是否要重新播放标识
		public static boolean IS_PLAY_NEW = true;
		public static int TOTAL_TIME = 0;
	}

	/*
	 * 音乐播放状态
	 */
	public class MusicPlayState {
		// 暂停状态
		public static final int PLAY_STATE_PAUSE = 0;
		// 播放状态
		public static final int PLAY_STATE_PLAYING = 1;
		// 当前播放状态，默认为暂停状态
		public static int CURRENT_PLAY_STATE = PLAY_STATE_PAUSE;
	}

	/*
	 * 音乐播放模式
	 */
	public class MusicPlayMode {
		// 顺序播放
		public static final int PLAY_MODE_ORDER = 0;
		// 列表循环
		public static final int PLAY_MODE_LIST_LOOP = 1;
		// 随机播放
		public static final int PLAY_MODE_RANDOM = 2;
		// 单曲循环
		public static final int PLAY_MODE_SINGLE = 3;
		// 播放模式数组
		public static final int[] PLAY_MODE_ARRAY = { PLAY_MODE_ORDER, PLAY_MODE_LIST_LOOP, PLAY_MODE_RANDOM, PLAY_MODE_SINGLE };
		// 当前播放模式，默认播放模式为顺序播放
		public static int CURRENT_PLAY_MODE = PLAY_MODE_ARRAY[1];
	}

	/*
	 * 命令
	 */
	public class MusicPlayControl {
		// 播放命令
		public static final int MUSIC_CONTROL_PLAY = 0;
		// 暂停命令
		public static final int MUSIC_CONTROL_PAUSE = 1;
		// 上一首命令
		public static final int MUSIC_CONTROL_PREVIOUS = 2;
		// 下一首命令
		public static final int MUSIC_CONTROL_NEXT = 3;
		// 进度条点击命令
		public static final int MUSIC_CONTROL_SEEKBAR = 4;

		// 蓝牙控制
		public static final String SERVICECMD = "com.bluetooth.music.musicservicecommand";
		public static final String TOGGLEPAUSE_ACTION = "com.bluetooth.music.togglepause";
		public static final String PAUSE_ACTION = "com.bluetooth.music.pause";
		public static final String PREVIOUS_ACTION = "com.bluetooth.music.previous";
		public static final String NEXT_ACTION = "com.bluetooth.music.next";
	}

	/*
	 * 标识符
	 */
	public class MusicPlayVariate {
		// 当前播放索引标识符
		public static final String MUSIC_INDEX_STR = "playIndex";

		public static final String MUSIC_PLAY_DATA = "playdata";
		// 播放状态标识符
		public static final String MUSIC_PLAY_STATE_STR = "playState";
		// 控制命令标识符
		public static final String MUSIC_CONTROL_STR = "control";
		// 1-代表开始播放新歌，更新歌曲名，总播放时间，播放索引号
		public static final int MUSIC_PALY_DATA_INT = 1;

		public static String CTL_ACTION = "com.actions.action.CTL_ACTION";
		public static String UPDATE_ACTION = "com.actions.action.UPDATE_ACTION";
	}

	public static class IPCKey {
		public static final String ALARM_ENTRY = "alarm.entry";
	}
}
