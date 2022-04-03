package shuttle.permissions.domain.usecase

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
class HasAllLocationPermissions(
    private val hasBackgroundLocation: HasBackgroundLocation,
    private val hasCoarseLocation: HasCoarseLocation,
    private val hasFineLocation: HasFineLocation
) {

    operator fun invoke(state: MultiplePermissionsState) =
        hasBackgroundLocation(state) && hasCoarseLocation(state) && hasFineLocation(state)
}
