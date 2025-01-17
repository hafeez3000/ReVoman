@file:JvmName("JsonWriterUtils")

package com.salesforce.revoman.input.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonWriter
import java.util.function.Consumer
import org.springframework.beans.BeanUtils

fun <T> objW(name: String, obj: T, writer: JsonWriter, block: Consumer<T>): Unit =
  with(writer) {
    name(name)
    objW(obj, writer, block)
  }

fun <T> objW(obj: T, writer: JsonWriter, block: Consumer<T>): Unit =
  with(writer) {
    if (obj == null) {
      nullValue()
    } else {
      beginObject()
      block.accept(obj)
      endObject()
    }
  }

fun string(name: String, value: String?, writer: JsonWriter): Unit =
  with(writer) {
    name(name)
    value?.also(::value) ?: nullValue()
  }

fun JsonWriter.string(name: String, value: String?) {
  name(name)
  value?.also(::value) ?: nullValue()
}

fun bool(name: String, value: Boolean?, writer: JsonWriter): Unit =
  with(writer) {
    name(name)
    value?.also(::value) ?: nullValue()
  }

fun JsonWriter.bool(name: String, value: Boolean?) {
  name(name)
  value?.also(::value) ?: nullValue()
}

fun integer(name: String, value: Int?, writer: JsonWriter): Unit =
  with(writer) {
    name(name)
    value?.also(::value) ?: nullValue()
  }

fun JsonWriter.integer(name: String, value: Int?) {
  name(name)
  value?.also(::value) ?: nullValue()
}

fun doubl(name: String, value: Double?, writer: JsonWriter): Unit =
  with(writer) {
    name(name)
    value?.also(::value) ?: nullValue()
  }

fun JsonWriter.doubl(name: String, value: Double?) {
  name(name)
  value?.also(::value) ?: nullValue()
}

fun lng(name: String, value: Long?, writer: JsonWriter): Unit =
  with(writer) {
    name(name)
    value?.also(::value) ?: nullValue()
  }

fun JsonWriter.lng(name: String, value: Long?) {
  name(name)
  value?.also(::value) ?: nullValue()
}

fun <T> listW(name: String, list: List<T>?, writer: JsonWriter, block: Consumer<T>): JsonWriter =
  with(writer) {
    name(name)
    listW(list, this, block)
  }

fun <T> listW(list: List<T>?, writer: JsonWriter, block: Consumer<T>): JsonWriter =
  with(writer) {
    when (list) {
      null -> nullValue()
      else -> {
        beginArray()
        list.forEach(block)
        endArray()
      }
    }
  }

fun <T> JsonWriter.writeProps(
  type: Class<T>,
  bean: T,
  excludePropTypes: Set<Class<*>>,
  dynamicJsonAdapter: JsonAdapter<Any>
) =
  BeanUtils.getPropertyDescriptors(type)
    .asSequence()
    .filterNot {
      it.propertyType.name == "java.lang.Class" || excludePropTypes.contains(it.propertyType)
    }
    .forEach {
      name(it.name)
      dynamicJsonAdapter.toJson(this, it.readMethod(bean))
    }
