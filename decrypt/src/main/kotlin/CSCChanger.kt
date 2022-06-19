import jssc.SerialPort
import jssc.SerialPortList
import kotlinx.coroutines.*

object CSCChanger {
    private const val TARGET_CSC = "XAA"
    private val commands = arrayOf(
        "AT+SWATD=0",
        "AT+ACTIVATE=0,0,0",
        "AT+SWATD=1",
        "AT+PRECONFG=2,${TARGET_CSC}",
        "AT+CFUN=1,1"
    )

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            launch {
                sendCommands()
            }
        }
    }

    private suspend fun sendCommands() {
        val port = findPort()
        val opened = port.openPort()

        println("Found port ${port.portName}")

        if (!opened) {
            println("Failed to open")
        } else {
            port.setDTR(true)
            port.setRTS(true)

            commands.forEach {
                print("${port.writeAtCommand(it)}\n")
            }

            // Has to run twice for some reason
            commands.forEach {
                print("${port.writeAtCommand(it)}\n")
            }
        }
    }

    private suspend fun SerialPort.writeAtCommand(command: String, timeoutMs: Long = 6000L): String {
        val startTime = System.currentTimeMillis()
        println("Command: $command")

        this.flowControlMode = SerialPort.FLOWCONTROL_NONE

        return coroutineScope {
            val write = async(Dispatchers.IO) { writeString("$command\r\n\n") }
            val writeResult = withTimeoutOrNull(6000) { write.await() }
            val lines = ArrayList<String>()

            if (writeResult == true) {
                while (true) {
                    val read = readString() ?: ""

                    if (read.isNotBlank()) {
                        lines.add(read.replace("\r", "").replace("\n", ""))
                    }

                    if (
                        read.contains("\nOK") ||
                        read.contains("\nERROR") ||
                        read.contains(":OK") ||
                        read.contains("\r\nOK\r\n")
                    ) {
                        break
                    }

                    if ((System.currentTimeMillis() - startTime) > timeoutMs) {
                        lines.add("TIMEOUT")
                        break
                    }
                }
            } else {
                purgePort(SerialPort.PURGE_TXABORT)
                purgePort(SerialPort.PURGE_TXCLEAR)
            }

            lines.joinToString("\n")
        }
    }

    private suspend fun findPort(): SerialPort {
        return SerialPortList.getPortNames().map { SerialPort(it) }.first {
            val p = try {
                it.openPort()
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            println(it.portName)
            if (!p) {
                println("couldn't open")
                false
            } else {
                if (!it.setRTS(true)) {
                    println("Couldn't set RTS")
                }
                if (!it.setDTR(true)) {
                    println("Couldn't set DTR")
                }

                it.writeAtCommand("AT").contains("OK").also { _ -> it.closePort() }
            }
        }
    }
}
