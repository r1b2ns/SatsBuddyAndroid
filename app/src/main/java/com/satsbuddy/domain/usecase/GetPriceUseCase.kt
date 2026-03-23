package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.Price
import com.satsbuddy.domain.repository.PriceRepository
import javax.inject.Inject

class GetPriceUseCase @Inject constructor(
    private val priceRepository: PriceRepository
) {
    suspend operator fun invoke(): Result<Price> = runCatching {
        priceRepository.fetchPrice()
    }
}
