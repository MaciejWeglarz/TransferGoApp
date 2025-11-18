package com.maciejweglarz.transfergoapp.converter.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.converter.ConverterCard
import com.maciejweglarz.transfergoapp.core.model.Currencies
import com.maciejweglarz.transfergoapp.ui.theme.TransferGoAppTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.maciejweglarz.transfergoapp.core.model.Currency
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource

private enum class PickerTarget {
    FROM, TO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: CurrencyConverterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        onSendingCurrencyClick = { pickerTarget = PickerTarget.FROM },
        onReceiverCurrencyClick = { pickerTarget = PickerTarget.TO }
    )

    if (pickerTarget != null) {
        CurrencyPickerBottomSheet(
            sheetState = sheetState,
            onDismiss = { pickerTarget = null },
            onCurrencySelected = { currency ->
                when(pickerTarget) {
                    PickerTarget.FROM -> {
                        viewModel.updateCurrencies(
                            from = currency.code,
                            to = state.toCurrency
                        )
                        viewModel.convert()
                    }
                    PickerTarget.TO -> {
                        viewModel.updateCurrencies(
                            from = state.fromCurrency,
                            to = currency.code
                        )
                        viewModel.convert()
                    }
                    null -> Unit
                }
                pickerTarget = null
            }
        )
    }
}

@Composable
private fun ConverterScreenContent(
    state: ConverterUiState,
    onReverseClick: () -> Unit,
    onSendingAmountChange: (String) -> Unit,
    onReceiverAmountChange: (String) -> Unit,
    onSendingCurrencyClick: () -> Unit,
    onReceiverCurrencyClick: () -> Unit
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

            val fromCurrencyConfig = Currencies.getByCode(state.fromCurrency)
            val toCurrencyConfig = Currencies.getByCode(state.toCurrency)

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
                onSendingCurrencyClick = onSendingCurrencyClick,
                onReceiverCurrencyClick = onReceiverCurrencyClick,
                sendingFlagRes = fromCurrencyConfig.flagRes,
                receiverFlagRes = toCurrencyConfig.flagRes,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCurrencySelected: (Currency) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Currencies.list.forEach { currency ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCurrencySelected(currency) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(currency.flagRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currency.country,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${currency.code} · ${currency.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8C9199)
                        )
                    }
                }
            }
        }
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
            onReceiverAmountChange = {},
            onSendingCurrencyClick = {},
            onReceiverCurrencyClick = {}
        )
    }
}
