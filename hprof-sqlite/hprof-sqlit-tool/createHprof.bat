@echo off
adb connect %1
start /wait adb shell am dumpheap %2 /sdcard/test.hprof
@echo off
ping /n 8 127.0.0.1 >nul
start /wait adb pull /sdcard/test.hprof d:\
ping /n 8 127.0.0.1 >nul
start /wait hprof-conv d:\test.hprof %3
ping /n 8 127.0.0.1 >nul
del /f d:\test.hprof