package com.badoo.hprof.library.processor;

import com.badoo.hprof.library.HprofProcessor;
import com.badoo.hprof.library.HprofReader;

import java.io.IOException;

import static com.badoo.hprof.library.util.StreamUtil.*;

/**
 * A HprofProcessor implementation that reads and discards all records.
 * <p/>
 * Created by Erik Andre on 13/07/2014.
 */
public abstract class DiscardProcessor implements HprofProcessor {

	@Override
	public void onHeader(String text, int idSize, int timeHigh,
			int timeLow) throws IOException {
	}

	@Override
	public void onRecord(int tag, int timestamp, int length,
			HprofReader reader) throws IOException {
		skip(reader.getInputStream(), length);
	}
}
