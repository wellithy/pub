/**
 * Describe the relation between ClosedRange.  Provides methods merge of one or more ranges.
 */
package com.arxict.common

import java.util.*

enum class ClosedRangeRelation {
    EMPTY,
    EQUALS,
    AFTER,
    BEFORE,
    CONTAINS,
    CONTAINED,
    OVERLAPS,
    OVERLAPPED,
    ;

    companion object {
        private infix fun <T : Comparable<T>, R : ClosedRange<T>> R.after(r: R): Boolean =
            r.endInclusive < start

        private infix fun <T : Comparable<T>, R : ClosedRange<T>> R.contains(r: R): Boolean =
            r.start in this && r.endInclusive in this

        private infix fun <T : Comparable<T>, R : ClosedRange<T>> R.overlaps(r: R): Boolean =
            r.start in this && endInclusive < r.endInclusive

        fun <T : Comparable<T>, R : ClosedRange<T>> of(r1: R, r2: R): ClosedRangeRelation = when {
            /* It's critical to apply these conditions in the following _*specific order*_
                 Condition              Pre-Condition                   Relative Position
              r1 after    r2   !( EMPTY || EQUALS )                    s2 <= e2 <  s1 <= e1
              r1 contains r2   !( r1 after r2 || r2 after r1 )         s1 <= s2 <= e2 <= e1
              r1 overlaps r2   !( r1 contains r2 || r2 contains r1 )   s1 <= s2 <= e1 <  e2
             */
            r1.isEmpty() || r2.isEmpty() -> EMPTY
            r1 == r2 -> EQUALS
            r1 after r2 -> AFTER
            r2 after r1 -> BEFORE
            r1 contains r2 -> CONTAINS
            r2 contains r1 -> CONTAINED
            r1 overlaps r2 -> OVERLAPS
            r2 overlaps r1 -> OVERLAPPED
            else -> error("Unhandled condition")
        }

        private val REVERSE = EnumMap(
            mapOf(
                EMPTY to EMPTY,
                EQUALS to EQUALS,
                AFTER to BEFORE,
                BEFORE to AFTER,
                CONTAINS to CONTAINED,
                CONTAINED to CONTAINS,
                OVERLAPS to OVERLAPPED,
                OVERLAPPED to OVERLAPS,
            )
        )
    }

    fun reverse(): ClosedRangeRelation =
        REVERSE[this]!!
}

fun <T : Comparable<T>, R : ClosedRange<T>> R.canMerge(other: R, allowReverse: Boolean = false): Boolean =
    when (ClosedRangeRelation.of(this, other)) {
        ClosedRangeRelation.EQUALS, ClosedRangeRelation.CONTAINS, ClosedRangeRelation.OVERLAPS -> true
        ClosedRangeRelation.CONTAINED, ClosedRangeRelation.OVERLAPPED -> allowReverse
        else -> false
    }

private operator fun <T : Comparable<T>, R : ClosedRange<T>> R.plus(r2: R): ClosedRange<T> =
    start..r2.endInclusive

fun <T : Comparable<T>, R : ClosedRange<T>> R.merge(r2: R, allowReverse: Boolean = false): ClosedRange<T>? =
    when (ClosedRangeRelation.of(this, r2)) {
        ClosedRangeRelation.EQUALS, ClosedRangeRelation.CONTAINS -> this
        ClosedRangeRelation.OVERLAPS -> this + r2
        ClosedRangeRelation.CONTAINED -> if (allowReverse) r2 else null
        ClosedRangeRelation.OVERLAPPED -> if (allowReverse) r2 + this else null
        else -> null
    }

fun <T : Comparable<T>> Iterator<ClosedRange<T>>.merge(): Iterator<ClosedRange<T>> =
    object : AbstractIterator<ClosedRange<T>>() {
        val myself = this@merge
        var last: ClosedRange<T>? = null
        override fun computeNext() {
            var r1 = last ?: if (myself.hasNext()) myself.next() else return done()
            last = null
            for (r2 in myself) when (ClosedRangeRelation.of(r1, r2)) {
                ClosedRangeRelation.EQUALS, ClosedRangeRelation.CONTAINS -> continue
                ClosedRangeRelation.OVERLAPS -> r1 += r2
                ClosedRangeRelation.BEFORE -> {
                    last = r2
                    break
                }
                else -> error("Can't merge : $r2 into $r1")
            }
            setNext(r1)
        }
    }
