package com.interviewprep.brokencalc

import java.io.File
import java.io.FileOutputStream

// keeps the last 50 things that were on the display, so we can attach them to bug reports
object InputLog {

    fun record(text: String) {
        val file = File(MainActivity.instance.filesDir, "input_log.txt")
        val lines = ArrayList<String>()
        if (file.exists()) {
            lines.addAll(file.readLines())
        }
        lines.add("" + System.currentTimeMillis() + " " + text)
        while (lines.size > 50) {
            lines.removeAt(0)
        }
        val out = FileOutputStream(file)
        out.write(lines.joinToString("\n").toByteArray())
        out.fd.sync() // make sure it's really on disk in case the app crashes
        out.close()
    }
}
