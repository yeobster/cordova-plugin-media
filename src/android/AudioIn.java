package org.apache.cordova.media;

import android.media.AudioFormat;
import android.media.AudioRecord;

public class AudioIn {

	public static final int MAX_FRAMEBUF = 100;
	boolean audiorec = false;
	public AudioRecord audioRecord = null;
	public int bufferSizeShort = 0;
	public short[][] buffer = null;
	public short[] buffer2 = null;
	public int buf_wpos = 0;
	public int buf_use = 0;
	public int buf_rpos = 0;
	public boolean Thread_flag = false;
	public boolean rec_flag = false;
	private int frame_count = 0;

	public AudioIn(int samplerate, int recmode) {
		// ----------------------------------------Sound In
		// Define-------------------------------------------//
		bufferSizeShort = AudioRecord.getMinBufferSize(samplerate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		int step = bufferSizeShort;
		while (bufferSizeShort < 4000)
			bufferSizeShort += step;

		audioRecord = new AudioRecord(recmode, samplerate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSizeShort);
		bufferSizeShort = bufferSizeShort / 2;
		// bufferSizeShort = 512;
		buffer = new short[MAX_FRAMEBUF][bufferSizeShort];
		buffer2 = new short[bufferSizeShort];
		frame_count = 0;

		// ------------------------------------------------------------------------------------------------------//
	}

	class ReadWave extends Thread {
		ReadWave() {
			setName("AudioIn");
		}

		public void run() {
			Thread_flag = true;
			while (rec_flag) {
				audioRecord.read(buffer[buf_wpos], 0, bufferSizeShort);
				if (frame_count < MAX_FRAMEBUF)
					frame_count++;
				buf_wpos++;
				if (buf_wpos == MAX_FRAMEBUF)
					buf_wpos = 0;
				buf_use++;
			}
			Thread_flag = false;
		}
	}

	public int Start() {

		buf_wpos = 0;
		buf_use = 0;
		buf_rpos = 0;
		rec_flag = true;
		audioRecord.startRecording();
		Thread thrw = new ReadWave();
		thrw.start();
		audiorec = true;
		return 0;
	}

	public int Stop() {
		rec_flag = false;
		audiorec = false;
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (Thread_flag) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		audioRecord.stop();
		buf_wpos = 0;
		buf_use = 0;
		buf_rpos = 0;

		return 0;
	}

	public int getBuf() {

		int ret = -1;

		if (buf_use != 0) {
			ret = buf_rpos;
			buf_rpos++;
			buf_use--;
			if (buf_rpos == MAX_FRAMEBUF)
				buf_rpos = 0;
		}
		if (!audiorec)
			ret = -2;
		return ret;
	}

	public int Release() {
		audioRecord.release();
		return 0;
	}

	public int back_buf(int n_frame) {
		if (n_frame > MAX_FRAMEBUF)
			return -1;
		if (frame_count < n_frame)
			return -1;
		if (buf_use < MAX_FRAMEBUF - n_frame) {
			if (buf_rpos < n_frame)
				buf_rpos = buf_rpos - n_frame + MAX_FRAMEBUF;
			else
				buf_rpos = buf_rpos - n_frame;
			buf_use = buf_use + n_frame;
			if (buf_rpos < 0)
				buf_rpos = MAX_FRAMEBUF - buf_rpos;
		}
		return 0;
	}

}
