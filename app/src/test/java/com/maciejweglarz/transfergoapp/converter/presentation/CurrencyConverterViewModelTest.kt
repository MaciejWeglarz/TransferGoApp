package com.maciejweglarz.transfergoapp.converter.presentation

import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote
import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import com.maciejweglarz.transfergoapp.converter.domain.usecase.ConvertCurrencyUseCase
import com.maciejweglarz.transfergoapp.core.network.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// ---- FAKE CONNECTIVITY ----

private class FakeConnectivityObserver : ConnectivityObserver {

    private val _status = MutableStateFlow(ConnectivityObserver.Status.Available)

    override fun observe(): Flow<ConnectivityObserver.Status> = _status

    fun setStatus(status: ConnectivityObserver.Status) {
        _status.value = status
    }
}

// ---- FAKE REPOSITORY ----

private class FakeCurrencyRepository : CurrencyRepository {

    var lastFrom: String? = null
    var lastTo: String? = null
    var lastAmount: Double? = null

    var nextResult: FxQuote? = null
    var nextError: Throwable? = null

    override suspend fun getQuote(
        from: String,
        to: String,
        amount: Double
    ): FxQuote {
        lastFrom = from
        lastTo = to
        lastAmount = amount

        nextError?.let { throw it }


        return nextResult ?: FxQuote(
            fromCurrency = from,
            toCurrency = to,
            amountFrom = amount,
            amountTo = amount * 7.23,
            rate = 7.23
        )
    }
}

// ---- TESTY VIEWMODELU ----

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyConverterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepo: FakeCurrencyRepository
    private lateinit var fakeConnectivity: FakeConnectivityObserver
    private lateinit var useCase: ConvertCurrencyUseCase
    private lateinit var viewModel: CurrencyConverterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeCurrencyRepository()
        fakeConnectivity = FakeConnectivityObserver()
        useCase = ConvertCurrencyUseCase(fakeRepo)

        viewModel = CurrencyConverterViewModel(
            convertCurrencyUseCase = useCase,
            connectivityObserver = fakeConnectivity
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `convert updates state with correct values`() = runTest {
        // given
        fakeRepo.nextResult = FxQuote(
            fromCurrency = "PLN",
            toCurrency = "UAH",
            amountFrom = 300.0,
            amountTo = 2169.0,
            rate = 7.23
        )

        viewModel.updateAmountFrom("300")

        // when
        viewModel.convert()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value

        // then
        assertEquals("PLN", state.fromCurrency)
        assertEquals("UAH", state.toCurrency)
        assertEquals("300,00", state.amountFrom)
        assertEquals("2169,00", state.amountTo)
        assertEquals("1 PLN = 7.23 UAH", state.rateText)
        assertFalse(state.loading)
        assertNull(state.error)

        assertEquals("PLN", fakeRepo.lastFrom)
        assertEquals("UAH", fakeRepo.lastTo)
        assertEquals(300.0, fakeRepo.lastAmount!!, 0.0)
    }

    @Test
    fun `reverseConvert uses amountTo and swaps direction`() = runTest {
        // given
        viewModel.updateAmountFrom("100,00")
        viewModel.updateAmountTo("723,00")

        fakeRepo.nextResult = FxQuote(
            fromCurrency = "UAH",
            toCurrency = "PLN",
            amountFrom = 723.0,
            amountTo = 100.0,
            rate = 0.1383
        )

        // when
        viewModel.reverseConvert()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value

        // then – znowu przecinki
        assertEquals("PLN", state.fromCurrency)
        assertEquals("UAH", state.toCurrency)
        assertEquals("100,00", state.amountFrom)
        assertEquals("723,00", state.amountTo)
        assertEquals("1 UAH = 0.1383 PLN", state.rateText)
        assertFalse(state.loading)
        assertNull(state.error)

        assertEquals("UAH", fakeRepo.lastFrom)
        assertEquals("PLN", fakeRepo.lastTo)
        assertEquals(723.0, fakeRepo.lastAmount!!, 0.0)
    }

    @Test
    fun `convert sets error when amount exceeds limit`() = runTest {
        // given
        viewModel.updateAmountFrom("21000")

        // when
        viewModel.convert()


        val state = viewModel.state.value

        // then
        assertTrue(state.error?.contains("Maximum sending amount") == true)
        assertTrue(state.error?.contains("PLN") == true)
        assertEquals("21000", state.amountFrom)
        assertFalse(state.loading)
    }

    @Test
    fun `onSendingAmountChange clears error`() = runTest {
        // given
        viewModel.updateAmountFrom("21000")
        viewModel.convert()

        val withError = viewModel.state.value
        assertNotNull(withError.error)

        // when
        viewModel.onSendingAmountChange("1000")

        val state = viewModel.state.value

        // then
        assertEquals("1000", state.amountFrom)
        assertNull(state.error)
    }

    @Test
    fun `swapCurrencies swaps codes and amounts`() {
        // given
        viewModel.updateCurrencies(from = "PLN", to = "UAH")
        viewModel.updateAmountFrom("100,00")
        viewModel.updateAmountTo("723,00")

        // when
        viewModel.swapCurrencies()

        val state = viewModel.state.value

        // then
        assertEquals("UAH", state.fromCurrency)
        assertEquals("PLN", state.toCurrency)
        assertEquals("723,00", state.amountFrom)
        assertEquals("100,00", state.amountTo)
    }
}
