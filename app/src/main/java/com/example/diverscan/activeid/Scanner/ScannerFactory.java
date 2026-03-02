package com.example.diverscan.activeid.Scanner;

import android.content.Context;
import com.example.diverscan.activeid.BuildConfig;

public class ScannerFactory {

    /**
     * Creates a ScannerService instance based on the current build configuration and runtime preference.
     * 
     * @param context Application context
     * @param forceSimulation If true, forces the use of MockScannerImpl regardless of build type.
     * @return ScannerService implementation.
     */
    public static ScannerService createScanner(Context context, boolean forceSimulation) {
        // Environment Configuration Validation:
        // Allow forcing simulation for testing in any environment, or default to build config.
        if (forceSimulation || BuildConfig.DEBUG) {
             // If specifically requested or in DEBUG (and not overridden), use Mock.
             // However, the user wants to test physical reader in Dev too.
             // So logic should be:
             // 1. If forceSimulation is true -> Mock
             // 2. If forceSimulation is false -> Physical (Auto-detected)
             
             if (forceSimulation) {
                 return new MockScannerImpl();
             } else {
                 return new PhysicalScannerImpl(context);
             }
        } else {
            // For Prod (Release), always default to real hardware unless we add a hidden backdoor later.
            return new PhysicalScannerImpl(context);
        }
    }

    /**
     * Default factory method preserving original behavior (Mock in Debug, Zebra in Release)
     * BUT adapted for the new requirement: "en desarrollo tambien hare pruebas con el lector fisico".
     * So we need a way to persist this preference or default to Mock in Debug but allow switching.
     */
    public static ScannerService createScanner(Context context) {
        // Default behavior: Debug -> Mock, Release -> Zebra
        // But since we want to allow physical in dev, we should probably check a preference here if we were using it globally.
        // For now, let's keep the simple default and let the caller decide if they want to override.
        return createScanner(context, BuildConfig.DEBUG);
    }
}
