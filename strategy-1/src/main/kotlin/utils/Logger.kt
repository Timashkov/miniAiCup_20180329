package utils

import java.io.File
import java.io.PrintWriter

class Logger {

    private var mWriter: PrintWriter = File("/Users/aleksey/projects/agario/log.txt").printWriter()


    fun writeLog(message: String){
//        mWriter.append(message + "\n")
    }
}