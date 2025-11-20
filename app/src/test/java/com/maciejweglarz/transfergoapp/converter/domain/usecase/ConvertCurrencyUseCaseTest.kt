package com.maciejweglarz.transfergoapp.converter.domain.usecase

import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote
import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


private class FakeCurrencyRepository : CurrencyRepository {

    var lastFrom: String? = null
    var lastTo: String? = null
    var lastAmount: Double? = null

    override suspend fun getQuote(
        from: String,
        to: String,
        amount: Double
    ): FxQuote {
        lastFrom = from
        lastTo = to
        lastAmount = amount

        return FxQuote(
            fromCurrency = from,
            toCurrency = to,
            amountFrom = amount,
            amountTo = amount * 7.23,
            rate = 7.23
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ConvertCurrencyUseCaseTest {


    @Test
    fun `invoke delegates to repository and returns quote`() = runTest {
        //given
        val fakeRepository = FakeCurrencyRepository()
        val useCase = ConvertCurrencyUseCase(fakeRepository)

        //when
        val result = useCase(
            from = "PLN",
            to = "UAH",
            amount = 100.0
        )

        //then
        assertEquals("PLN", fakeRepository.lastFrom)
        assertEquals("UAH", fakeRepository.lastTo)
        assertEquals(100.0, fakeRepository.lastAmount!!, 0.0)

        assertEquals("PLN", result.fromCurrency)
        assertEquals("UAH", result.toCurrency)
        assertEquals(100.0, result.amountFrom, 0.0)
        assertEquals(100.0 * 7.23, result.amountTo!!, 0.0)
        assertEquals(7.23, result.rate, 0.0)
    }

}