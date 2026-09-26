package com.salat.gsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.usecases.LoadBoolSharedPrefUseCase
import com.salat.preferences.domain.usecases.LoadFloatPrefUseCase
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.mappers.toDisplay
import com.salat.splitlauncher.domain.usecases.GetNoCaptionWindowsFlowUseCase
import com.salat.splitpresets.domain.usecases.GetPresetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ShortcutViewModel @Inject constructor(
    private val getPresetsUseCase: GetPresetsUseCase,
    private val loadFloatPrefUseCase: LoadFloatPrefUseCase,
    private val loadBoolSharedPrefUseCase: LoadBoolSharedPrefUseCase,
    getNoCaptionWindowsFlowUseCase: GetNoCaptionWindowsFlowUseCase
) : ViewModel() {
    private val _presetsState = MutableStateFlow<List<DisplaySplitPreset>?>(null)
    val presetsState = _presetsState.asStateFlow()

    val noCaptionWindowsState = getNoCaptionWindowsFlowUseCase.flow

    private val _uiScaleState = MutableStateFlow(1f)
    val uiScaleState = _uiScaleState.asStateFlow()

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    private val _finishState = Channel<Unit>()
    val finishState = _finishState.receiveAsFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _darkTheme.value = loadBoolSharedPrefUseCase.execute(BoolSharedPref.DarkTheme)
            _uiScaleState.value = loadFloatPrefUseCase.execute(FloatPref.UiScale)

            val presets = getPresetsUseCase.execute()
            if (presets.isEmpty()) {
                _finishState.send(Unit)
            } else {
                _presetsState.value = presets.toDisplay()
            }
        }
    }
}
