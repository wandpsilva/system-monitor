package org.example;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getCpuLoad();
                getUsedMemory();
            }
        }, 0, 1000);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void getCpuLoad() {
        Kernel32 kernel32 = Kernel32.INSTANCE;
        WinNT.SYSTEM_INFO systemInfo = new WinNT.SYSTEM_INFO();
        kernel32.GetSystemInfo(systemInfo);
        double cpuLoad = -1;

        WinBase.FILETIME idleTime = new WinBase.FILETIME();
        WinBase.FILETIME kernelTime = new WinBase.FILETIME();
        WinBase.FILETIME userTime = new WinBase.FILETIME();

        if (kernel32.GetSystemTimes(idleTime, kernelTime, userTime)) {
            long idle = fileTimeToLong(idleTime);
            long kernel = fileTimeToLong(kernelTime);
            long user = fileTimeToLong(userTime);

            long system = kernel + user;
            cpuLoad = (1.0 - ((double) idle / system)) * 100.0;
            System.out.printf("CPU load: %.2f%%%n", cpuLoad);
            return;
        }
        System.out.printf("Uso da CPU: %.2f%%%n", cpuLoad);
    }

    private static void getUsedMemory() {
        WinBase.MEMORYSTATUSEX memoryStatus = new WinBase.MEMORYSTATUSEX();
        if (Kernel32.INSTANCE.GlobalMemoryStatusEx(memoryStatus)) {
            long totalMemory = memoryStatus.ullTotalPhys.longValue() / (1024 * 1024);
            long freeMemory = memoryStatus.ullAvailPhys.longValue() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;

            System.out.printf("Used memory: %d MB / %d MB%n", usedMemory, totalMemory);
        }
    }

    private static long fileTimeToLong(WinBase.FILETIME fileTime) {
        return ((long) fileTime.dwHighDateTime << 32) + fileTime.dwLowDateTime;
    }
}