package com.arxict.common

import kotlin.test.*

internal class ClosedRangeRelationTest {
    private companion object {
        fun validate(
            expected: ClosedRangeRelation,
            merge: Boolean,
            reverseMerge: Boolean,
            s1: Int,
            e1: Int,
            s2: Int,
            e2: Int,
        ) {
            val r1 = (s1..e1)
            val r2 = (s2..e2)
            assertEquals(expected, ClosedRangeRelation.of(r1, r2))
            assertEquals(expected.reverse(), ClosedRangeRelation.of(r2, r1))
            assertEquals(merge, r1.canMerge(r2))
            if (!merge && reverseMerge) assertTrue { r1.canMerge(r2, allowReverse = true) }
            assertEquals(reverseMerge, r2.canMerge(r1))
            if (!reverseMerge && merge) assertTrue { r2.canMerge(r1, allowReverse = true) }
        }

        fun basicValidation(s1: Int, e1: Int, s2: Int, e2: Int): ClosedRangeRelation {
            val r1 = (s1..e1)
            val r2 = (s2..e2)
            val relation = ClosedRangeRelation.of(r1, r2)
            assertNotNull(relation)
            when {
                r1.isEmpty() || r2.isEmpty() -> assertEquals(ClosedRangeRelation.EMPTY, relation)
                r1 == r2 -> assertEquals(ClosedRangeRelation.EQUALS, relation)
            }
            return relation
        }

        fun <T : Comparable<T>> Sequence<ClosedRange<T>>.merge(): Sequence<ClosedRange<T>> =
            iterator().merge().asSequence()

        fun ClosedRange<*>.assertSameRange(other: ClosedRange<*>?) =
            assertTrue { start == other!!.start && endInclusive == other.endInclusive }

        fun Sequence<ClosedRange<*>>.assertSameSequence(other: Sequence<ClosedRange<*>>) {
            val iterator = other.iterator()
            forEach { it.assertSameRange(iterator.next()) }
            assertFalse { iterator.hasNext() }
        }
    }

    @Test
    fun `check range`() {
        validate(ClosedRangeRelation.EMPTY, false, false, 2, 1, 1, 2)
        validate(ClosedRangeRelation.EMPTY, false, false, 2, 1, 2, 1)
        validate(ClosedRangeRelation.EQUALS, true, true, 1, 2, 1, 2)
        validate(ClosedRangeRelation.AFTER, false, false, 3, 4, 1, 2)
        validate(ClosedRangeRelation.CONTAINS, true, false, 1, 5, 2, 4)
        validate(ClosedRangeRelation.CONTAINS, true, false, 1, 5, 2, 5)
        validate(ClosedRangeRelation.CONTAINS, true, false, 1, 5, 1, 4)
        validate(ClosedRangeRelation.OVERLAPS, true, false, 1, 5, 2, 6)
    }

    @Test
    fun `check all combinations`() {
        var count = 0
        val s1 = 1234 // 2^1 - 1=1
        val stats = mutableMapOf<ClosedRangeRelation, Int>()
        ((s1 - 4)..(s1 + 4) step 4).forEach { e1 -> // 2^2 - 1 = 3
            ((s1 - 4 - 2)..(s1 + 4 + 2) step 2).forEach { s2 -> // 2^3 - 1 = 7
                ((s1 - 4 - 2 - 1)..(s1 + 4 + 2 + 1)).forEach { e2 -> // 2^4 - 1  = 15
                    count++
                    stats.merge(basicValidation(s1, e1, s2, e2), 1, Int::plus)
                }
            }
        }
        assertEquals(1 * 3 * 7 * 15, count) // 3*7*15 = 315
        assertEquals(count, stats.values.sum())
        assertEquals(
            mapOf(
                ClosedRangeRelation.EMPTY to 203,
                ClosedRangeRelation.CONTAINED to 46,
                ClosedRangeRelation.AFTER to 24,
                ClosedRangeRelation.BEFORE to 14,
                ClosedRangeRelation.OVERLAPPED to 12,
                ClosedRangeRelation.CONTAINS to 8,
                ClosedRangeRelation.OVERLAPS to 6,
                ClosedRangeRelation.EQUALS to 2,
            ), stats
        )
    }

    @Test
    fun `reverse reverse`() {
        ClosedRangeRelation.values().forEach { assertEquals(it, it.reverse().reverse()) }
    }

    @Test
    fun `simple merges`() {
        (1..5).assertSameRange((1..5).merge(1..5))
        (1..5).assertSameRange((1..3).merge(2..5))
        (1..5).assertSameRange((2..5).merge(1..3, allowReverse = true))
    }

    @Test
    fun `cannot merge`() {
        assertNull((6..6.dec()).merge(6..6.dec(), allowReverse = true))
        assertNull((2..5).merge(6..8, allowReverse = true))
    }

    @Test
    fun `range sort by start`() {
        sequenceOf(1..2, 3..3, 4..8).assertSameSequence(
            sequenceOf(3..3, 1..2, 4..8).sortedBy(ClosedRange<Int>::start)
        )
    }

    @Test
    fun `simple merge sequence`() {
        (1..9).assertSameRange(sequenceOf(1..3, 2..5, 5..9).merge().single())
    }

    @Test
    fun `merge sequence`() {
        sequenceOf(1..5, 7..12).assertSameSequence(sequenceOf(1..3, 2..5, 7..9, 8..12).merge())
    }

    @Test
    fun `invalid merge sequence`() {
        assertFails { sequenceOf(2..5, 1..3).merge().count() }
        assertFails { sequenceOf(2..5, 4..7, 3..8).merge().count() }
    }

}
