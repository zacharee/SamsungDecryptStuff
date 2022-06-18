import jssc.SerialPort
import jssc.SerialPortList
import kotlinx.coroutines.*

object CSCChanger {
    const val TARGET_CSC = "XAA"

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            launch {
                sendCommands()
            }
        }
    }

    suspend fun SerialPort.writeAtCommand(command: String, timeoutMs: Long = 6000L): List<String> {
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
                        lines.add(read)
                    }

                    if (read.contains("\nOK") || read.contains("\nERROR") || read.contains(":OK") || read.contains("\r\nOK\r\n")) {
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

            lines
        }
    }

    suspend fun sendCommands() {
        val port = findPort()
        val opened = port.openPort()

        println("Found port ${port.portName}")

        if (!opened) {
            println("Failed to open")
        } else {
            port.setDTR(true)
            port.setRTS(true)
            println(port.writeAtCommand("AT+DEVCONINFO"))
            println(port.writeAtCommand("AT+SWATD=0"))
            println(port.writeAtCommand("AT+ACTIVATE=0,0,0"))
            println(port.writeAtCommand("AT+SWATD=1"))
            println(port.writeAtCommand("AT+PRECONFG=2,${TARGET_CSC}"))
            println(port.writeAtCommand("AT+CFUN=1,1"))
        }
    }

    suspend fun findPort(): SerialPort {
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

                it.writeAtCommand("AT+DEVCONINFO").any { line -> line.contains("OK" )}.also { _ -> it.closePort() }
            }
        }
    }
}