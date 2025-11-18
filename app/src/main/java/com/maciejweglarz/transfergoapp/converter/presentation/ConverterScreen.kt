package com.maciejweglarz.transfergoapp.converter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.converter.ConverterCard
import com.maciejweglarz.transfergoapp.ui.theme.TransferGoAppTheme

@Composable
fun ConverterScreen(
    viewModel: CurrencyConverterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ConverterScreenContent(
        state = state,
        onSendingAmountChange = {value ->
            viewModel.onSendingAmountChange(value)
            viewModel.convert()
        },
        onReceiverAmountChange = { value ->
            viewModel.onReceiverAmountChange(value)
            viewModel.reverseConvert()
        },
        onReverseClick = { viewModel.onReverseClick() },
    )
}

@Composable
private fun ConverterScreenContent(
    state: ConverterUiState,
    onReverseClick: () -> Unit,
    onSendingAmountChange: (String) -> Unit,
    onReceiverAmountChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5E5E5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            ConverterCard(
                sendingLabel = "Sending from",
                sendingCurrencyCode = state.fromCurrency,
                sendingAmount = state.amountFrom,
                receiverLabel = "Receiver gets",
                receiverCurrencyCode = state.toCurrency,
                receiverAmount = state.amountTo,
                rateLabel = state.rateText,
                onReverseClick = onReverseClick,
                onSendingAmountChange = onSendingAmountChange,
                onReceiverAmountChange = onReceiverAmountChange,
                hasError = state.error != null
            )

            state.error?.let { errorMsg ->
                Spacer(modifier = Modifier.height(12.dp))
                ErrorBanner(message = errorMsg)
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = Color(0xFFFFF0F4),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFFFF4F9A),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConverterScreenPreview() {
    TransferGoAppTheme {
        ConverterScreenContent(
            state = ConverterUiState(
                fromCurrency = "PLN",
                toCurrency = "UAH",
                amountFrom = "100.00",
                amountTo = "723.38",
                rateText = "1 PLN = 7.23 UAH",
                loading = false,
                error = null
            ),
            onReverseClick =  {},
            onSendingAmountChange = {},
            onReceiverAmountChange = {}
        )
    }
}
