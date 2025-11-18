package com.example.converter

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maciejweglarz.transfergoapp.R

private val AppFont = FontFamily.SansSerif

@Composable
fun ConverterCard(
    modifier: Modifier = Modifier,
    sendingLabel: String,
    sendingCurrencyCode: String,
    sendingAmount: String,
    receiverLabel: String,
    receiverCurrencyCode: String,
    receiverAmount: String,
    rateLabel: String,
    onReverseClick: () -> Unit,
    onSendingAmountChange: (String) -> Unit,
    onReceiverAmountChange: (String) -> Unit,
    sendingFlagRes: Int,
    receiverFlagRes: Int,
    onSendingCurrencyClick: () -> Unit = {},
    onReceiverCurrencyClick: () -> Unit = {},
    hasError: Boolean = false
) {
    Box(
        modifier = modifier.padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .then(
                    if (hasError) {
                        Modifier.border(
                            width = 2.dp,
                            color = Color(0xFFFF4F9A),
                            shape = RoundedCornerShape(24.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F4F8)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                CurrencySection(
                    title = sendingLabel,
                    flagRes = sendingFlagRes,
                    currencyCode = sendingCurrencyCode,
                    amount = sendingAmount,
                    onAmountChange = onSendingAmountChange,
                    onCurrencyClick = onSendingCurrencyClick,
                    amountColor = Color(0xFF0084FF),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.weight(1f)
                )

                CurrencySection(
                    title = receiverLabel,
                    flagRes = receiverFlagRes,
                    currencyCode = receiverCurrencyCode,
                    amount = receiverAmount,
                    onAmountChange = onReceiverAmountChange,
                    onCurrencyClick = onReceiverCurrencyClick,
                    amountColor = Color.Black,
                    backgroundColor = Color.LightGray,
                    shape = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onReverseClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.reverse_button),
                    contentDescription = "Reverse currencies",
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                shadowElevation = 4.dp,
                color = Color.Black
            ) {
                Text(
                    text = rateLabel,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = AppFont
                )
            }
        }
    }
}

@Composable
private fun CurrencySection(
    title: String,
    @DrawableRes flagRes: Int,
    currencyCode: String,
    amount: String,
    amountColor: Color,
    backgroundColor: Color?,
    shape: Shape?,
    modifier: Modifier = Modifier,
    onAmountChange: (String) -> Unit,
    onCurrencyClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .let { base ->
                if (backgroundColor != null) {
                    base.background(backgroundColor, shape ?: RoundedCornerShape(0.dp))
                } else {
                    base
                }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color(0xFF8C9199),
                    fontFamily = AppFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCurrencyClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 24.dp)
                            .clip(RoundedCornerShape(50))
                    ) {
                        Image(
                            painter = painterResource(flagRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = currencyCode,
                        fontSize = 16.sp,
                        fontFamily = AppFont,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            BasicTextField(
                value = amount,
                onValueChange = { newValue ->
                    val cleanedUserInput = sanitizeAmountInput(newValue)
                    onAmountChange(cleanedUserInput)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(0.5f),
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    fontFamily = AppFont,
                    color = amountColor
                ),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        inner()
                    }
                }
            )
        }
    }
}

private fun sanitizeAmountInput(raw: String): String {
    var separatorSeen = false
    val result = StringBuilder()

    for (char in raw) {
        when {
            char.isDigit() -> result.append(char)
            (char == '.' || char == ',') && !separatorSeen -> {
                result.append(char)
                separatorSeen = true
            }
            else -> Unit
        }
    }

    return result.toString()
}

@Preview(showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun ConverterCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5E5E5))
        ) {
            ConverterCard(
                sendingLabel = "Sending from",
                sendingCurrencyCode = "PLN",
                sendingAmount = "100.00",
                receiverLabel = "Receiver gets",
                receiverCurrencyCode = "UAH",
                receiverAmount = "723.38",
                rateLabel = "1 PLN = 7.23 UAH",
                onReverseClick = {},
                onSendingAmountChange = {},
                onReceiverAmountChange = {},
                sendingFlagRes = R.drawable.flag_pol,
                receiverFlagRes = R.drawable.flag_uah
            )
        }
    }
}
