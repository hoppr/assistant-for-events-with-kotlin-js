import kotlin.js.*
import kotlin.js.Math.random
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round


// JS generic utils

fun also(obj: dynamic, onReady: (dynamic) -> dynamic) = obj.unsafeCast<Any>().apply { onReady(asDynamic()) }.asDynamic()

fun getWeekNumber(date: Date) = eval("""
var d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
var dayNum = d.getUTCDay() || 7;
d.setUTCDate(d.getUTCDate() + 4 - dayNum);
var yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));
Math.ceil((((d - yearStart) / 86400000) + 1)/7);""").unsafeCast<Int>()

fun getDateOfWeek(week: Int, year: Int) = eval("""
var simple = new Date(year, 0, 1 + (week - 1) * 7);
var dow = simple.getDay();
var iso = simple;
if (dow <= 4) iso.setDate(simple.getDate() - simple.getDay() + 1);
else iso.setDate(simple.getDate() + 8 - simple.getDay());
iso;""").unsafeCast<Date>()

// Json

fun toJson(obj: dynamic) = JSON.stringify(obj) { key, value -> value ?: undefined }


// Arrays / Map

fun jsMap(init: (dynamic) -> Unit) = Any().apply { init(asDynamic()) }.asDynamic()

fun keys(obj: dynamic) = js("Object").keys(obj).unsafeCast<Array<String>>()

fun <T : Any> List<T>.shuffle() = shuffleIdx(size).map { this[it] }

fun shuffleIdx(max: Int): List<Int> {
    val l = (0 until max).toMutableList()
    val shuffle = ArrayList<Int>()
    while (l.isNotEmpty()) {
        val randomIdx = floor(random() * l.size).toInt()
        val item = l.removeAt(randomIdx)
        shuffle.add(item)
    }
    return shuffle
}

fun Double.oneDigit() = floor(this * 10) / 10


// Firebase Database Realtime

fun fireReadOnce(ref: dynamic, onReady: (dynamic) -> dynamic) = ref.once("value") { snap -> onReady(snap.`val`()) }

fun firePush(ref: dynamic, data: dynamic, onPush: ((dynamic) -> dynamic) = {}) = ref.push(data).then(onPush)
