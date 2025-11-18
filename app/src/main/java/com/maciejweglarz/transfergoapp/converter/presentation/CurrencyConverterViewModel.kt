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
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

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
        _state.value = _state.value.copy(error = null)
    }

    fun onReceiverAmountChange(value: String) {
        updateAmountTo(value)
        _state.value = _state.value.copy(error = null)
    }

    private fun maxLimitFor(currency: String): Double =
        when (currency) {
            "PLN" -> 20000.0
            "EUR" -> 5000.0
            "GBP" -> 1000.0
            "UAH" -> 50000.0
            else -> Double.MAX_VALUE
        }

    private fun mapError(e: Throwable): String {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    422 -> "We can't process this amount. Try a different value."
                    in 500..599 -> "Server error. Please try again."
                    else -> "Request failed. Please try again."
                }
            }
            is UnknownHostException,
            is IOException -> {
                "Check your internet connection and try again."
            }
            else -> {
                "Something went wrong. Please try again."
            }
        }
    }

    fun convert() {
        val currentState = _state.value
        val amount = currentState.amountFrom
            .replace(',', '.')
            .toDoubleOrNull() ?: return

        val max = maxLimitFor(currentState.fromCurrency)
        if (amount > max) {
            _state.value = currentState.copy(
                error = "Maximum sending amount: ${max.toInt()} ${currentState.fromCurrency}"
            )
            return
        }

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
                    error = mapError(e)
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
                    error = mapError(e)
                )
            }
        }
    }
}
