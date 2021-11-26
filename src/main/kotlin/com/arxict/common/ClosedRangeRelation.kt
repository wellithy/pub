package com.arxict.common

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
        fun <T : Comparable<T>, R : ClosedRange<T>> of(r1: R, r2: R): ClosedRangeRelation {
            val s1 = r1.start
            val e1 = r1.endInclusive
            val s2 = r2.start
            val e2 = r2.endInclusive
            return when {
                // It's critical to apply these conditions in the following _*specific order*_
                //    Equivalent Condition                  Relative Position
                r1.isEmpty() || r2.isEmpty() -> EMPTY  //  e1 < s1 || e2 < s2
                r1 == r2 -> EQUALS                     //  s1==s2 && e1==e2
                e2 < s1 -> AFTER                       //  s2 <= e2 <  s1 <= e1
                e1 < s2 -> BEFORE                      //  s1 <= e1 <  s2 <= e2
                s2 in r1 && e2 in r1 -> CONTAINS       //  s1 <= s2 <= e2 <= e1
                s1 in r2 && e1 in r2 -> CONTAINED      //  s2 <= s1 <= e1 <= e2
                s2 in r1 && e1 < e2 -> OVERLAPS        //  s1 <= s2 <= e1 <  e2
                s1 in r2 && e2 < e1 -> OVERLAPPED      //  s2 <= s1 <= e2 <  e1
                else -> error("Unhandled condition")
            }
        }
    }

    fun reverse(): ClosedRangeRelation = when (this) {
        EMPTY -> EMPTY
        EQUALS -> EQUALS
        AFTER -> BEFORE
        BEFORE -> AFTER
        CONTAINS -> CONTAINED
        CONTAINED -> CONTAINS
        OVERLAPS -> OVERLAPPED
        OVERLAPPED -> OVERLAPS
    }
}

fun <T : Comparable<T>, R : ClosedRange<T>> R.canMerge(other: R, allowReverse: Boolean = false): Boolean =
    when (ClosedRangeRelation.of(this, other)) {
        ClosedRangeRelation.EQUALS, ClosedRangeRelation.CONTAINS, ClosedRangeRelation.OVERLAPS -> true
        ClosedRangeRelation.CONTAINED, ClosedRangeRelation.OVERLAPPED -> allowReverse
        else -> false
    }

fun <T : Comparable<T>, R : ClosedRange<T>> R.merge(r2: R, allowReverse: Boolean = false): ClosedRange<T>? =
    when (ClosedRangeRelation.of(this, r2)) {
        ClosedRangeRelation.EQUALS, ClosedRangeRelation.CONTAINS -> this
        ClosedRangeRelation.OVERLAPS -> start..r2.endInclusive
        ClosedRangeRelation.CONTAINED -> if (allowReverse) r2 else null
        ClosedRangeRelation.OVERLAPPED -> if (allowReverse) r2.start..endInclusive else null
        else -> null
    }

fun <T : Comparable<T>> Iterator<ClosedRange<T>>.merge(): Iterator<ClosedRange<T>> {
    val myself = this
    var last: ClosedRange<T>? = null
    return object : AbstractIterator<ClosedRange<T>>() {
        override fun computeNext() {
            if (!myself.hasNext()) return done()
            val current = last ?: myself.next()
            last = null
            var finish = current.endInclusive
            var prevStart = current.start
            while (last == null && myself.hasNext())
                myself.next().apply {
                    require(start >= prevStart) { "Can't merge unsorted ClosedRange: $prevStart is followed by $start" }
                    prevStart = start
                    if (finish >= start) finish = endInclusive
                    else last = this
                }
            setNext(current.start..finish)
        }
    }
}
