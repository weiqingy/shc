/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources.hbase.types

import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.execution.SparkSqlSerializer
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String
import org.apache.spark.sql.execution.datasources.hbase._

class PrimitiveType(f: Field) extends SHCDataType {

  def bytesToColumn(src: HBaseType): Any = {
    f.dt match {
      case BooleanType => toBoolean(src)
      case ByteType => src(0)
      case DoubleType => Bytes.toDouble(src)
      case FloatType => Bytes.toFloat(src)
      case IntegerType => Bytes.toInt(src)
      case LongType => Bytes.toLong(src)
      case ShortType => Bytes.toShort(src)
      case StringType => toUTF8String(src, src.length)
      case BinaryType => src
      case _ => SparkSqlSerializer.deserialize[Any](src)
    }
  }

  def toBytes(input: Any): Array[Byte] = {
    input match {
      case data: Boolean => Bytes.toBytes(data)
      case data: Byte => Array(data)
      case data: Array[Byte] => data
      case data: Double => Bytes.toBytes(data)
      case data: Float => Bytes.toBytes(data)
      case data: Int => Bytes.toBytes(data)
      case data: Long => Bytes.toBytes(data)
      case data: Short => Bytes.toBytes(data)
      case data: UTF8String => data.getBytes
      case data: String => Bytes.toBytes(data)
      case _ => throw new Exception(s"unsupported data type ${f.dt}")
    }
  }

  def bytesToCompositeKeyField(src: HBaseType, offset: Int, length: Int): Any = {
    f.dt match {
      case BooleanType => toBoolean(src, offset)
      case ByteType => src(offset)
      case DoubleType => Bytes.toDouble(src, offset)
      case FloatType => Bytes.toFloat(src, offset)
      case IntegerType => Bytes.toInt(src, offset)
      case LongType => Bytes.toLong(src, offset)
      case ShortType => Bytes.toShort(src, offset)
      case StringType => toUTF8String(src, length, offset)
      case BinaryType =>
        val newArray = new Array[Byte](length)
        System.arraycopy(src, offset, newArray, 0, length)
        newArray
      case _ => SparkSqlSerializer.deserialize[Any](src)
    }
  }

  private def toBoolean(input: HBaseType, offset: Int = 0): Boolean = {
    input(offset) != 0
  }

  private def toUTF8String(input: HBaseType, length: Int, offset: Int = 0): UTF8String = {
    UTF8String.fromBytes(input.slice(offset, offset + length))
  }
}
