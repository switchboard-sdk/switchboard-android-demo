package com.synervoz.switchboardandroiddemo

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.File
import java.io.IOException

object AssetUtils {
    /**
     * Copies a file from assets to internal storage
     * @param context Android context object
     * @param assetPath Path to the file in assets
     * @param targetFileName Name of the target file in internal storage
     * @return The File object pointing to the copied file
     */
    fun copyAssetFileToInternal(context: Context, assetPath: String, targetFileName: String): File {
        val outFile = File(context.filesDir, targetFileName)
        try {
            context.assets.open(assetPath).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            Log.e("AssetUtils", "Failed to copy asset file: $assetPath", e)
        }
        return outFile
    }

    /**
     * Copies a directory from assets to internal storage
     * @param context Android context object
     * @param assetDirPath Path to the directory in assets (e.g., "myFolder")'
     * @param targetDirName Name for the copied directory in internal storage
     * @return The File object pointing to the copied directory
     */
    fun copyAssetDirectoryToInternal(context: Context, assetDirPath: String, targetDirName: String): File {
        val assetManager = context.assets

        // Create the target directory in internal storage
        val targetDir = File(context.filesDir, targetDirName)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        try {
            // Copy the directory contents recursively
            copyDirectoryContents(assetManager, assetDirPath, targetDir)
        } catch (e: IOException) {
            Log.e("AssetUtils", "Error copying directory: $assetDirPath", e)
        }

        return targetDir
    }

    /**
     * Recursively copies contents from an asset directory to a target directory
     */
    private fun copyDirectoryContents(assetManager: AssetManager, path: String, targetDir: File) {
        // Try to list contents of the path
        val assets = assetManager.list(path) ?: return

        for (asset in assets) {
            val assetPath = if (path.isEmpty()) asset else "$path/$asset"

            // Try to open the asset as a file
            try {
                // If this succeeds, it's a file
                assetManager.open(assetPath).use { input ->
                    val outFile = File(targetDir, asset)
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: IOException) {
                // If opening as a file fails, it's a directory
                val subDir = File(targetDir, asset)
                if (!subDir.exists()) {
                    subDir.mkdirs()
                }
                // Recurse into this directory
                copyDirectoryContents(assetManager, assetPath, subDir)
            }
        }
    }
}