package com.terminal0.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.terminal0.app.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var terminalAdapter: TerminalAdapter
    private lateinit var terminalSession: TerminalSession
    private lateinit var commandHandler: CommandHandler

    private val commandHistory = mutableListOf<String>()
    private var historyIndex = -1

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTerminal()
        setupInputField()
        requestPermissions()
        showBanner()
    }

    private fun setupTerminal() {
        terminalAdapter = TerminalAdapter()
        terminalSession = TerminalSession()
        commandHandler = CommandHandler(this, terminalSession)

        binding.recyclerViewTerminal.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
            adapter = terminalAdapter
        }
    }

    private fun setupInputField() {
        binding.editTextInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                executeCurrentInput()
                true
            } else false
        }

        binding.editTextInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        executeCurrentInput()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        navigateHistory(-1)
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        navigateHistory(1)
                        true
                    }
                    else -> false
                }
            } else false
        }

        binding.btnExecute.setOnClickListener {
            executeCurrentInput()
        }
    }

    private fun executeCurrentInput() {
        val input = binding.editTextInput.text.toString().trim()
        if (input.isEmpty()) return

        binding.editTextInput.setText("")

        commandHistory.add(0, input)
        historyIndex = -1
        if (commandHistory.size > 100) commandHistory.removeLast()

        val prompt = "terminal@device:~$ "
        appendLine(prompt + input, TerminalLine.Type.INPUT)

        lifecycleScope.launch {
            processCommand(input)
        }
    }

    private suspend fun processCommand(command: String) {
        val result = withContext(Dispatchers.IO) {
            commandHandler.execute(command)
        }

        withContext(Dispatchers.Main) {
            when (result) {
                is CommandResult.Output -> {
                    result.lines.forEach { line ->
                        appendLine(line, TerminalLine.Type.OUTPUT)
                    }
                }
                is CommandResult.Error -> {
                    appendLine(result.message, TerminalLine.Type.ERROR)
                }
                is CommandResult.Clear -> {
                    terminalAdapter.clear()
                    showBanner()
                }
                is CommandResult.Empty -> { /* nothing */ }
            }
            scrollToBottom()
        }
    }

    fun appendLine(text: String, type: TerminalLine.Type = TerminalLine.Type.OUTPUT) {
        terminalAdapter.addLine(TerminalLine(text, type))
        scrollToBottom()
    }

    private fun scrollToBottom() {
        binding.recyclerViewTerminal.post {
            val count = terminalAdapter.itemCount
            if (count > 0) {
                binding.recyclerViewTerminal.smoothScrollToPosition(count - 1)
            }
        }
    }

    private fun navigateHistory(direction: Int) {
        if (commandHistory.isEmpty()) return
        historyIndex = (historyIndex + direction).coerceIn(-1, commandHistory.size - 1)
        if (historyIndex >= 0) {
            binding.editTextInput.setText(commandHistory[historyIndex])
            binding.editTextInput.setSelection(binding.editTextInput.text.length)
        } else {
            binding.editTextInput.setText("")
        }
    }

    private fun showBanner() {
        val banner = listOf(
            "  _____ _____ ____  __  __ ___ _   _    _    _          ___  ",
            " |_   _| ____|  _ \\|  \\/  |_ _| \\ | |  / \\  | |        / _ \\ ",
            "   | | |  _| | |_) | |\\/| || ||  \\| | / _ \\ | |       | | | |",
            "   | | | |___|  _ <| |  | || || |\\  |/ ___ \\| |___    | |_| |",
            "   |_| |_____|_| \\_\\_|  |_|___|_| \\_/_/   \\_\\_____|    \\___/ ",
            "",
            "  Terminal-0 v1.0  |  Android Shell",
            "  ─────────────────────────────────────",
            "  Type 'help' for available commands",
            "  Type 'sysinfo' for device information",
            ""
        )
        banner.forEach { appendLine(it, TerminalLine.Type.BANNER) }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val denied = grantResults.any { it != PackageManager.PERMISSION_GRANTED }
            if (denied) {
                Toast.makeText(this, "Storage permission denied - some commands limited", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        terminalSession.destroy()
    }
}
