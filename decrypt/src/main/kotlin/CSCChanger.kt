import jssc.SerialPort
import jssc.SerialPortList
import kotlinx.coroutines.*
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import java.util.Scanner

object CSCChanger {
    //    private const val TARGET_CSC = "XAA"
    private val setup = arrayOf(
        "AT+KSTRINGB=0,3",
        "AT+DUMPCTRL=1,0",
        "AT+DEBUGLVC=0,5",
        "AT+SWATD=0",
        "AT+ACTIVATE=0,0,0",
        "AT+SWATD=1",
        "AT+DEBUGLVC=0,5",
    )
    private val commands = { csc: String ->
        arrayOf(
            "AT+PRECONFG=2,$csc",
            "AT+PRECONFG=1,0"
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgumentParsers.newFor("CSCChanger").build()
            .defaultHelp(true)
            .description("Change the CSC on your Samsung device.")

        parser.addArgument("--csc")
            .nargs(1)
            .required(false)
            .type(String::class.java)
            .help("The CSC to change to (e.g., XAA).")

        val csc = try {
            parser.parseArgs(args).getList<String>("csc").firstOrNull() ?: run {
                print("Enter CSC (e.g., XAA): ")
                Scanner(System.`in`).nextLine()
            }
        } catch (e: ArgumentParserException) {
            parser.handleError(e)
            return
        }

        runBlocking {
            launch {
                sendCommands(csc.uppercase())
            }
        }
    }

    private suspend fun sendCommands(csc: String) {
        val scanner = Scanner(System.`in`)

        if (!(csc.length == 3 && csc.contains(Regex("^[A-Z]+\$")))) {
            println("Provided CSC ($csc) is invalid!")
            return
        }

        val port = findPort() ?: run {
            println("No suitable port found! Dial *#0808# in the Samsung dialer and make sure \"RNDIS + DM + MODEM + ADB\" is selected.")
            return
        }
        val opened = port.openPort()

        println("Found port ${port.portName}")

        println()
        println("IMPORTANT: Open the Samsung Phone app and dial *#0*#. Once the debug screen appears, press Enter here to continue.")
        scanner.nextLine()

        if (!opened) {
            println("Failed to open")
        } else {
            port.setDTR(true)
            port.setRTS(true)

            setup.forEach {
                print("${port.writeAtCommand(it)}\n")
            }

            commands(csc).forEach {
                print("${port.writeAtCommand(it)}\n")
            }
        }

        println("Done! Please reboot your phone.")
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
                        val split = read.split("\r").flatMap { it.split("\n") }.filterNot { it.isBlank() }
                        lines.addAll(split)
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

    private suspend fun findPort(): SerialPort? {
        return SerialPortList.getPortNames()
            .filterNot { it.contains("Bluetooth", true) }
            .sortedBy {
                if (it.contains("modem", true)) 0 else 1
            }
            .also {
                println("Found possible ports: ${it.joinToString(", ")}")
            }
            .map { SerialPort(it) }.firstOrNull {
                val p = try {
                    it.openPort()
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
                println("Checking ${it.portName}")
                if (!p) {
                    println("Couldn't open ${it.portName}")
                    false
                } else {
                    if (!it.setRTS(true)) {
                        println("Couldn't set RTS for ${it.portName}")
                    }
                    if (!it.setDTR(true)) {
                        println("Couldn't set DTR for ${it.portName}")
                    }

                    it.writeAtCommand("AT").contains("OK").also { _ -> it.closePort() }
                }
            }
    }
}
