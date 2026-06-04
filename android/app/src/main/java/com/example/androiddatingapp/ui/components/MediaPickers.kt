package com.example.androiddatingapp.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberVideoPicker(onVideoPicked: (Uri) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onVideoPicked(uri)
    }

@Composable
fun rememberImagePicker(onImagePicked: (Uri) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImagePicked(uri)
    }

fun launchVideoPicker(launcher: androidx.activity.result.ActivityResultLauncher<String>) {
    launcher.launch("video/*")
}

fun launchImagePicker(launcher: androidx.activity.result.ActivityResultLauncher<String>) {
    launcher.launch("image/*")
}
