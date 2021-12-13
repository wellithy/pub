package com.arxict.common

import java.time.LocalDate
import kotlin.test.*

// The following is copied from Cartesian.kt for convenience, changed the names to enforce non-global-usage
private typealias FamilyTest = Sequence<Any?>
private typealias TupleTest = Sequence<Any?>
private typealias CartesianProductTest = Sequence<TupleTest>

internal class CartesianTest {
    private companion object {
        val FamilyTest.familySize: Int get() = count()
        val Sequence<FamilyTest>.cartesianProductArity: Int get() = count()
        val Sequence<FamilyTest>.cartesianProductSize: Int get() = map { it.familySize }.reduce(Math::multiplyExact)
        val TupleTest.tupleArity: Int get() = count()
        val CartesianProductTest.cpSize: Int get() = count()
        val CartesianProductTest.cpArity: Int? get() = firstOrNull()?.tupleArity.takeIf { all { tuple -> tuple.tupleArity == it } }

        val intFamily5: FamilyTest = sequenceOf(1, 2, 3, 4, 5)
        val stringFamily2: FamilyTest = sequenceOf("A", "B")
        val doubleFamily3: FamilyTest = sequenceOf(1.1, 2.2, 3.3)
        val families: Sequence<FamilyTest> = sequenceOf(intFamily5, stringFamily2, doubleFamily3)
        const val cartesianProductDimension: Int = 5 * 2 * 3
        const val cartesianProductArity: Int = 3
        val intStringTuple1: TupleTest = sequenceOf(1, "A")
        val Sequence<Any?>.theString: String get() = joinToString(" ")
        val CartesianProductTest.allStrings: String get() = joinToString("|") { it.theString }
    }

    @Test
    fun `singleton and 1-D Cartesian Product`() {
        assertEquals(1, (9.asSingleton).tupleArity)
        intFamily5.asCartesianProduct.apply {
            assertEquals(5, cpSize)
            assertEquals(1, cpArity)
        }
    }

    @Test
    fun `0-D Cartesian Product`() {
        emptySequence<FamilyTest>().toCartesianProduct().apply {
            assertEquals(1, cpSize)
            assertEquals(0, cpArity)
        }

        val emptyFamily: FamilyTest = emptySequence()
        sequenceOf(emptyFamily).toCartesianProduct().apply {
            assertEquals(0, cpSize)
            assertNull(cpArity)
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
        val family: FamilyTest = sequenceOf('X', 'Y')
        val newCp: CartesianProductTest = cp.addFamily(family)
        assertEquals("1 A 1.1 X|1 A 1.1 Y|1 A 2.2 X|1 A 2.2 Y|1 A 3.3 X|1 A 3.3 Y", newCp.allStrings)
        assertEquals(cp.cpArity!! + 1, newCp.cpArity!!)
        assertEquals(cp.cpSize * family.familySize, newCp.cpSize)
    }

    @Test
    fun `family and tuple to cartesian product`() {
        assertEquals("1 A 1.1|1 A 2.2|1 A 3.3", doubleFamily3.appendTo(intStringTuple1).allStrings)
        val family: FamilyTest = IntArray(10).asSequence()
        val tuple: TupleTest = IntArray(7).asSequence()
        family.appendTo(tuple).apply {
            assertEquals(family.familySize, cpSize)
            assertEquals(tuple.tupleArity + 1, cpArity)
        }
    }

    @Test
    fun dimension() {
        sequenceOf(1, "A").cartesianProduct(3).apply {
            assertEquals("1 1 1|1 1 A|1 A 1|1 A A|A 1 1|A 1 A|A A 1|A A A", allStrings)
            assertEquals(8, count())
        }
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun cast() {
        val cp = sequenceOf('C', 'D').cartesianProduct(3)
        val typedCp: Sequence<Sequence<Char>> = cp as Sequence<Sequence<Char>>
        typedCp.flatMap { it }.forEach { assertTrue { it.isDefined() } }

        val wrongTypedCp: Sequence<Sequence<LocalDate>> = cp as Sequence<Sequence<LocalDate>>
        assertFails { wrongTypedCp.first().first().month }
    }

    @Test
    fun basic() {
        assertEquals(5, intFamily5.familySize)
        assertEquals(cartesianProductDimension, families.cartesianProductSize)
        assertEquals(cartesianProductArity, families.cartesianProductArity)
        assertEquals(2, intStringTuple1.tupleArity)
    }

}
