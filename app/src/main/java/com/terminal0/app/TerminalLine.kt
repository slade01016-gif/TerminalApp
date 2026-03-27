package com.terminal0.app

data class TerminalLine(
    val text: String,
    val type: Type
) {
    enum class Type {
        INPUT,   // User typed command  — cyan
        OUTPUT,  // Normal output       — white/green
        ERROR,   // Error output        — red
        BANNER   // Welcome/header      — orange/red
    }
}

sealed class CommandResult {
    data class Output(val lines: List<String>) : CommandResult()
    data class Error(val message: String) : CommandResult()
    object Clear : CommandResult()
    object Empty : CommandResult()
}
