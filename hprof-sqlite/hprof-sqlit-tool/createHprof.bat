@echo off
adb kill-server
adb start-server
adb connect %1
ping /n 5 127.0.0.1 >nul
adb shell am dumpheap %2 /sdcard/test.hprof
@echo off
ping /n 5 127.0.0.1 >nul
adb pull /sdcard/test.hprof d:\
ping /n 5 127.0.0.1 >nul
hprof-conv d:\test.hprof %3
ping /n 5 127.0.0.1 >nul
adb shell rm /sdcard/test.hprof
del /f d:\test.hprof
adb kill-server