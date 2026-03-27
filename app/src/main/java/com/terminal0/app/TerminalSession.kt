package com.terminal0.app

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class TerminalSession {

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var errorReader: BufferedReader? = null

    var currentDirectory: String = "/data/data/com.terminal0.app"

    init {
        startShell()
    }

    private fun startShell() {
        try {
            val pb = ProcessBuilder("/system/bin/sh")
            pb.redirectErrorStream(false)
            pb.environment()["TERM"] = "xterm-256color"
            pb.environment()["HOME"] = "/data/data/com.terminal0.app"
            pb.environment()["USER"] = "terminal0"
            pb.environment()["SHELL"] = "/system/bin/sh"
            pb.environment()["PATH"] = "/sbin:/system/sbin:/system/bin:/system/xbin:/vendor/bin:/vendor/xbin"
            process = pb.start()
            writer = BufferedWriter(OutputStreamWriter(process!!.outputStream))
            reader = BufferedReader(InputStreamReader(process!!.inputStream))
            errorReader = BufferedReader(InputStreamReader(process!!.errorStream))
        } catch (e: Exception) {
            // Shell failed to start — will use built-in fallback commands
        }
    }

    fun executeShellCommand(command: String): CommandResult {
        return try {
            val proc = Runtime.getRuntime().exec(
                arrayOf("/system/bin/sh", "-c", command),
                arrayOf(
                    "TERM=xterm-256color",
                    "HOME=/data/data/com.terminal0.app",
                    "PATH=/sbin:/system/sbin:/system/bin:/system/xbin:/vendor/bin"
                )
            )

            val stdout = proc.inputStream.bufferedReader().readText()
            val stderr = proc.errorStream.bufferedReader().readText()
            proc.waitFor()

            val combined = buildList {
                if (stdout.isNotBlank()) addAll(stdout.lines().filter { it.isNotEmpty() || stdout.lines().size == 1 })
                if (stderr.isNotBlank()) addAll(stderr.lines().map { "⚠ $it" })
            }

            if (combined.isEmpty()) CommandResult.Empty
            else CommandResult.Output(combined)
        } catch (e: Exception) {
            CommandResult.Error("exec failed: ${e.message}")
        }
    }

    fun isAlive(): Boolean = try {
        process?.exitValue()
        false
    } catch (e: IllegalThreadStateException) {
        true
    }

    fun destroy() {
        try {
            writer?.close()
            reader?.close()
            errorReader?.close()
            process?.destroy()
        } catch (_: Exception) {}
    }
}
