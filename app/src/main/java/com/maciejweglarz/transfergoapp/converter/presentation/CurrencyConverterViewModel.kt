package com.maciejweglarz.transfergoapp.converter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote
import com.maciejweglarz.transfergoapp.converter.domain.usecase.ConvertCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConverterUiState(
    val fromCurrency: String = "PLN",
    val toCurrency: String = "UAH",
    val amountFrom: String = "300.00",
    val amountTo: String = "0.00",
    val rateText: String = "",
    val loading: Boolean = false,
    val error: String? = null
)


@HiltViewModel
class CurrencyConverterViewModel @Inject constructor(
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
): ViewModel() {

    private val _state = MutableStateFlow(ConverterUiState())
    val state: StateFlow<ConverterUiState> = _state

    init {
        convert()
    }

    fun updateAmountFrom(amount: String) {
        _state.value = _state.value.copy(amountFrom = amount)
    }

    fun updateAmountTo(amount: String) {
        _state.value = _state.value.copy(amountTo = amount)
    }

    fun updateCurrencies(from: String, to: String) {
        _state.value = _state.value.copy(fromCurrency = from, toCurrency = to)
    }

    fun swapCurrencies() {
        val s = _state.value
        _state.value = s.copy(
            fromCurrency = s.toCurrency,
            toCurrency = s.fromCurrency,
            amountFrom = s.amountTo,
            amountTo = s.amountFrom
        )
    }

    fun onReverseClick() {
        swapCurrencies()
        convert()
    }

    fun onSendingAmountChange(value: String) {
        updateAmountFrom(value)
    }

    fun onReceiverAmountChange(value: String) {
        updateAmountTo(value)
    }


    fun convert() {
        val currentState = _state.value
        val amount = currentState.amountFrom
            .replace(',', '.')
            .toDoubleOrNull() ?: return

        viewModelScope.launch {
            _state.value = currentState.copy(loading = true, error = null)

            try {
                val quote: FxQuote = convertCurrencyUseCase(
                    from = currentState.fromCurrency,
                    to = currentState.toCurrency,
                    amount = amount
                )

                _state.value = currentState.copy(
                    amountFrom = String.format("%.2f", quote.amountFrom),
                    amountTo = String.format("%.2f", quote.amountTo),
                    rateText = "1 ${quote.fromCurrency} = ${quote.rate} ${quote.toCurrency}",
                    loading = false,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "error"
                )
            }

        }
    }

    fun reverseConvert() {
        val current = _state.value
        val amount = current.amountTo
            .replace(',', '.')
            .toDoubleOrNull() ?: return

        viewModelScope.launch {
            _state.value = current.copy(loading = true, error = null)

            try {
                val quote = convertCurrencyUseCase(
                    from = current.toCurrency,
                    to = current.fromCurrency,
                    amount = amount
                )

                _state.value = _state.value.copy(
                    amountTo = String.format("%.2f", quote.amountFrom),
                    amountFrom = String.format("%.2f", quote.amountTo),
                    rateText = "1 ${quote.fromCurrency} = ${quote.rate} ${quote.toCurrency}",
                    loading = false
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "error"
                )
            }
        }
    }
}
