package com.terminal0.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TerminalAdapter : RecyclerView.Adapter<TerminalAdapter.LineViewHolder>() {

    private val lines = mutableListOf<TerminalLine>()

    fun addLine(line: TerminalLine) {
        lines.add(line)
        notifyItemInserted(lines.size - 1)
    }

    fun clear() {
        val size = lines.size
        lines.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_terminal_line, parent, false)
        return LineViewHolder(view)
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bind(lines[position])
    }

    override fun getItemCount() = lines.size

    class LineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.textViewLine)

        fun bind(line: TerminalLine) {
            textView.text = line.text
            textView.setTextColor(
                when (line.type) {
                    TerminalLine.Type.INPUT  -> Color.parseColor("#00CFFF")   // cyan-blue
                    TerminalLine.Type.OUTPUT -> Color.parseColor("#C8E6C9")   // soft green-white
                    TerminalLine.Type.ERROR  -> Color.parseColor("#FF5252")   // bright red
                    TerminalLine.Type.BANNER -> Color.parseColor("#FF3C3C")   // terminal red
                }
            )
        }
    }
}
