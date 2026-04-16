package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.repository.PsbtRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildPsbtUseCaseTest {

    private val repository = mockk<PsbtRepository>()
    private val useCase = BuildPsbtUseCase(repository)

    @Test
    fun `invoke returns success with PSBT`() = runTest {
        coEvery { repository.buildSweepPsbt("desc", "bc1qdest", 10) } returns "psbt_hex"

        val result = useCase("desc", "bc1qdest", 10)

        assertTrue(result.isSuccess)
        assertEquals("psbt_hex", result.getOrNull())
    }

    @Test
    fun `invoke returns failure on NotImplementedError`() = runTest {
        coEvery { repository.buildSweepPsbt(any(), any(), any()) } throws
                NotImplementedError("bdk-android not yet integrated")

        val result = useCase("desc", "bc1qdest", 10)

        assertTrue(result.isFailure)
    }
}
