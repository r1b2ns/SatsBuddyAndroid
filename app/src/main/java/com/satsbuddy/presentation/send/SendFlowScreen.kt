package com.satsbuddy.presentation.send

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.satsbuddy.presentation.send.destination.SendDestinationScreen
import com.satsbuddy.presentation.send.fee.SendFeeScreen
import com.satsbuddy.presentation.send.review.SendReviewScreen
import com.satsbuddy.presentation.send.sign.SendSignScreen

private enum class SendStep { DESTINATION, FEE, REVIEW, SIGN }

@Composable
fun SendFlowScreen(onNavigateBack: () -> Unit) {
    var currentStep by rememberSaveable { mutableStateOf(SendStep.DESTINATION) }
    var destinationAddress by rememberSaveable { mutableStateOf("") }
    var selectedFeeRate by rememberSaveable { mutableIntStateOf(2) }

    when (currentStep) {
        SendStep.DESTINATION -> SendDestinationScreen(
            onBack = onNavigateBack,
            onNext = { address ->
                destinationAddress = address
                currentStep = SendStep.FEE
            }
        )
        SendStep.FEE -> SendFeeScreen(
            onBack = { currentStep = SendStep.DESTINATION },
            onNext = { feeRate ->
                selectedFeeRate = feeRate
                currentStep = SendStep.REVIEW
            }
        )
        SendStep.REVIEW -> SendReviewScreen(
            destinationAddress = destinationAddress,
            feeRate = selectedFeeRate,
            onBack = { currentStep = SendStep.FEE },
            onNext = { currentStep = SendStep.SIGN }
        )
        SendStep.SIGN -> SendSignScreen(
            destinationAddress = destinationAddress,
            feeRate = selectedFeeRate,
            onBack = { currentStep = SendStep.REVIEW },
            onDone = onNavigateBack
        )
    }
}
