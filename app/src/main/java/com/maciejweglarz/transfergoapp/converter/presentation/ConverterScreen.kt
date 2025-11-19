package com.maciejweglarz.transfergoapp.converter.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.converter.ConverterCard
import com.maciejweglarz.transfergoapp.core.model.Currencies
import com.maciejweglarz.transfergoapp.core.model.Currency
import com.maciejweglarz.transfergoapp.ui.theme.TransferGoAppTheme

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
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ConverterScreenContent(
        state = state,
        onSendingAmountChange = { value ->
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

        val title = when (pickerTarget) {
            PickerTarget.FROM -> "Sending from"
            PickerTarget.TO -> "Receiver gets"
            null -> ""
        }

        CurrencyPickerBottomSheet(
            sheetState = sheetState,
            title = title,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onDismiss = {
                searchQuery = ""
                pickerTarget = null
            },
            onCurrencySelected = { currency ->
                when (pickerTarget) {
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
                searchQuery = ""
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
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
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
                .fillMaxHeight(0.95f)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Search",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8C9199),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            SearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "All countries",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8C9199),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filteredCurrencies = Currencies.list.filter { currency ->
                if (searchQuery.isBlank()) return@filter true
                val q = searchQuery.trim()
                currency.country.contains(q, ignoreCase = true) ||
                        currency.code.contains(q, ignoreCase = true) ||
                        currency.name.contains(q, ignoreCase = true)
            }

            filteredCurrencies.forEach { currency ->
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

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFEDF0F4), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        textStyle = MaterialTheme.typography.bodyMedium
    ) { inner ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "Search",
                    color = Color(0xFF8C9199),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            inner()
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
            onReverseClick = {},
            onSendingAmountChange = {},
            onReceiverAmountChange = {},
            onSendingCurrencyClick = {},
            onReceiverCurrencyClick = {}
        )
    }
}
