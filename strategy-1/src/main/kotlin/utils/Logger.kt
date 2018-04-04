package utils

import java.io.File
import java.io.PrintWriter

class Logger {

//    private var mWriter: PrintWriter = File("/Users/aleksey/projects/agario/log.txt").printWriter()
    private var mLogFile: File = File("/home/timashkov/Experience/aicup/log.txt")


    fun writeLog(message: String){
        mLogFile.appendText(message + "\n")
    }
}