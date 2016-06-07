package com.badoo.hprof.library;

import com.badoo.hprof.library.model.*;
import com.badoo.hprof.library.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.naming.OperationNotSupportedException;

import static com.badoo.hprof.library.util.StreamUtil.readByte;
import static com.badoo.hprof.library.util.StreamUtil.readInt;
import static com.badoo.hprof.library.util.StreamUtil.readString;

/**
 * Class for reading hprof files.
 * <p/>
 * Based on documentation from: https://java.net/downloads/heap-snapshot/hprof-binary-format.html
 * <p/>
 * <p></p><h3>Usage</h3></p>
 * <p>
 * Create an instance of the class and give it an input stream that is positioned at the first byte of the hprof file and a HprofProcessor
 * instance to receive callbacks when records are read. To read the hprof records, keep calling next() while hasNext() returns true.
 * </p>
 * <pre>
 *     HprofReader reader = new HprofReader(in, processor);
 *     while (reader.hasNext()) {
 *         reader.next();
 *     }
 * </pre>
 * <p/>
 * Created by Erik Andre on 12/07/2014.
 */
public class HprofReader {

    private final InputStream in;
    private final HprofProcessor processor;
    private int readCount;
    private int nextTag;

    public HprofReader(@Nonnull InputStream in, @Nonnull HprofProcessor processor) {
        this.in = in;
        this.processor = processor;
    }

    /**
     * Returns true if there is more data to be read (You can call next())
     *
     * @return True if there is more data to be read.
     * @throws IOException
     */
    public boolean hasNext() throws IOException {
        return nextTag != -1;
    }

    /**
     * Read the next record. This will trigger a callback to the processor.
     *
     * @throws IOException
     */
    public void next() throws IOException {
        if (readCount == 0) { // The header is always assumed to come first
            readHprofFileHeader();
        }
        else {
            readRecord();
        }
        readCount++;
        // Check if there are more records
        nextTag = readByte(in);
    }

    /**
     * Returns the InputStream that this HprofReader is reading its data from.
     *
     * @return The InputStream
     */
    @Nonnull
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Read a LOAD_CLASS record and create a class definition for the loaded class.
     *
     * @return A ClassDefinition with some fields filled in (Serial number, class object id, stack trace serial & class name string id)
     * @throws IOException
     */
    @Nonnull
    public ClassDefinition readLoadClassRecord() throws IOException {
        int serialNumber = readInt(in);
        int classObjectId = readInt(in);
        int stackTraceSerial = readInt(in);
        int classNameStringId = readInt(in);
        ClassDefinition cls = new ClassDefinition();
        cls.setSerialNumber(serialNumber);
        cls.setObjectId(classObjectId);
        cls.setStackTraceSerial(stackTraceSerial);
        cls.setNameStringId(classNameStringId);
        return cls;
    }

    /**
     * Read a STRING record and create a HprofString based on its contents.
     *
     * @param recordLength Length of the record, part of the record header provided to HprofProcessor.onRecord()
     * @param timestamp    Timestamp of the record, part of the record header provided to HprofProcessor.onRecord()
     * @return A HprofString containing the string data
     * @throws IOException
     */
    @Nonnull
    public HprofString readStringRecord(int recordLength, int timestamp) throws IOException {
        int id = readInt(in);
        String string = readString(in, recordLength - 4);
        return new HprofString(id, string, timestamp);
    }

    /**
     *
     * @return
     * @throws IOException
     */

    @Nonnull
    public StackFrame readStackFrameRecord() throws IOException{
        int id = readInt(in);
        int methodNameId = readInt(in);
        int methodSignatureID = readInt(in);
        int sourceNameID = readInt(in);
        int classSerialNumber = readInt(in);
        int state = readInt(in);
        StackFrame stackFrame = new StackFrame();
        stackFrame.setId(id);
        stackFrame.setClassSerialNumber(classSerialNumber);
        stackFrame.setMethodNameId(methodNameId);
        stackFrame.setMethodSignatureID(methodSignatureID);
        stackFrame.setSourceNameID(sourceNameID);
        stackFrame.setState(state);
        return stackFrame;
    }


    /**
     *
     * @return
     * @throws IOException
     */
    @Nonnull
    public StackTrace readStackTraceRecord() throws IOException{
        int stackTraceSerialNumber = readInt(in);
        int threadSerialNumber = readInt(in);
        int frameNum = readInt(in);
        ArrayList frameIds = new ArrayList();
        for (int i = 0;i<frameNum;i++)
            frameIds.add(readInt(in));
        StackTrace stackTrace = new StackTrace();
        stackTrace.setFrameIds(frameIds);
        stackTrace.setStackTraceSerialNumber(stackTraceSerialNumber);
        stackTrace.setThreadSerialNumber(threadSerialNumber);
        return stackTrace;
    }

    public ThreadField readThreadRecord() throws IOException{
        int serialNumber = readInt(in);
        int id = readInt(in);
        int stackTraceSerialNumber = readInt(in);
        int nameStringId = readInt(in);
        int groupNameId = readInt(in);
        int parentGroupNameId = readInt(in);
        ThreadField threadField = new ThreadField();
        threadField.setStackTraceSerialNumber(stackTraceSerialNumber);
        threadField.setGroupNameId(groupNameId);
        threadField.setParentGroupNameId(parentGroupNameId);
        threadField.setId(id);
        threadField.setNameStringId(nameStringId);
        threadField.setSerialNumber(serialNumber);
        return threadField;
    }

    public HeapSummary readHeapSummary() throws IOException{
        int liveByteNum  = readInt(in);
        int liveInstanceNum  = readInt(in);
        int allocByteNum  = readInt(in);
        int allocInstanceNum  = readInt(in);
        HeapSummary  heapSummary = new HeapSummary();
        heapSummary.setLiveByteNum(liveByteNum);
        heapSummary.setLiveInstanceNum(liveInstanceNum);
        heapSummary.setAllocByteNum(allocByteNum);
        heapSummary.setAllocInstanceNum(allocInstanceNum);
        return heapSummary;
    }

    private void readRecord() throws IOException {
        int tagValue = nextTag;
        int time = readInt(in);
        int size = readInt(in);
        processor.onRecord(tagValue, time, size, this);
    }

    private void readHprofFileHeader() throws IOException {
        String text = StreamUtil.readNullTerminatedString(in);
        int idSize = readInt(in);
        if (idSize != 4) { // Currently only 4-byte ids are supported
            throw new UnsupportedOperationException("Only hprof files with 4-byte ids can be read! This file has ids of " + idSize + " bytes");
        }
        int timeHigh = readInt(in);
        int timeLow = readInt(in);
        processor.onHeader(text, idSize, timeHigh, timeLow);
    }

}
