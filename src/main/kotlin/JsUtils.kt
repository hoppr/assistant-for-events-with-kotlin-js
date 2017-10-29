import kotlin.js.Math


// JS generic utils

fun also(obj: dynamic, onReady: (dynamic) -> dynamic) = obj.unsafeCast<Any>().apply { onReady(asDynamic()) }.asDynamic()


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
        val randomIdx = Math.floor(Math.random() * l.size)
        val item = l.removeAt(randomIdx)
        shuffle.add(item)
    }
    return shuffle
}


// Firebase Database Realtime

fun fireReadOnce(ref: dynamic, onReady: (dynamic) -> dynamic) = ref.once("value") { snap -> onReady(snap.`val`()) }

fun firePush(ref: dynamic, data: dynamic, onPush: ((dynamic) -> dynamic) = {}) = ref.push(data).then(onPush)
