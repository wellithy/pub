package com.arxict.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class EratosthenesSieveTest {
    companion object {
        /**
         * List of prime number [OEIS A000040][https://oeis.org/A000040]
         */
        val FIRST_PRIMES =
            listOf(
                2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101,
                103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199,
                211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271
            )

        /**
         * List of some [Prime Counting function][https://en.wikipedia.org/wiki/Prime-counting_function#Table_of_%CF%80(x),_x_/_log_x,_and_li(x)]
         */
        val PRIME_COUNTING = mapOf(
            100 to 25,
            1_000_000 to 78_498,
            1_000_000_000 to 50_847_534,
        )

        fun same(expected: Iterator<Int>, nums: IntIterator): Boolean {
            while (expected.hasNext() && nums.hasNext())
                if (expected.next() != nums.nextInt())
                    return false
            return !nums.hasNext() && !expected.hasNext()
        }
    }

    @Test
    fun `first primes`() {
        FIRST_PRIMES.let {
            val sieve = EratosthenesSieve(it.last())
            assertContentEquals(it, sieve)
            assertTrue { same(it.iterator(), sieve.iterator()) }
        }
    }

    @Test
    fun `primes count`() {
        PRIME_COUNTING.forEach { (max, count) ->
            assertEquals(count, EratosthenesSieve(max).count())
        }
    }
}
