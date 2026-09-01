package com.blindfoldchess.trainer.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class StockfishChessEngine : ChessEngine {
    private val mutex = Mutex()
    private val lines = LinkedBlockingQueue<String>()
    private val started = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)

    override suspend fun bestMove(fen: String, elo: Int): String? = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (closed.get()) return@withContext null
            if (!ensureStarted()) return@withContext null
            val clamped = clampStockfishElo(elo)
            send("setoption name UCI_LimitStrength value true")
            send("setoption name UCI_Elo value $clamped")
            send("position fen $fen")
            send("go movetime ${movetimeMsForElo(clamped)}")
            waitForBestMove()
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        if (started.get()) {
            runCatching { NativeStockfish.sendCommand("quit") }
        }
    }

    private fun ensureStarted(): Boolean {
        if (started.get()) return true
        return try {
            NativeStockfish.startEngine()
            Thread(::readLoop, "stockfish-reader").apply { isDaemon = true }.start()
            send("uci")
            if (!waitForToken("uciok")) return false
            send("setoption name Use NNUE value false")
            send("setoption name Threads value 1")
            send("setoption name Hash value 16")
            send("isready")
            if (!waitForToken("readyok")) return false
            started.set(true)
            true
        } catch (_: UnsatisfiedLinkError) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun send(command: String) {
        NativeStockfish.sendCommand(command)
    }

    private fun readLoop() {
        while (!closed.get()) {
            val line = runCatching { NativeStockfish.readLine() }.getOrNull() ?: break
            if (line.isNotEmpty()) {
                lines.offer(line)
            }
        }
    }

    private fun waitForToken(token: String, timeoutMs: Long = 8_000): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val remaining = deadline - System.currentTimeMillis()
            val line = lines.poll(remaining.coerceAtLeast(1), TimeUnit.MILLISECONDS) ?: return false
            if (line == token || line.endsWith(token)) return true
        }
        return false
    }

    private fun waitForBestMove(timeoutMs: Long = 15_000): String? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val remaining = deadline - System.currentTimeMillis()
            val line = lines.poll(remaining.coerceAtLeast(1), TimeUnit.MILLISECONDS) ?: return null
            parseBestMove(line)?.let { return it }
        }
        return null
    }
}
