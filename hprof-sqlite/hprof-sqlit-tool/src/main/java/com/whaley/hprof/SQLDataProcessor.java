package com.whaley.hprof;

import static com.badoo.hprof.library.util.StreamUtil.skip;

import com.badoo.hprof.library.HprofReader;
import com.badoo.hprof.library.Tag;
import com.badoo.hprof.library.heap.HeapDumpReader;
import com.badoo.hprof.library.heap.HeapTag;
import com.badoo.hprof.library.heap.processor.HeapDumpDiscardProcessor;
import com.badoo.hprof.library.model.*;
import com.badoo.hprof.library.processor.DiscardProcessor;
import com.whaley.hprof.sqlitemanager.SqliteManager;
import com.whaley.hprof.sqlitemanager.Utils;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by hc on 2016/5/23.
 */
public class SQLDataProcessor extends DiscardProcessor {
	int id = 0;
	int objId = 0;
	private Map<Integer, ClassDefinition> classes = new HashMap<Integer, ClassDefinition>();
	private HeapDumpDiscardProcessor heapDumpProcessor = new HeapDumpDiscardProcessor() {

		@Override
		public void onHeapRecord(int tag, @Nonnull HeapDumpReader reader)
				throws IOException {
			int id;
			switch (tag) {
			case HeapTag.CLASS_DUMP: {
				ClassDefinition classDefinition = reader
						.readClassDumpRecord(classes);
				if (classDefinition != null) {
					SqliteManager.getInstance().insertHeapData(
							HeapTag.CLASS_DUMP, classDefinition);
				}
				break;
			}
			case HeapTag.INSTANCE_DUMP: {
				final Instance instance = reader.readInstanceDump();
				// System.out.print(instance.getStackTraceSerialId()+"\n");
				SqliteManager.getInstance().insertHeapData(
						HeapTag.INSTANCE_DUMP, instance);
				break;
			}
			case HeapTag.OBJECT_ARRAY_DUMP: {
				ObjectArray array = reader.readObjectArray();
				// objArrays.put(array.getObjectId(), array);
				SqliteManager.getInstance().insertHeapData(
						HeapTag.OBJECT_ARRAY_DUMP, array);
				break;
			}
			case HeapTag.PRIMITIVE_ARRAY_DUMP: {
				PrimitiveArray array = reader.readPrimitiveArray();
				// primitiveArrays.put(array.getObjectId(), array);
				SqliteManager.getInstance().insertHeapData(
						HeapTag.PRIMITIVE_ARRAY_DUMP, array);
				break;
			}
			case HeapTag.ROOT_UNKNOWN:
				// System.out.print("ROOT_UNKNOWN"+"\n");
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_UNKNOWN,
						reader.readUnKnowOrStickOrMonitor());
				break;
			case HeapTag.ROOT_JNI_GLOBAL:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_JNI_GLOBAL, reader.readGlobalModel());
				break;
			case HeapTag.ROOT_JNI_LOCAL:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_JNI_LOCAL, reader.readLocalModel());
				break;
			case HeapTag.ROOT_JAVA_FRAME:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_JAVA_FRAME, reader.readJavaFrameModel());
				break;
			case HeapTag.ROOT_NATIVE_STACK:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_NATIVE_STACK,
						reader.readNativeStack());
				break;
			case HeapTag.ROOT_STICKY_CLASS:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_STICKY_CLASS,
						reader.readUnKnowOrStickOrMonitor());
				break;
			case HeapTag.ROOT_THREAD_BLOCK:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_THREAD_BLOCK, reader.readThreadBlock());
				break;
			case HeapTag.ROOT_MONITOR_USED:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_MONITOR_USED,
						reader.readUnKnowOrStickOrMonitor());
				break;
			case HeapTag.ROOT_THREAD_OBJECT:
				SqliteManager.getInstance().insertHeapData(
						HeapTag.ROOT_THREAD_OBJECT,
						reader.readThreadObjectModel());
				break;
			default:
				super.onHeapRecord(tag, reader);
			}
		}
	};

	@Override
	public void onRecord(int tag, int timestamp, int length, HprofReader reader)
			throws IOException {
		// SqliteManager.getInstance().insertData(length);
		switch (tag) {
		case Tag.STRING: {
			HprofString string = reader.readStringRecord(length, timestamp);
			// strings.put(string.getId(), string);
			// if (string.getValue().contains("com.moretv.baseCtrl.Util"))
			// id = string.getId();
			SqliteManager.getInstance().insertData(Tag.STRING, string);
			break;
		}
		case Tag.LOAD_CLASS: {
			ClassDefinition cls = reader.readLoadClassRecord();
			// cls.set
			// if (cls.getNameStringId()==id){
			// objId = cls.getObjectId();
			// }
			classes.put(cls.getObjectId(), cls);
			SqliteManager.getInstance().insertData(Tag.LOAD_CLASS, cls);
			break;
		}
		case Tag.HEAP_DUMP:
			System.out.print("HEAP_DUMP");
		case Tag.HEAP_DUMP_SEGMENT: {
			int id = UUID.randomUUID().hashCode();
			// SqliteManager.getInstance().insertHeapData(id,length);
			HeapDumpReader heapReader = new HeapDumpReader(
					reader.getInputStream(), length, heapDumpProcessor, id);
			while (heapReader.hasNext()) {
				heapReader.next();
			}
			break;
		}
		case Tag.STACK_FRAME:
			StackFrame stackFrame = reader.readStackFrameRecord();
			// System.out.print(stackFrame.getId() + "STACK_FRAME"+"\n");
			break;
		case Tag.STACK_TRACE:
			StackTrace stackTrace = reader.readStackTraceRecord();
			// System.out.print(stackTrace.getFrameIds() + "STACK_TRACE"+"\n");
			break;
		case Tag.ALLOC_SITES:
			System.out.print("ALLOC_SITES:" + "\n");
			break;
		case Tag.HEAP_SUMMARY:
			System.out.print("liveByte:" + "liveInstance:" + "\n");
			HeapSummary summary = reader.readHeapSummary();

			break;
		case Tag.START_THREAD:
			ThreadField threadField = reader.readThreadRecord();
			// System.out.print(threadField.getId()+"START_THREAD");
			break;
		default:
			super.onRecord(tag, timestamp, length, reader);
		}
	}

}
