package com.xplore.paymobile.util

import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.BundleCompat
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class FragmentArgumentDelegate<T : Any?> : ReadWriteProperty<Fragment, T?> {

    var value: T? = null

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
        if (value == null) {
            val args = thisRef.arguments

            @Suppress("UNCHECKED_CAST")
            val v = args?.get(property.name) as? T

            value = v
        }

        return value
    }

    override operator fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        var args = thisRef.arguments
        if (args == null) {
            args = Bundle()
            thisRef.arguments = args
        }

        val key = property.name

        with(args) {

            when (value) {
                is Boolean -> putBoolean(key, value)
                is Byte -> putByte(key, value)
                is Char -> putChar(key, value)
                is Short -> putShort(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Double -> putDouble(key, value)
                is String -> putString(key, value)
                is CharSequence -> putCharSequence(key, value)
                is java.io.Serializable -> putSerializable(key, value)
                is BooleanArray -> putBooleanArray(key, value)
                is ByteArray -> putByteArray(key, value)
                is ShortArray -> putShortArray(key, value)
                is CharArray -> putCharArray(key, value)
                is IntArray -> putIntArray(key, value)
                is LongArray -> putLongArray(key, value)
                is FloatArray -> putFloatArray(key, value)
                is DoubleArray -> putDoubleArray(key, value)
                (value as? Array<*>)?.isArrayOf<String>() -> {
                    @Suppress("UNCHECKED_CAST")
                    putStringArray(key, (value as Array<String>))
                }
                (value as? Array<*>)?.isArrayOf<CharSequence>() -> {
                    @Suppress("UNCHECKED_CAST")
                    putCharSequenceArray(key, (value as Array<CharSequence>))
                }
                is Bundle -> putBundle(key, value)
                is Binder -> BundleCompat.putBinder(args, key, value)
                is Parcelable -> {
                    try {
                        putParcelable(key, value)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        putParcelable(key, value)
                    }
                }
                else -> throw IllegalStateException("Type $value of property ${property.name} is not supported")
            }
        }
    }
}

inline fun <reified T : Any?> Fragment.bundle(): ReadWriteProperty<Fragment, T?> = FragmentArgumentDelegate()