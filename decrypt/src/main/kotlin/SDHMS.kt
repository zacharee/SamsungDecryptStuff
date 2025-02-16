fun main() {
    val arr = intArrayOf(25, 21, 23, 84, 9, 31, 25, 84, 17, 31, 3, 9, 14, 8, 19, 20, 29, 23, 27, 19, 20)
    val dec = m2626C(arr)

    println(dec)
}

fun m2626C(iArr: IntArray): String {
    val sb = StringBuilder()
    for (i3 in iArr) {
        sb.append((i3 xor 122).toChar())
    }
    return sb.toString()
}
