package com.maciejweglarz.transfergoapp.converter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maciejweglarz.transfergoapp.core.model.Currencies
import com.maciejweglarz.transfergoapp.core.network.ConnectivityObserver
import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote
import com.maciejweglarz.transfergoapp.converter.domain.usecase.ConvertCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

data class ConverterUiState(
    val fromCurrency: String = "PLN",
    val toCurrency: String = "UAH",
    val amountFrom: String = "300.00",
    val amountTo: String = "0.00",
    val rateText: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val isNetworkAvailable: Boolean = true,
    val showNoNetworkBanner: Boolean = false
)

@HiltViewModel
class CurrencyConverterViewModel @Inject constructor(
    private val convertCurrencyUseCase: ConvertCurrencyUseCase,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _state = MutableStateFlow(ConverterUiState())
    val state: StateFlow<ConverterUiState> = _state

    init {
        observeNetwork()
        convert()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                val isAvailable = status == ConnectivityObserver.Status.Available

                _state.value = _state.value.copy(
                    isNetworkAvailable = isAvailable,
                    showNoNetworkBanner = !isAvailable
                )
            }
        }
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

    fun dismissNoNetworkBanner() {
        _state.value = _state.value.copy(showNoNetworkBanner = false)
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

        val currencyConfig = Currencies.getByCode(currentState.fromCurrency)
        val max = currencyConfig.maxAmount
        if (amount > max) {
            _state.value = currentState.copy(
                error = "Maximum sending amount: ${max.toInt()} ${currencyConfig.code}"
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
                    loading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()

                val isNetworkError = e is UnknownHostException || e is IOException

                _state.value = _state.value.copy(
                    loading = false,
                    error = if (isNetworkError) null else mapError(e),
                    isNetworkAvailable = !isNetworkError,
                    showNoNetworkBanner = if (isNetworkError) true else _state.value.showNoNetworkBanner
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

                val isNetworkError = e is UnknownHostException || e is IOException

                _state.value = _state.value.copy(
                    loading = false,
                    error = if (isNetworkError) null else mapError(e),
                    isNetworkAvailable = !isNetworkError,
                    showNoNetworkBanner = if (isNetworkError) true else _state.value.showNoNetworkBanner
                )
            }
        }
    }
}
