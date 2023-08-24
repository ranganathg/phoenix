/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.phoenix.schema.types;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SortOrder;
import org.apache.phoenix.util.ByteUtil;

import java.sql.Types;

import org.bson.RawBsonDocument;

/**
 * <p>
 * A Phoenix data type to represent JSON. The json data type stores JSON in BSON format as used by
 * mongodb. We use the mongodb libraries to store and retrieve the JSON object using the JSON
 * functions.
 * <p>
 * JSON data types are for storing JSON (JavaScript Object Notation) data, as specified in RFC 7159.
 * Such data can also be stored as text, but the JSON data types have the advantage of enforcing
 * that each stored value is valid according to the JSON rules.
 */
public class PJson extends PVarbinary{

    public static final PJson INSTANCE = new PJson();

    private PJson() {
        super("JSON", Types.VARBINARY, byte[].class, null, 48);
    }

    @Override
    public boolean canBePrimaryKey() {
        return false;
    }

    @Override
    public boolean isEqualitySupported() {
        return false;
    }

    @Override
    public int toBytes(Object object, byte[] bytes, int offset) {
        if (object == null) {
            return 0;
        }
        byte[] b = toBytes(object);
        System.arraycopy(b, 0, bytes, offset, b.length);
        return b.length;

    }

    @Override
    public byte[] toBytes(Object object)  {
        if (object == null) {
            return ByteUtil.EMPTY_BYTE_ARRAY;
        }
        return Bytes.toBytes(((RawBsonDocument)object).getByteBuffer().asNIO());
    }

    @Override
    public Object toObject(byte[] bytes, int offset, int length,
                           @SuppressWarnings("rawtypes") PDataType actualType, SortOrder sortOrder,
                           Integer maxLength, Integer scale) {
        if (length == 0) {
            return null;
        }
        return new RawBsonDocument(bytes, offset, length);
    }

    @Override
    public Object toObject(Object object, @SuppressWarnings("rawtypes") PDataType actualType) {
        if (object == null) {
            return null;
        }
        if (equalsAny(actualType, PVarchar.INSTANCE)) {
            return RawBsonDocument.parse((String)object);
        }
        return object;
    }

    @Override
    public Object toObject(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        return RawBsonDocument.parse(value);
    }

    @Override
    public boolean isCoercibleTo(@SuppressWarnings("rawtypes") PDataType targetType) {
        return equalsAny(targetType, this, PBinary.INSTANCE, PVarbinary.INSTANCE);

    }

    @Override
    public int estimateByteSize(Object o) {
        RawBsonDocument rawBSON = (RawBsonDocument)o;
        return rawBSON.size();
    }

    @Override
    public Integer getByteSize() {
        return null;
    }

    @Override
    public boolean isBytesComparableWith(@SuppressWarnings("rawtypes") PDataType otherType) {
        return otherType == PVarbinary.INSTANCE ;
    }

    @Override
    public Object getSampleValue(Integer maxLength, Integer arrayLength) {
        String json = "{a : 1}";
        return this.toObject(json);
    }
}
