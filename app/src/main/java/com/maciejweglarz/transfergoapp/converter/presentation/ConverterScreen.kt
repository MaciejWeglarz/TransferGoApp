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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maciejweglarz.transfergoapp.R
import com.maciejweglarz.transfergoapp.core.model.Currencies
import com.maciejweglarz.transfergoapp.core.model.Currency
import com.maciejweglarz.transfergoapp.ui.theme.ConverterCurrencyListDivider
import com.maciejweglarz.transfergoapp.ui.theme.ConverterErrorBackground
import com.maciejweglarz.transfergoapp.ui.theme.ConverterErrorBorder
import com.maciejweglarz.transfergoapp.ui.theme.ConverterNetworkBannerBackground
import com.maciejweglarz.transfergoapp.ui.theme.ConverterNetworkBannerText
import com.maciejweglarz.transfergoapp.ui.theme.ConverterReceiverBackground
import com.maciejweglarz.transfergoapp.ui.theme.ConverterScreenBackground
import com.maciejweglarz.transfergoapp.ui.theme.ConverterSubtitleText
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
        onReverseClick = { viewModel.onReverseClick() },
        onSendingAmountChange = { value ->
            viewModel.onSendingAmountChange(value)
            viewModel.convert()
        },
        onReceiverAmountChange = { value ->
            viewModel.onReceiverAmountChange(value)
            viewModel.reverseConvert()
        },
        onSendingCurrencyClick = { pickerTarget = PickerTarget.FROM },
        onReceiverCurrencyClick = { pickerTarget = PickerTarget.TO },
        onDismissNetworkBanner = { viewModel.dismissNoNetworkBanner() }
    )

    if (pickerTarget != null) {

        val title = when (pickerTarget) {
            PickerTarget.FROM -> stringResource(R.string.sending_from_text)
            PickerTarget.TO -> stringResource(R.string.receiver_gets_text)
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
    onReceiverCurrencyClick: () -> Unit,
    onDismissNetworkBanner: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ConverterScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (state.showNoNetworkBanner) {
                NetworkBanner(
                    onCloseClick = onDismissNetworkBanner
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val fromCurrencyConfig = Currencies.getByCode(state.fromCurrency)
            val toCurrencyConfig = Currencies.getByCode(state.toCurrency)

            ConverterCard(
                sendingLabel = stringResource(R.string.sending_from_text),
                sendingCurrencyCode = state.fromCurrency,
                sendingAmount = state.amountFrom,
                receiverLabel = stringResource(R.string.receiver_gets_text),
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
private fun NetworkBanner(
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ConverterNetworkBannerBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.icon_network_banner),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.network_banner_title_text),
                color = ConverterNetworkBannerText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.network_banner_description_text),
                color = ConverterNetworkBannerText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Image(
            painterResource(R.drawable.icon_close),
            contentDescription = stringResource(R.string.network_close_button_text),
            modifier = Modifier
                .size(20.dp)
                .clickable { onCloseClick() }
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = ConverterErrorBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = message,
            color = ConverterErrorBorder,
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
                text = stringResource(R.string.search_basic_text),
                style = MaterialTheme.typography.bodySmall,
                color = ConverterSubtitleText,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            SearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.all_countries_basic_text),
                style = MaterialTheme.typography.bodySmall,
                color = ConverterSubtitleText,
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

            filteredCurrencies.forEachIndexed { index, currency ->
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
                            color = ConverterSubtitleText
                        )
                    }
                }

                if (index < filteredCurrencies.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(
                        color = ConverterCurrencyListDivider,
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
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
            .background(ConverterReceiverBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        textStyle = MaterialTheme.typography.bodyMedium
    ) { inner ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_search),
                contentDescription = stringResource(R.string.search_basic_text),
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_basic_text),
                        color = ConverterSubtitleText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                inner()
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
                error = null,
                showNoNetworkBanner = true
            ),
            onReverseClick = {},
            onSendingAmountChange = {},
            onReceiverAmountChange = {},
            onSendingCurrencyClick = {},
            onReceiverCurrencyClick = {},
            onDismissNetworkBanner = {}
        )
    }
}
