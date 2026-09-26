package com.salat.gsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.statekeeper.domain.usecases.CheckAccessibilityServiceEnabledUseCase
import com.salat.statekeeper.domain.usecases.LaunchLastFromPanelUseCase
import com.salat.statekeeper.domain.usecases.LaunchPresetFromPanelUseCase
import com.salat.statekeeper.domain.usecases.SetPresetPanelShownUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PresetPanelViewModel @Inject constructor(
    private val setPresetPanelShownUseCase: SetPresetPanelShownUseCase,
    private val checkAccessibilityServiceEnabledUseCase: CheckAccessibilityServiceEnabledUseCase,
    private val launchPresetFromPanelUseCase: LaunchPresetFromPanelUseCase,
    private val launchLastFromPanelUseCase: LaunchLastFromPanelUseCase
) : ViewModel() {

    fun setPanelShown(value: Boolean) = setPresetPanelShownUseCase.execute(value)

    fun launchPresetViaService(presetId: Long) = launchViaService { launchPresetFromPanelUseCase.execute(presetId) }

    fun launchLastViaService() = launchViaService { launchLastFromPanelUseCase.execute() }

    // The accessibility service closes the current split before it launches the next one
    private fun launchViaService(sendLaunchEvent: suspend () -> Unit): Boolean {
        if (!checkAccessibilityServiceEnabledUseCase.flow.value) return false
        viewModelScope.launch { sendLaunchEvent() }
        return true
    }
}
