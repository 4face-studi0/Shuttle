package shuttle.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import shuttle.accessibility.usecase.IsLaunchCounterServiceEnabled
import shuttle.design.util.Effect
import shuttle.permissions.domain.usecase.HasAllLocationPermissions
import shuttle.settings.domain.usecase.ObservePrioritizeLocation
import shuttle.settings.domain.usecase.ResetOnboardingShown
import shuttle.settings.domain.usecase.UpdatePrioritizeLocation
import shuttle.settings.presentation.model.SettingsState
import shuttle.settings.presentation.viewmodel.SettingsViewModel.Action
import shuttle.util.android.viewmodel.ShuttleViewModel
import shuttle.utils.kotlin.GetAppVersion

@OptIn(ExperimentalPermissionsApi::class)
class SettingsViewModel(
    private val getAppVersion: GetAppVersion,
    private val hasAllLocationPermissions: HasAllLocationPermissions,
    private val isLaunchCounterServiceEnabled: IsLaunchCounterServiceEnabled,
    private val observePrioritizeLocation: ObservePrioritizeLocation,
    private val resetOnboardingShown: ResetOnboardingShown,
    private val updatePrioritizeLocation: UpdatePrioritizeLocation
) : ShuttleViewModel<Action, SettingsState>(initialState = SettingsState.Loading) {

    init {
        viewModelScope.launch {
            val newState = state.value.copy(appVersion = getAppVersion().toString())
            emit(newState)
        }
        viewModelScope.launch {
            observePrioritizeLocation().collectLatest { prioritizeLocation ->
                val prioritizeLocationValue =
                    if (prioritizeLocation) SettingsState.PrioritizeLocation.True
                    else SettingsState.PrioritizeLocation.False
                val newState = state.value.copy(prioritizeLocation = prioritizeLocationValue)
                emit(newState)
            }
        }
    }

    override fun submit(action: Action) {
        viewModelScope.launch {
            val newState = when (action) {
                Action.ResetOnboardingShown -> onResetOnboardingShown()
                is Action.UpdatePermissionsState -> onPermissionsStateUpdate(action.permissionsState)
                is Action.UpdatePrioritizeLocation -> onUpdatePrioritizeLocation(action.value)
            }
            emit(newState)
        }
    }

    private suspend fun onResetOnboardingShown(): SettingsState {
        resetOnboardingShown()
        return state.value.copy(openOnboardingEffect = Effect.of(Unit))
    }

    private fun onPermissionsStateUpdate(permissionsState: MultiplePermissionsState): SettingsState {
        val hasPermissions = hasAllLocationPermissions(permissionsState) && isLaunchCounterServiceEnabled()
        val permissions = if (hasPermissions) SettingsState.Permissions.Granted else SettingsState.Permissions.Denied

        return state.value.copy(permissions = permissions)
    }

    private fun onUpdatePrioritizeLocation(value: Boolean): SettingsState {
        viewModelScope.launch {
            updatePrioritizeLocation(value)
        }

        val prioritizeLocation =
            if (value) SettingsState.PrioritizeLocation.True
            else SettingsState.PrioritizeLocation.False
        return state.value.copy(prioritizeLocation = prioritizeLocation)
    }

    sealed interface Action {

        object ResetOnboardingShown : Action
        data class UpdatePermissionsState(val permissionsState: MultiplePermissionsState): Action
        data class UpdatePrioritizeLocation(val value: Boolean): Action
    }
}
