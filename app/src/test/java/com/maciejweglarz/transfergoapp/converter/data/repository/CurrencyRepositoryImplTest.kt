package com.maciejweglarz.transfergoapp.converter.data.repository

import com.maciejweglarz.transfergoapp.converter.data.remote.TransferGoApiService
import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CurrencyRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TransferGoApiService
    private lateinit var repo: CurrencyRepository

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val moshi = Moshi.Builder().build()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TransferGoApiService::class.java)

        repo = CurrencyRepositoryImpl(api)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `repository returns correct FxQuote from valid JSON`() = runTest {
        val json = """
        {
            "rate": 7.23,
            "fromAmount": 100.0,
            "toAmount": 723.0
        }
    """.trimIndent()

        server.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val result = repo.getQuote("PLN", "UAH", 100.0)

        assertEquals("PLN", result.fromCurrency)
        assertEquals("UAH", result.toCurrency)
        assertEquals(100.0, result.amountFrom, 0.0)
        assertEquals(723.0, result.amountTo!!, 0.0)
        assertEquals(7.23, result.rate, 0.0)
    }

    @Test
    fun `fallback to computed toAmount when missing`() = runTest {
        // Arrange
        val json = """
        {
            "rate": 7.23,
            "fromAmount": 100.0
        }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(json).setResponseCode(200))

        // Act
        val result = repo.getQuote("PLN", "UAH", 100.0)

        // Assert
        assertEquals(100.0 * 7.23, result.amountTo!!, 0.0)
    }

    @Test
    fun `fallback to request amount when fromAmount missing`() = runTest {
        val json = """
        {
            "rate": 7.23,
            "toAmount": 723.0
        }
    """.trimIndent()

        server.enqueue(MockResponse().setBody(json).setResponseCode(200))

        val result = repo.getQuote("PLN", "UAH", 100.0)

        assertEquals(100.0, result.amountFrom, 0.0)
        assertEquals(723.0, result.amountTo!!, 0.0)
    }

    @Test
    fun `throws HttpException for 422`() = runTest {
        server.enqueue(MockResponse().setResponseCode(422))

        try {
            repo.getQuote("PLN", "UAH", 100.0)
            org.junit.Assert.fail("Expected HttpException to be thrown")
        } catch (e: HttpException) {
            assertEquals(422, e.code())
        }
    }

}
