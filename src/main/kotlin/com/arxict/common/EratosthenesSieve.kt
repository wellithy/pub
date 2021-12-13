/**
 * An implementation of [Sieve of Eratosthenes][https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes]
 */

package com.arxict.common

import java.util.*
import kotlin.NoSuchElementException
import kotlin.math.sqrt

class EratosthenesSieve(max: Int) : Iterable<Int> {
    companion object {
        fun sqrt(num: Int): Int =
            sqrt(num.toDouble()).toInt()

        fun sieve(
            seq: Sequence<Int>,
            bits: BitSet,
            end: Int,
            start: (Int) -> Int,
        ) =
            seq.forEach { (start(it) until end step it).forEach(bits::clear) }
    }

    private val bits: BitSet = (max + 1).let { size ->
        BitSet(size).apply {
            set(2, size)
            val end = sqrt(size)
            generateSequence(2) { nextSetBit(it + 1) }
                .takeWhile { it <= end }
                .let { seq -> sieve(seq, this, size) { it * it } }
        }
    }

    override fun iterator(): IntIterator = object : IntIterator() {
        private var prime = bits.nextSetBit(0)
        override fun hasNext(): Boolean =
            prime > 0

        override fun nextInt(): Int =
            if (prime > 0) prime.also { prime = bits.nextSetBit(it + 1) }
            else throw NoSuchElementException()
    }

}
