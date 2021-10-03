import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val bytes = convertIStoByte(javaClass.getResourceAsStream("TMB_keystrings.dat"))
        val decrypted = Decrypter.decrypt(bytes, Decrypter.getKey())

        if (decrypted == null) {
            println("Decrypt failed")
        } else {
            println(String(decrypted))
        }
    }

    private fun convertIStoByte(inputStream: InputStream): ByteArray {
        val i: Int
        i = try {
            inputStream.available()
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bArr = ByteArray(i)
        while (true) {
            try {
                val read = inputStream.read(bArr)
                if (read == -1) {
                    break
                }
                byteArrayOutputStream.write(bArr, 0, read)
            } catch (e2: IOException) {
                e2.printStackTrace()
            }
        }
        return byteArrayOutputStream.toByteArray()
    }
}
