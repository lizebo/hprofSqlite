package com.badoo.hprof.library.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.badoo.hprof.library.util.StreamUtil.readByte;
import static com.badoo.hprof.library.util.StreamUtil.readDouble;
import static com.badoo.hprof.library.util.StreamUtil.readFloat;
import static com.badoo.hprof.library.util.StreamUtil.readInt;
import static com.badoo.hprof.library.util.StreamUtil.readLong;
import static com.badoo.hprof.library.util.StreamUtil.readShort;
import static com.badoo.hprof.library.util.StreamUtil.skip;

/**
 * Class containing the data of a class instance dump (INSTANCE_DUMP) heap record.
 * <p/>
 * Created by Erik Andre on 17/07/2014.
 */
@SuppressWarnings({"RedundantIfStatement", "unused"})
public class Instance extends Record {

    private int objectId;
    private int stackTraceSerialId;
    private int classId;
    private int length;
//    private int heapId;
    private byte[] instanceFieldData;

    public Instance(int objectId, int stackTraceSerialId, int classId,int length, @Nonnull byte[] instanceFieldData) {
        this.objectId = objectId;
        this.stackTraceSerialId = stackTraceSerialId;
        this.classId = classId;
        this.length = length;
        this.instanceFieldData = instanceFieldData;
//        this.heapId = heapId;
    }

    public int getLength() {
        return length;
    }

//    public int getHeapId() {
//        return heapId;
//    }
//
//    public void setHeapId(int heapId) {
//        this.heapId = heapId;
//    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getStackTraceSerialId() {
        return stackTraceSerialId;
    }

    public void setStackTraceSerialId(int stackTraceSerialId) {
        this.stackTraceSerialId = stackTraceSerialId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    @Nonnull
    public byte[] getInstanceFieldData() {
        return instanceFieldData;
    }

    public void setInstanceFieldData(byte[] instanceFieldData) {
        this.instanceFieldData = instanceFieldData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Instance instance = (Instance) o;

        if (classId != instance.classId) return false;
        if (objectId != instance.objectId) return false;
        if (stackTraceSerialId != instance.stackTraceSerialId) return false;
        if (!Arrays.equals(instanceFieldData, instance.instanceFieldData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = objectId;
        result = 31 * result + stackTraceSerialId;
        result = 31 * result + classId;
        result = 31 * result + Arrays.hashCode(instanceFieldData);
        return result;
    }

    /**
     * Returns the value of an Object field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public int getObjectField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.OBJECT) {
            throw new IllegalArgumentException("Field is not of type OBJECT");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readInt(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an byte field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public int getByteField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.BYTE) {
            throw new IllegalArgumentException("Field is not of type BYTE");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readByte(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an int field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public int getIntField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.INT) {
            throw new IllegalArgumentException("Field is not of type INT");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readInt(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an short field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public int getShortField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.SHORT) {
            throw new IllegalArgumentException("Field is not of type SHORT");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readShort(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an char field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public char getCharField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.INT) {
            throw new IllegalArgumentException("Field is not of type INT");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return (char) readShort(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an long field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public long getLongField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.LONG) {
            throw new IllegalArgumentException("Field is not of type LONG");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readLong(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an boolean field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public boolean getBooleanField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.BOOLEAN) {
            throw new IllegalArgumentException("Field is not of type BOOLEAN");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readByte(in) != 0;
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an double field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public double getDoubleField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.DOUBLE) {
            throw new IllegalArgumentException("Field is not of type DOUBLE");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readDouble(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }

    /**
     * Returns the value of an float field in this instance
     *
     * @param field   the field to read
     * @param classes map containing all classes (or at least the ones between this class and the root)
     * @return the field value
     */
    public float getFloatField(InstanceField field, Map<Integer, ClassDefinition> classes) throws IOException {
        if (field.getType() != BasicType.FLOAT) {
            throw new IllegalArgumentException("Field is not of type FLOAT");
        }
        // Iterate over all the instance fields until we find one that is matching
        ByteArrayInputStream in = new ByteArrayInputStream(instanceFieldData);
        ClassDefinition currentClass = classes.get(classId);
        while (currentClass != null) {
            for (InstanceField currentField : currentClass.getInstanceFields()) {
                if (currentField == field) { // This is the one we are looking for
                    return readFloat(in);
                }
                else {
                    skip(in, currentField.getType().size);
                }
            }
            currentClass = classes.get(currentClass.getSuperClassObjectId());
        }
        throw new IllegalStateException("Failed to find field");
    }
}
