#include <jni.h>
#include <pthread.h>
#include <unistd.h>

#include <cstring>
#include <iostream>
#include <streambuf>
#include <string>
#include <thread>

#include "bitboard.h"
#include "endgame.h"
#include "evaluate.h"
#include "misc.h"
#include "position.h"
#include "psqt.h"
#include "search.h"
#include "thread.h"
#include "tune.h"
#include "uci.h"

namespace {

class FdInBuf : public std::streambuf {
public:
    explicit FdInBuf(int fd) : fd_(fd) { setg(buffer_, buffer_, buffer_); }

protected:
    int underflow() override {
        if (gptr() < egptr()) {
            return static_cast<unsigned char>(*gptr());
        }
        const ssize_t n = read(fd_, buffer_, sizeof(buffer_));
        if (n <= 0) {
            return traits_type::eof();
        }
        setg(buffer_, buffer_, buffer_ + n);
        return static_cast<unsigned char>(*gptr());
    }

private:
    int fd_;
    char buffer_[1024];
};

class FdOutBuf : public std::streambuf {
public:
    explicit FdOutBuf(int fd) : fd_(fd) {}

protected:
    std::streamsize xsputn(const char* s, std::streamsize n) override {
        std::streamsize written = 0;
        while (written < n) {
            const ssize_t r = write(fd_, s + written, static_cast<size_t>(n - written));
            if (r <= 0) {
                break;
            }
            written += r;
        }
        return written;
    }

    int overflow(int c) override {
        if (c == traits_type::eof()) {
            return traits_type::eof();
        }
        const char ch = static_cast<char>(c);
        return write(fd_, &ch, 1) == 1 ? c : traits_type::eof();
    }

private:
    int fd_;
};

int input_pipe[2] = {-1, -1};
int output_pipe[2] = {-1, -1};
FdInBuf* in_buf = nullptr;
FdOutBuf* out_buf = nullptr;

void stockfish_main() {
    using namespace Stockfish;

    char arg0[] = "stockfish";
    char* argv[] = {arg0, nullptr};
    const int argc = 1;

    CommandLine::init(argc, argv);
    UCI::init(Options);
    Tune::init();
    PSQT::init();
    Bitboards::init();
    Position::init();
    Bitbases::init();
    Endgames::init();

    Options["Use NNUE"] = std::string("false");
    Options["Threads"] = std::string("1");
    Options["Hash"] = std::string("16");

    Threads.set(size_t(Options["Threads"]));
    Search::clear();
    Eval::NNUE::init();

    UCI::loop(argc, argv);
    Threads.set(0);
}

} // namespace

extern "C" JNIEXPORT void JNICALL
Java_com_blindfoldchess_trainer_engine_NativeStockfish_startEngine(JNIEnv*, jclass) {
    if (input_pipe[0] != -1) {
        return;
    }
    pipe(input_pipe);
    pipe(output_pipe);

    in_buf = new FdInBuf(input_pipe[0]);
    out_buf = new FdOutBuf(output_pipe[1]);
    std::cin.rdbuf(in_buf);
    std::cout.rdbuf(out_buf);
    std::cout << std::unitbuf;

    std::thread([]() { stockfish_main(); }).detach();
}

extern "C" JNIEXPORT void JNICALL
Java_com_blindfoldchess_trainer_engine_NativeStockfish_sendCommand(JNIEnv* env, jclass, jstring jcmd) {
    if (input_pipe[1] < 0 || jcmd == nullptr) {
        return;
    }
    const char* cmd = env->GetStringUTFChars(jcmd, nullptr);
    const size_t len = std::strlen(cmd);
    if (len > 0) {
        write(input_pipe[1], cmd, len);
    }
    write(input_pipe[1], "\n", 1);
    env->ReleaseStringUTFChars(jcmd, cmd);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_blindfoldchess_trainer_engine_NativeStockfish_readLine(JNIEnv* env, jclass) {
    if (output_pipe[0] < 0) {
        return env->NewStringUTF("");
    }
    std::string line;
    char ch;
    while (true) {
        const ssize_t n = read(output_pipe[0], &ch, 1);
        if (n <= 0) {
            break;
        }
        if (ch == '\n') {
            break;
        }
        if (ch != '\r') {
            line.push_back(ch);
        }
    }
    return env->NewStringUTF(line.c_str());
}
