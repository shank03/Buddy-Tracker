package com.major.buddytracker.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.major.buddytracker.ui.theme.BuddyTrackerTheme
import com.major.buddytracker.utils.Data
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrCodeScreen(onBack: () -> Unit) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val ctx = LocalContext.current

    if (cameraPermissionState.status.isGranted) {
        cameraScreen(onBack)
    } else if (cameraPermissionState.status.shouldShowRationale) {
        Toast.makeText(ctx, "Camera Permission permanently denied", Toast.LENGTH_SHORT).show()
    } else {
        SideEffect {
            cameraPermissionState.run { launchPermissionRequest() }
        }
        Text("No Camera Permission")
    }
}

@Composable
fun cameraScreen(onBack: () -> Unit) {
    BuddyTrackerTheme {
        Scaffold {
            Row(modifier = Modifier.padding(it)) {
                val localContext = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val cameraProviderFuture = remember {
                    ProcessCameraProvider.getInstance(localContext)
                }
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val preview = Preview.Builder().build()
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        val imageAnalysis = ImageAnalysis.Builder().build()
                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            BarcodeAnalyzer(context) {
                                FirebaseDatabase.getInstance().getReference("buddyPairs")
                                    .child(Data.PAIR_ID)
                                    .updateChildren(HashMap<String?, Any?>().apply {
                                        this["buddy" + Data.ID_DATA + "CheckInTime"] =
                                            Calendar.getInstance().time.toString()
                                    }).addOnSuccessListener {
                                        onBack()
                                    }
                            }
                        )

                        runCatching {
                            cameraProviderFuture.get().bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )
                        }.onFailure {
                            Toast.makeText(localContext, "Error binding camera", Toast.LENGTH_SHORT)
                                .show()
                        }
                        previewView
                    }
                )
            }
        }
    }
}

class BarcodeAnalyzer(private val context: Context, private val getData: (data: String) -> Unit) :
    ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODABAR)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            scanner.process(
                InputImage.fromMediaImage(
                    image, imageProxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { barcode ->
                barcode?.takeIf { it.isNotEmpty() }
                    ?.mapNotNull { it.rawValue }
                    ?.joinToString(",")
                    ?.let {
                        if (it == Data.LOCATION_ADR) {
                            scanner.close()
                            getData(it)
                        } else {
                            Toast.makeText(context, "Not matched", Toast.LENGTH_SHORT).show()
                        }
                    }
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}

private const val TAG = "QrCodeScreen"
