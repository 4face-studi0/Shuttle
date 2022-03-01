package shuttle.predictions.presentation.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import shuttle.design.Dimens
import shuttle.design.ShuttleTheme
import shuttle.predictions.presentation.mapper.LocationPermissionsStateMapper
import shuttle.predictions.presentation.model.LocationPermissionsState
import shuttle.predictions.presentation.model.LocationPermissionsState.AllGranted
import shuttle.predictions.presentation.model.LocationPermissionsState.Pending.AllDenied
import shuttle.predictions.presentation.model.LocationPermissionsState.Pending.CoarseOnly
import shuttle.predictions.presentation.model.LocationPermissionsState.Pending.Init
import shuttle.predictions.presentation.model.LocationPermissionsState.Pending.MissingBackground
import shuttle.predictions.presentation.resources.Strings

private var wasGranted = false

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun LocationPermissionsScreen(onAllPermissionsGranted: () -> Unit) {
    val mapper = LocationPermissionsStateMapper()
    val permissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
//            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val locationPermissionsState = rememberMultiplePermissionsState(permissionsList)

    when (val state = mapper.toLocationPermissionState(locationPermissionsState)) {
        AllGranted -> {
            if (wasGranted.not()) {
                wasGranted = true
                onAllPermissionsGranted()
            }
        }
        is LocationPermissionsState.Pending -> RequestPermissions(state) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
}

@Composable
internal fun RequestPermissions(state: LocationPermissionsState.Pending, onPermissionRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize().padding(Dimens.Margin.XXLarge)
    ) {
        val textToShow = when (state) {
            Init -> Strings.Message.RequestLocation
            CoarseOnly -> Strings.Message.RequestPreciseLocation
            MissingBackground -> Strings.Message.RequestBackgroundLocation
            AllDenied -> Strings.Message.LocationFeatureDisabled
        }

        val buttonText = if (state == CoarseOnly) {
            Strings.Action.AllowPreciseLocation
        } else {
            Strings.Action.RequestPermissions
        }

        Text(
            text = textToShow,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Justify,
        )
        Row(horizontalArrangement = Arrangement.End) {
            Button(onClick = onPermissionRequest) {
                Text(buttonText)
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 250)
fun InitPermissionsPreview() {
    ShuttleTheme {
        RequestPermissions(state = Init, onPermissionRequest = {})
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 250)
fun CoarseOnlyPermissionsPreview() {
    ShuttleTheme {
        RequestPermissions(state = CoarseOnly, onPermissionRequest = {})
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 250)
fun MissingBackgroundOnlyPermissionsPreview() {
    ShuttleTheme {
        RequestPermissions(state = MissingBackground, onPermissionRequest = {})
    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 250)
fun AllDeniedPermissionsPreview() {
    ShuttleTheme {
        RequestPermissions(state = AllDenied, onPermissionRequest = {})
    }
}
