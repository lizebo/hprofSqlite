@echo off
adb connect %1
start /wait adb shell am dumpheap %2 /sdcard/test.hprof
@echo off
ping /n 2 127.0.0.1 >nul
start /wait adb pull /sdcard/test.hprof %3
ping /n 2 127.0.0.1 >nul
start /wait hprof-conv %3\test.hprof %3\test2.hprof
ping /n 2 127.0.0.1 >nul
del /f %3\test.hprof