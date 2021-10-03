import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.RSAPrivateKeySpec
import javax.crypto.Cipher


object Decrypter {
    fun getKey(): PrivateKey {
        val key1Stream = javaClass.getResourceAsStream("mod_pri_key.txt")
        val key2Stream = javaClass.getResourceAsStream("exp_pri_key.txt")

        val key1Bytes = ByteArray(key1Stream.available())
        val key2Bytes = ByteArray(key2Stream.available())

        key1Stream.read(key1Bytes)
        key2Stream.read(key2Bytes)

        val key1Int = BigInteger(key1Bytes)
        val key2Int = BigInteger(key2Bytes)

        val spec = RSAPrivateKeySpec(key1Int, key2Int)
        val factory = KeyFactory.getInstance("RSA")

        return factory.generatePrivate(spec)
    }

    fun decrypt(bArr: ByteArray, privateKey: PrivateKey?): ByteArray? {
        return try {
            val instance = Cipher.getInstance("RSA", "BC")
            instance.init(2, privateKey)
            var i = 64
            var length = bArr.size.toLong()
            val byteArrayOutputStream = ByteArrayOutputStream()
            var j: Long = 0
            while (true) {
                if (j >= length) {
                    break
                }
                var j2 = length - j
                val j3 = i.toLong()
                if (j2 > j3) {
                    j2 = j3
                }
                val bArr3 = ByteArray(j2.toInt())
                val j4 = length
                for (j5 in 0 until j2) {
                    bArr3[j5.toInt()] = bArr[(j5 + j).toInt()]
                }
                j += j2
                val doFinal = instance.doFinal(bArr3) ?: break
                byteArrayOutputStream.write(doFinal)
                if (j2 < j3) {
                    break
                }
                length = j4
                i = 64
            }
            val byteArray = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()
            byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}