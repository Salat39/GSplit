package com.salat.gsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.splitpresets.domain.entity.SplitPreset
import com.salat.splitpresets.domain.usecases.GetPresetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ShortcutViewModel @Inject constructor(
    private val getPresetsUseCase: GetPresetsUseCase
) : ViewModel() {
    private val _presetsListState = Channel<List<SplitPreset>>()
    val presetsListState = _presetsListState.receiveAsFlow()

    init {
        viewModelScope.launch {
            val presets = getPresetsUseCase.execute()
            _presetsListState.send(presets)
        }
    }
}
