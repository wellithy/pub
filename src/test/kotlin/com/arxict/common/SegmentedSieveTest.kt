package com.arxict.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class SegmentedSieveTest {
    @Test
    fun `first primes`() {
        EratosthenesSieveTest.FIRST_PRIMES.let {
            assertContentEquals(it, SegmentedSieve(it.last(), 10))
        }
    }

    @Test
    fun `same as Eratosthenes sieve`() {
        (400..40_000 step 200).forEach {
            assertContentEquals(EratosthenesSieve(it), SegmentedSieve(it))
        }
    }

    @Test
    fun `primes count`() {
        EratosthenesSieveTest.PRIME_COUNTING.forEach { (max, count) ->
            assertEquals(count, SegmentedSieve(max).count())
        }
    }
}
