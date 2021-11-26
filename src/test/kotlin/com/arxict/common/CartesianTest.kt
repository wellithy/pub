package com.arxict.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The following is copied from Cartesian.kt for convenience, changed the names to enforce non-global-usage
private typealias FamilyTest<T> = Sequence<T>
private typealias TupleTest = Sequence<Any?>
private typealias CartesianProductTest = Sequence<TupleTest>

internal class CartesianTest {
    private companion object {
        val FamilyTest<*>.familySize: Int get() = count()
        val Sequence<FamilyTest<*>>.cartesianProductArity: Int get() = count()
        val Sequence<FamilyTest<*>>.cartesianProductSize: Int get() = map { it.familySize }.reduce(Math::multiplyExact)
        val TupleTest.tupleArity: Int get() = count()
        val CartesianProductTest.cpSize: Int get() = count()
        val CartesianProductTest.cpArity: Int? get() = firstOrNull()?.tupleArity.takeIf { all { tuple -> tuple.tupleArity == it } }

        val intFamily5: FamilyTest<Int> = sequenceOf(1, 2, 3, 4, 5)
        val stringFamily2: FamilyTest<String> = sequenceOf("A", "B")
        val doubleFamily3: FamilyTest<Double> = sequenceOf(1.1, 2.2, 3.3)
        val families: Sequence<FamilyTest<*>> = sequenceOf(intFamily5, stringFamily2, doubleFamily3)
        const val cartesianProductDimension: Int = 5 * 2 * 3
        const val cartesianProductArity: Int = 3
        val intStringTuple1: TupleTest = sequenceOf(1, "A")
        val Sequence<Any?>.theString: String get() = joinToString(" ")
        val CartesianProductTest.allStrings: String get() = joinToString("|") { it.theString }

        @Suppress("UNCHECKED_CAST")
        fun <T, U> TupleTest.asPair(): Pair<T, U> =
            first() as T to drop(1).first() as U

        fun <T, U> FamilyTest<T>.cartesianProductAlt(other: FamilyTest<U>): Sequence<Pair<T, U>> =
            sequenceOf(this, other).toCartesianProduct().map { it.asPair() }
    }

    @Test
    fun `singleton and 1-D Cartesian Product`(){
        assertEquals(1, (9.asSingleton).tupleArity)
        intFamily5.asCartesianProduct.apply {
            assertEquals(5, cpSize)
            assertEquals(1, cpArity)
        }
    }

    @Test
    fun `0-D Cartesian Product`() {
        emptySequence<FamilyTest<*>>().toCartesianProduct().apply {
            assertEquals(1, cpSize)
            assertEquals(0, cpArity)
        }

        val emptyFamily: FamilyTest<Nothing> = emptySequence()
        sequenceOf(emptyFamily).toCartesianProduct().apply {
            assertEquals(0, cpSize)
            assertNull(cpArity)
        }
    }

    @Test
    fun `2-D Cartesian Product`() {
        intFamily5.cartesianProduct(doubleFamily3).apply {
            assertEquals(intFamily5.familySize * doubleFamily3.familySize, count())
            assertEquals(toList(), intFamily5.cartesianProductAlt(doubleFamily3).toList())
        }
    }

    @Test
    fun `invalid Cartesian Product`() {
        assertNull(sequenceOf(sequenceOf(1), sequenceOf(1, 2)).cpArity)
    }

    @Test
    fun `Sequence of families to Cartesian Product`() {
        val cp: CartesianProductTest = families.toCartesianProduct()
        assertEquals(families.cartesianProductSize, cp.cpSize)
        assertEquals(families.cartesianProductArity, cp.cpArity)
        assertEquals(
            "1 A 1.1|1 A 2.2|1 A 3.3|1 B 1.1|1 B 2.2|1 B 3.3|2 A 1.1|2 A 2.2|2 A 3.3|2 B 1.1|2 B 2.2|2 B 3.3|3 A 1.1|3 A 2.2|3 A 3.3|3 B 1.1|3 B 2.2|3 B 3.3|4 A 1.1|4 A 2.2|4 A 3.3|4 B 1.1|4 B 2.2|4 B 3.3|5 A 1.1|5 A 2.2|5 A 3.3|5 B 1.1|5 B 2.2|5 B 3.3",
            cp.allStrings
        )
    }

    @Test
    fun `cartesian product and family to cartesian product`() {
        val cp: CartesianProductTest = doubleFamily3.appendTo(intStringTuple1)
        val family: FamilyTest<Char> = sequenceOf('X', 'Y')
        val newCp: CartesianProductTest = cp.addFamily(family)
        assertEquals("1 A 1.1 X|1 A 1.1 Y|1 A 2.2 X|1 A 2.2 Y|1 A 3.3 X|1 A 3.3 Y", newCp.allStrings)
        assertEquals(cp.cpArity!! + 1, newCp.cpArity!!)
        assertEquals(cp.cpSize * family.familySize, newCp.cpSize)
    }

    @Test
    fun `family and tuple to cartesian product`() {
        assertEquals("1 A 1.1|1 A 2.2|1 A 3.3", doubleFamily3.appendTo(intStringTuple1).allStrings)
        val family: FamilyTest<Int> = IntArray(10).asSequence()
        val tuple: TupleTest = IntArray(7).asSequence()
        family.appendTo(tuple).apply {
            assertEquals(family.familySize, cpSize)
            assertEquals(tuple.tupleArity + 1, cpArity)
        }
    }

    @Test
    fun basic() {
        assertEquals(5, intFamily5.familySize)
        assertEquals(cartesianProductDimension, families.cartesianProductSize)
        assertEquals(cartesianProductArity, families.cartesianProductArity)
        assertEquals(2, intStringTuple1.tupleArity)
    }
}
