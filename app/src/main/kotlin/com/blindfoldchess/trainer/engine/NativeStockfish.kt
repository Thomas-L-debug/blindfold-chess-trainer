package com.blindfoldchess.trainer.engine

internal object NativeStockfish {
    init {
        System.loadLibrary("stockfishjni")
    }

    @JvmStatic
    external fun startEngine()

    @JvmStatic
    external fun sendCommand(cmd: String)

    @JvmStatic
    external fun readLine(): String
}
