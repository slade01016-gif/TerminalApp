package com.terminal0.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*

class CommandHandler(
    private val context: Context,
    private val session: TerminalSession
) {

    private var currentDir = File("/data/data/com.terminal0.app")

    fun execute(raw: String): CommandResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return CommandResult.Empty

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].lowercase()
        val args = parts.drop(1)

        return when (cmd) {
            "help"       -> cmdHelp()
            "clear", "cls" -> CommandResult.Clear
            "sysinfo"    -> cmdSysInfo()
            "ram", "mem", "free" -> cmdRam()
            "storage", "df" -> cmdStorage()
            "ls", "dir"  -> cmdLs(args)
            "cd"         -> cmdCd(args)
            "pwd"        -> cmdPwd()
            "cat"        -> cmdCat(args)
            "mkdir"      -> cmdMkdir(args)
            "rm"         -> cmdRm(args)
            "touch"      -> cmdTouch(args)
            "echo"       -> cmdEcho(args)
            "env"        -> cmdEnv()
            "date"       -> cmdDate()
            "uname"      -> cmdUname(args)
            "ps"         -> cmdPs()
            "whoami"     -> CommandResult.Output(listOf("terminal0"))
            "hostname"   -> CommandResult.Output(listOf(Build.MODEL.replace(" ", "-").lowercase()))
            "uptime"     -> cmdUptime()
            "id"         -> cmdId()
            "arch"       -> CommandResult.Output(listOf(System.getProperty("os.arch") ?: "aarch64"))
            "version", "ver" -> cmdVersion()
            "ping"       -> cmdPing(args)
            "netstat"    -> cmdNetstat()
            "ifconfig", "ip" -> cmdIfconfig()
            "battery"    -> cmdBattery()
            "cpu"        -> cmdCpu()
            "top"        -> cmdTop()
            "find"       -> cmdFind(args)
            "wc"         -> cmdWc(args)
            "head"       -> cmdHead(args)
            "tail"       -> cmdTail(args)
            "cp"         -> cmdCp(args)
            "mv"         -> cmdMv(args)
            "chmod"      -> CommandResult.Output(listOf("chmod: permission restricted on Android"))
            "history"    -> CommandResult.Output(listOf("history: use arrow keys to navigate command history"))
            "exit", "quit" -> CommandResult.Output(listOf("Use the device back button to exit."))
            else         -> session.executeShellCommand(trimmed)
        }
    }

    private fun cmdHelp(): CommandResult {
        val lines = listOf(
            "┌─────────────────────────────────────────────────────┐",
            "│              Terminal-0 — Command Reference           │",
            "└─────────────────────────────────────────────────────┘",
            "",
            "  SYSTEM INFO",
            "  ─────────────────────────────────────────────────────",
            "  sysinfo      Full device & OS information",
            "  ram / free   RAM usage (total / used / free)",
            "  storage / df Disk usage (internal & SD card)",
            "  cpu          CPU info & architecture",
            "  battery      Battery level & status",
            "  top          Running processes with memory",
            "  ps           List all processes",
            "  uptime       System uptime",
            "  uname [-a]   Kernel/OS info",
            "  version      Terminal-0 version info",
            "",
            "  FILESYSTEM",
            "  ─────────────────────────────────────────────────────",
            "  ls [path]    List directory contents",
            "  cd [path]    Change directory",
            "  pwd          Print working directory",
            "  cat <file>   Print file contents",
            "  mkdir <dir>  Create directory",
            "  rm <file>    Remove file or directory",
            "  touch <file> Create empty file",
            "  cp <src> <dst> Copy file",
            "  mv <src> <dst> Move/rename file",
            "  find <path> <name> Find files by name",
            "  head <file>  First 10 lines of file",
            "  tail <file>  Last 10 lines of file",
            "  wc <file>    Word/line/char count",
            "",
            "  NETWORK",
            "  ─────────────────────────────────────────────────────",
            "  ping <host>  Ping a hostname or IP",
            "  ifconfig     Network interface info",
            "  netstat      Network connections",
            "",
            "  GENERAL",
            "  ─────────────────────────────────────────────────────",
            "  echo <text>  Print text",
            "  date         Current date and time",
            "  env          Environment variables",
            "  id           User and group IDs",
            "  whoami       Current username",
            "  hostname     Device hostname",
            "  arch         CPU architecture",
            "  clear / cls  Clear terminal",
            "  history      Command history (arrow keys)",
            "  exit         Exit message",
            "",
            "  Any unknown command is passed to /system/bin/sh",
            ""
        )
        return CommandResult.Output(lines)
    }

    private fun cmdSysInfo(): CommandResult {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val totalRam = formatBytes(mi.totalMem)
        val usedRam = formatBytes(mi.totalMem - mi.availMem)
        val freeRam = formatBytes(mi.availMem)
        val ramPercent = ((mi.totalMem - mi.availMem) * 100 / mi.totalMem).toInt()

        val intStorage = StatFs(Environment.getDataDirectory().path)
        val extStorage = StatFs(Environment.getExternalStorageDirectory().path)
        val intTotal = formatBytes(intStorage.totalBytes)
        val intFree = formatBytes(intStorage.freeBytes)
        val extTotal = formatBytes(extStorage.totalBytes)
        val extFree = formatBytes(extStorage.freeBytes)

        val lines = listOf(
            "┌──────────────────────────────────────────────┐",
            "│            SYSTEM INFORMATION                  │",
            "└──────────────────────────────────────────────┘",
            "",
            "  Device       : ${Build.MANUFACTURER} ${Build.MODEL}",
            "  Brand        : ${Build.BRAND}",
            "  Product      : ${Build.PRODUCT}",
            "  Board        : ${Build.BOARD}",
            "  Hardware     : ${Build.HARDWARE}",
            "  Device ID    : ${Build.DEVICE}",
            "",
            "  Android Ver  : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "  Build        : ${Build.DISPLAY}",
            "  Kernel       : ${System.getProperty("os.version")}",
            "  Architecture : ${System.getProperty("os.arch")}",
            "  ABI          : ${Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"}",
            "",
            "  RAM Total    : $totalRam",
            "  RAM Used     : $usedRam  ($ramPercent%)",
            "  RAM Free     : $freeRam",
            "",
            "  Int Storage  : $intFree free / $intTotal total",
            "  Ext Storage  : $extFree free / $extTotal total",
            "",
            "  CPU Cores    : ${Runtime.getRuntime().availableProcessors()}",
            "  Java VM      : ${System.getProperty("java.vm.name")} ${System.getProperty("java.vm.version")}",
            "  Locale       : ${Locale.getDefault()}",
            "  Timezone     : ${TimeZone.getDefault().id}",
            ""
        )
        return CommandResult.Output(lines)
    }

    private fun cmdRam(): CommandResult {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val total = mi.totalMem
        val avail = mi.availMem
        val used = total - avail
        val percent = (used * 100 / total).toInt()
        val bar = buildProgressBar(percent, 30)

        return CommandResult.Output(listOf(
            "",
            "  Memory Usage",
            "  ────────────────────────────────────",
            "  Total   : ${formatBytes(total)}",
            "  Used    : ${formatBytes(used)}",
            "  Free    : ${formatBytes(avail)}",
            "  Threshold: ${formatBytes(mi.threshold)}",
            "  Low Mem : ${mi.lowMemory}",
            "",
            "  [$bar] $percent%",
            ""
        ))
    }

    private fun cmdStorage(): CommandResult {
        val lines = mutableListOf<String>()
        lines += ""
        lines += "  Storage Usage"
        lines += "  ────────────────────────────────────────────────"
        lines += "  Filesystem           Total       Free        Used%"
        lines += "  ──────────────────────────────────────────────────"

        try {
            val data = StatFs(Environment.getDataDirectory().path)
            val pct = ((data.totalBytes - data.freeBytes) * 100 / data.totalBytes).toInt()
            lines += "  /data (internal)     ${formatBytes(data.totalBytes).padEnd(12)}${formatBytes(data.freeBytes).padEnd(12)}$pct%"
        } catch (_: Exception) { lines += "  /data                [unavailable]" }

        try {
            val ext = StatFs(Environment.getExternalStorageDirectory().path)
            val pct = ((ext.totalBytes - ext.freeBytes) * 100 / ext.totalBytes).toInt()
            lines += "  /sdcard (external)   ${formatBytes(ext.totalBytes).padEnd(12)}${formatBytes(ext.freeBytes).padEnd(12)}$pct%"
        } catch (_: Exception) { lines += "  /sdcard              [unavailable]" }

        try {
            val cache = StatFs(Environment.getDownloadCacheDirectory().path)
            val pct = ((cache.totalBytes - cache.freeBytes) * 100 / cache.totalBytes).toInt()
            lines += "  /cache               ${formatBytes(cache.totalBytes).padEnd(12)}${formatBytes(cache.freeBytes).padEnd(12)}$pct%"
        } catch (_: Exception) {}

        lines += ""
        return CommandResult.Output(lines)
    }

    private fun cmdLs(args: List<String>): CommandResult {
        val path = if (args.isEmpty()) currentDir.absolutePath else resolvePath(args[0])
        val dir = File(path)
        if (!dir.exists()) return CommandResult.Error("ls: $path: No such file or directory")
        if (!dir.isDirectory) return CommandResult.Error("ls: $path: Not a directory")

        val files = dir.listFiles() ?: return CommandResult.Error("ls: $path: Permission denied")
        if (files.isEmpty()) return CommandResult.Output(listOf("(empty directory)"))

        val sorted = files.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
        val lines = mutableListOf<String>()
        lines += "  total ${files.size}"

        sorted.forEach { f ->
            val type = if (f.isDirectory) "d" else "-"
            val size = if (f.isDirectory) "<DIR>".padStart(10) else formatBytes(f.length()).padStart(10)
            val date = SimpleDateFormat("MMM dd HH:mm", Locale.US).format(Date(f.lastModified()))
            val name = if (f.isDirectory) "${f.name}/" else f.name
            lines += "  $type  $size  $date  $name"
        }
        return CommandResult.Output(lines)
    }

    private fun cmdCd(args: List<String>): CommandResult {
        if (args.isEmpty()) {
            currentDir = File("/data/data/com.terminal0.app")
            session.currentDirectory = currentDir.absolutePath
            return CommandResult.Output(listOf(currentDir.absolutePath))
        }
        val target = File(resolvePath(args[0]))
        return if (target.exists() && target.isDirectory) {
            currentDir = target
            session.currentDirectory = target.absolutePath
            CommandResult.Output(listOf(target.absolutePath))
        } else {
            CommandResult.Error("cd: ${args[0]}: No such directory")
        }
    }

    private fun cmdPwd() = CommandResult.Output(listOf(currentDir.absolutePath))

    private fun cmdCat(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("cat: missing operand")
        val file = File(resolvePath(args[0]))
        return when {
            !file.exists() -> CommandResult.Error("cat: ${args[0]}: No such file")
            !file.isFile   -> CommandResult.Error("cat: ${args[0]}: Is a directory")
            !file.canRead()-> CommandResult.Error("cat: ${args[0]}: Permission denied")
            file.length() > 1_000_000 -> CommandResult.Error("cat: file too large (>1MB), use head/tail")
            else -> {
                val lines = file.readLines()
                if (lines.isEmpty()) CommandResult.Output(listOf("(empty file)"))
                else CommandResult.Output(lines)
            }
        }
    }

    private fun cmdMkdir(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("mkdir: missing operand")
        val dir = File(resolvePath(args[0]))
        return if (dir.mkdirs()) CommandResult.Output(listOf("mkdir: created '${dir.absolutePath}'"))
        else CommandResult.Error("mkdir: cannot create '${args[0]}'")
    }

    private fun cmdRm(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("rm: missing operand")
        val isRecursive = args.contains("-r") || args.contains("-rf")
        val target = args.find { !it.startsWith("-") } ?: return CommandResult.Error("rm: missing file operand")
        val file = File(resolvePath(target))
        return when {
            !file.exists() -> CommandResult.Error("rm: $target: No such file")
            file.isDirectory && !isRecursive -> CommandResult.Error("rm: $target: Is a directory (use -r)")
            else -> if (file.deleteRecursively()) CommandResult.Output(listOf("removed '${file.absolutePath}'"))
                    else CommandResult.Error("rm: $target: Permission denied")
        }
    }

    private fun cmdTouch(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("touch: missing operand")
        val file = File(resolvePath(args[0]))
        return try {
            if (!file.exists()) file.createNewFile()
            else file.setLastModified(System.currentTimeMillis())
            CommandResult.Output(listOf("touched '${file.absolutePath}'"))
        } catch (e: Exception) {
            CommandResult.Error("touch: ${e.message}")
        }
    }

    private fun cmdEcho(args: List<String>) = CommandResult.Output(listOf(args.joinToString(" ")))

    private fun cmdEnv(): CommandResult {
        val env = System.getenv().entries.sortedBy { it.key }
        return CommandResult.Output(env.map { "  ${it.key}=${it.value}" })
    }

    private fun cmdDate() = CommandResult.Output(listOf(
        SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).format(Date())
    ))

    private fun cmdUname(args: List<String>): CommandResult {
        val all = args.contains("-a")
        return if (all) {
            CommandResult.Output(listOf(
                "Linux ${Build.MODEL.replace(" ","-").lowercase()} ${System.getProperty("os.version")} " +
                "#1 SMP PREEMPT Android ${Build.VERSION.RELEASE} ${System.getProperty("os.arch")}"
            ))
        } else {
            CommandResult.Output(listOf("Linux"))
        }
    }

    private fun cmdPs(): CommandResult {
        return session.executeShellCommand("ps -A 2>/dev/null || ps")
    }

    private fun cmdUptime(): CommandResult {
        val ms = android.os.SystemClock.elapsedRealtime()
        val hours = ms / 3_600_000
        val mins = (ms % 3_600_000) / 60_000
        val secs = (ms % 60_000) / 1000
        return CommandResult.Output(listOf(
            "up ${hours}h ${mins}m ${secs}s"
        ))
    }

    private fun cmdId() = CommandResult.Output(listOf(
        "uid=10000(terminal0) gid=10000(terminal0) groups=10000(terminal0)"
    ))

    private fun cmdVersion() = CommandResult.Output(listOf(
        "",
        "  Terminal-0 v1.0",
        "  Built for Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        "  Device: ${Build.MANUFACTURER} ${Build.MODEL}",
        "  Shell: /system/bin/sh",
        "  Kotlin + Java",
        ""
    ))

    private fun cmdCpu(): CommandResult {
        val cores = Runtime.getRuntime().availableProcessors()
        val arch = System.getProperty("os.arch") ?: "unknown"
        val abis = Build.SUPPORTED_ABIS.joinToString(", ")
        val cpuResult = session.executeShellCommand("cat /proc/cpuinfo 2>/dev/null | grep 'Hardware\\|Processor\\|model name\\|cpu MHz' | head -8")
        val cpuLines = if (cpuResult is CommandResult.Output) cpuResult.lines else emptyList()

        val lines = mutableListOf(
            "",
            "  CPU Information",
            "  ─────────────────────────────",
            "  Cores        : $cores",
            "  Architecture : $arch",
            "  Supported ABI: $abis",
        ) + cpuLines.map { "  $it" } + listOf("")
        return CommandResult.Output(lines)
    }

    private fun cmdBattery(): CommandResult {
        return session.executeShellCommand("dumpsys battery 2>/dev/null | grep -E 'level|status|health|voltage|temperature|technology'").let {
            when (it) {
                is CommandResult.Output -> CommandResult.Output(listOf("", "  Battery Info") + it.lines.map { l -> "  $l" } + listOf(""))
                else -> CommandResult.Error("battery: could not read battery info")
            }
        }
    }

    private fun cmdTop(): CommandResult {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procs = am.runningAppProcesses ?: return CommandResult.Error("top: could not read processes")
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val lines = mutableListOf<String>()
        lines += ""
        lines += "  RAM: ${formatBytes(mi.totalMem - mi.availMem)} used / ${formatBytes(mi.totalMem)} total"
        lines += ""
        lines += "  PID     NAME                          IMPORTANCE"
        lines += "  ─────────────────────────────────────────────────"

        val sorted = procs.sortedBy { it.importance }
        sorted.take(20).forEach { p ->
            val imp = when (p.importance) {
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "FOREGROUND"
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE    -> "VISIBLE"
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE    -> "SERVICE"
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED     -> "CACHED"
                else -> "OTHER(${p.importance})"
            }
            lines += "  ${p.pid.toString().padEnd(8)}${p.processName.take(30).padEnd(32)}$imp"
        }
        lines += ""
        return CommandResult.Output(lines)
    }

    private fun cmdPing(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("ping: missing host")
        return try {
            val host = args.last()
            val count = args.indexOf("-c").let { if (it >= 0 && it + 1 < args.size) args[it + 1].toIntOrNull() ?: 4 else 4 }
            session.executeShellCommand("ping -c $count $host")
        } catch (e: Exception) {
            CommandResult.Error("ping: ${e.message}")
        }
    }

    private fun cmdIfconfig(): CommandResult {
        return session.executeShellCommand("ip addr show 2>/dev/null || ifconfig 2>/dev/null")
    }

    private fun cmdNetstat(): CommandResult {
        return session.executeShellCommand("netstat -tuln 2>/dev/null || cat /proc/net/tcp 2>/dev/null | head -20")
    }

    private fun cmdFind(args: List<String>): CommandResult {
        if (args.size < 2) return CommandResult.Error("find: usage: find <path> <name>")
        val path = resolvePath(args[0])
        val name = args[1]
        val results = mutableListOf<String>()
        fun search(dir: File) {
            if (results.size > 200) return
            try {
                dir.listFiles()?.forEach { f ->
                    if (f.name.contains(name, ignoreCase = true)) results += f.absolutePath
                    if (f.isDirectory) search(f)
                }
            } catch (_: Exception) {}
        }
        search(File(path))
        return if (results.isEmpty()) CommandResult.Output(listOf("(no results)"))
        else CommandResult.Output(results)
    }

    private fun cmdWc(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("wc: missing file")
        val file = File(resolvePath(args[0]))
        if (!file.exists()) return CommandResult.Error("wc: ${args[0]}: No such file")
        val text = file.readText()
        val lines = text.lines().size
        val words = text.trim().split("\\s+".toRegex()).size
        val chars = text.length
        return CommandResult.Output(listOf("  $lines\t$words\t$chars\t${args[0]}"))
    }

    private fun cmdHead(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("head: missing file")
        val n = if (args.contains("-n") && args.size > args.indexOf("-n") + 1)
            args[args.indexOf("-n") + 1].toIntOrNull() ?: 10 else 10
        val filename = args.last()
        val file = File(resolvePath(filename))
        if (!file.exists()) return CommandResult.Error("head: $filename: No such file")
        return CommandResult.Output(file.readLines().take(n))
    }

    private fun cmdTail(args: List<String>): CommandResult {
        if (args.isEmpty()) return CommandResult.Error("tail: missing file")
        val n = if (args.contains("-n") && args.size > args.indexOf("-n") + 1)
            args[args.indexOf("-n") + 1].toIntOrNull() ?: 10 else 10
        val filename = args.last()
        val file = File(resolvePath(filename))
        if (!file.exists()) return CommandResult.Error("tail: $filename: No such file")
        return CommandResult.Output(file.readLines().takeLast(n))
    }

    private fun cmdCp(args: List<String>): CommandResult {
        if (args.size < 2) return CommandResult.Error("cp: usage: cp <source> <dest>")
        val src = File(resolvePath(args[0]))
        val dst = File(resolvePath(args[1]))
        if (!src.exists()) return CommandResult.Error("cp: '${args[0]}': No such file")
        return try {
            src.copyTo(dst, overwrite = true)
            CommandResult.Output(listOf("'${src.name}' -> '${dst.absolutePath}'"))
        } catch (e: Exception) {
            CommandResult.Error("cp: ${e.message}")
        }
    }

    private fun cmdMv(args: List<String>): CommandResult {
        if (args.size < 2) return CommandResult.Error("mv: usage: mv <source> <dest>")
        val src = File(resolvePath(args[0]))
        val dst = File(resolvePath(args[1]))
        if (!src.exists()) return CommandResult.Error("mv: '${args[0]}': No such file")
        return try {
            src.copyTo(dst, overwrite = true)
            src.delete()
            CommandResult.Output(listOf("'${src.name}' -> '${dst.absolutePath}'"))
        } catch (e: Exception) {
            CommandResult.Error("mv: ${e.message}")
        }
    }

    // ──────── Helpers ────────

    private fun resolvePath(path: String): String {
        return when {
            path.startsWith("/") -> path
            path == "~" || path == "~/" -> "/data/data/com.terminal0.app"
            path.startsWith("~/") -> "/data/data/com.terminal0.app/${path.substring(2)}"
            path == ".." -> currentDir.parent ?: currentDir.absolutePath
            path == "." -> currentDir.absolutePath
            else -> "${currentDir.absolutePath}/$path"
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576     -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024         -> "%.1f KB".format(bytes / 1_024.0)
            else                   -> "$bytes B"
        }
    }

    private fun buildProgressBar(percent: Int, width: Int): String {
        val filled = (percent * width / 100).coerceIn(0, width)
        return "█".repeat(filled) + "░".repeat(width - filled)
    }
}
