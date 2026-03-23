package com.satsbuddy.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.satsbuddy.domain.model.BalanceDisplayFormat
import com.satsbuddy.domain.model.Price

@Composable
fun BalanceText(
    satAmount: Long,
    format: BalanceDisplayFormat,
    price: Price?,
    fontSize: TextUnit = 32.sp,
    onFormatToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val text = format.formatted(satAmount, price)
    val suffix = format.displaySuffix()

    Text(
        text = "$text$suffix",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize
        ),
        modifier = modifier.then(
            if (onFormatToggle != null) Modifier.clickable { onFormatToggle() }
            else Modifier
        )
    )
}
