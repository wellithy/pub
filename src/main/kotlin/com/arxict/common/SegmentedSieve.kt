/**
 * An implementation of [Segmented sieve][https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes#Segmented_sieve]
 */

package com.arxict.common

import java.util.*
import com.arxict.common.EratosthenesSieve.Companion.sqrt
import com.arxict.common.EratosthenesSieve.Companion.sieve
import kotlin.NoSuchElementException

class SegmentedSieve(val max: Int, val delta: Int = sqrt(max)) : Iterable<Int> {
    private val sqrtMax = sqrt(max)

    init {
        require(delta <= sqrtMax)
    }

    override fun iterator(): IntIterator = object : IntIterator() {
        private val primes = EratosthenesSieve(sqrtMax).asSequence()
        private var primesIterator: Iterator<Int>? = primes.iterator()
        private var index = primesIterator!!.next()
        private val segment = BitSet(delta)
        private var start = 0
        private var end = -1

        private fun computeNextIndex() {
            if (end < 0) primesIterator?.let {
                if (it.hasNext()) {
                    index = it.next()
                    return
                } else {
                    end = sqrtMax + 1
                    primesIterator = null
                    index = -1
                }
            } ?: error("Invalid state")

            if (index >= 0)
                index = segment.nextSetBit(index + 1)
            while (index < 0) {
                start = end
                if (start > max) return
                end = (end + delta).coerceAtMost(max + 1)
                val size = end - start
                segment.set(0, size)
                segment.clear(size, delta)
                sieve(primes, segment, size) { it - 1 - (start - 1) % it }
                index = segment.nextSetBit(0)
            }
        }

        override fun hasNext(): Boolean =
            index >= 0

        override fun nextInt(): Int =
            if (index >= 0) (start + index).also { computeNextIndex() }
            else throw NoSuchElementException()
    }

}
