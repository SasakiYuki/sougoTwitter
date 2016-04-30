package wacode.yuki.newontapusha.Utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by Yuki on 2016/03/15.
 */
object PrefUtils {

    private var pref: SharedPreferences? = null

    fun getPref(context: Context): SharedPreferences? {
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(context)
        }
        return pref
    }

    fun put(context: Context, name: String, value: Boolean) {
        val edit = getPref(context)!!.edit()
        edit.putBoolean(name, value)
        edit.apply()
    }

    fun put(context: Context, name: String, value: String) {
        val edit = getPref(context)!!.edit()
        edit.putString(name, value)
        edit.apply()
    }

    fun put(context: Context, name: String, value: Int) {
        val edit = getPref(context)!!.edit()
        edit.putInt(name, value)
        edit.apply()
    }

    fun contains(context: Context, name: String): Boolean {
        return getPref(context)!!.contains(name)
    }

    fun remove(context: Context, name: String) {
        val edit = getPref(context)!!.edit()
        edit.remove(name)
        edit.apply()
    }

    operator fun get(context: Context, name: String, defaultValue: String): String {
        return getPref(context)!!.getString(name, defaultValue)
    }

    operator fun get(context: Context, name: String, defaultValue: Boolean): Boolean {
        return getPref(context)!!.getBoolean(name, defaultValue)
    }

    operator fun get(context: Context,name: String,defaultValue: Int):Int{
        return getPref(context)!!.getInt(name,defaultValue)
    }
}
