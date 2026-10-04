package com.salat.gsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.usecases.LoadBoolPrefUseCase
import com.salat.preferences.domain.usecases.LoadBoolSharedPrefUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.mappers.toDisplay
import com.salat.splitlauncher.domain.usecases.GetMainWindowAvailableFlowUseCase
import com.salat.splitlauncher.domain.usecases.GetNoCaptionWindowsFlowUseCase
import com.salat.splitpresets.domain.usecases.GetPresetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ShortcutViewModel @Inject constructor(
    private val getPresetsUseCase: GetPresetsUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val loadBoolSharedPrefUseCase: LoadBoolSharedPrefUseCase,
    private val loadBoolPrefUseCase: LoadBoolPrefUseCase,
    getNoCaptionWindowsFlowUseCase: GetNoCaptionWindowsFlowUseCase,
    getMainWindowAvailableFlowUseCase: GetMainWindowAvailableFlowUseCase
) : ViewModel() {
    private val _presetsState = MutableStateFlow<List<DisplaySplitPreset>?>(null)
    val presetsState = _presetsState.asStateFlow()

    val noCaptionWindowsState = getNoCaptionWindowsFlowUseCase.flow

    val mainWindowAvailableState = getMainWindowAvailableFlowUseCase.flow

    private val _nativeSplitState = MutableStateFlow(false)
    val nativeSplitState = _nativeSplitState.asStateFlow()

    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _darkTheme.value = loadBoolSharedPrefUseCase.execute(BoolSharedPref.DarkTheme)
            _uiScaleState.value = loadFloatPrefUseCase.execute(FloatPref.UiScale)
            _nativeSplitState.value = loadBoolPrefUseCase.execute(BoolPref.ExperimentalNativeSplit)

            _presetsState.value = getPresetsUseCase.execute().toDisplay()
        }
    }
}
