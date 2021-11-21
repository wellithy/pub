package com.arxict.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CartesianTest {
    private companion object {
        val Family<*>.familySize: Int get() = count()
        val Sequence<Family<*>>.cartesianProductArity: Int get() = count()
        val Sequence<Family<*>>.cartesianProductSize: Int get() = map{it.familySize}.reduce(Math::multiplyExact)
        val Tuple.tupleArity: Int get() = count()
        val CartesianProduct.cpSize: Int get() = count()
        val CartesianProduct.cpArity: Int? get() = first().tupleArity.takeIf { all { tuple -> tuple.tupleArity == it } }

        val intFamily5: Family<Int> = sequenceOf(1, 2, 3, 4, 5)
        val stringFamily2: Family<String> = sequenceOf("A", "B")
        val doubleFamily3: Family<Double> = sequenceOf(1.1, 2.2, 3.3)
        val families: Sequence<Family<*>> = sequenceOf(intFamily5, stringFamily2, doubleFamily3)
        const val cartesianProductDimension: Int = 5 * 2 * 3
        const val cartesianProductArity: Int = 3
        val intStringTuple1: Tuple = sequenceOf(1, "A")
        val Sequence<Any?>.theString: String get() = joinToString(" ")
        val CartesianProduct.allStrings: String get() = joinToString("|") { it.theString }

        fun <T, U> Family<T>.cartesianProductAlt(other: Family<U>): Sequence<Pair<T, U>> =
         sequenceOf(this, other).toCartesianProduct().map {
             @Suppress("UNCHECKED_CAST")
             it.first() as T to it.drop(1).first() as U
         }
    }

    @Test
    fun `2-D Cartesian Product`(){
        val cp: Sequence<Pair<Int, Double>> = intFamily5.cartesianProduct(doubleFamily3)
        assertEquals(intFamily5.familySize * doubleFamily3.familySize, cp.count())
        assertEquals(cp.toList(), intFamily5.cartesianProductAlt(doubleFamily3).toList())
    }

    @Test
    fun `invalid Cartesian Product`(){
        assertNull(sequenceOf(sequenceOf(1), sequenceOf(1, 2)).cpArity)
    }

    @Test
    fun `Sequence of families to Cartesian Product`(){
        val cp:CartesianProduct = families.toCartesianProduct()
        assertEquals(families.cartesianProductSize, cp.cpSize)
        assertEquals(families.cartesianProductArity, cp.cpArity)
        assertEquals(
        "1 A 1.1|1 A 2.2|1 A 3.3|1 B 1.1|1 B 2.2|1 B 3.3|2 A 1.1|2 A 2.2|2 A 3.3|2 B 1.1|2 B 2.2|2 B 3.3|3 A 1.1|3 A 2.2|3 A 3.3|3 B 1.1|3 B 2.2|3 B 3.3|4 A 1.1|4 A 2.2|4 A 3.3|4 B 1.1|4 B 2.2|4 B 3.3|5 A 1.1|5 A 2.2|5 A 3.3|5 B 1.1|5 B 2.2|5 B 3.3"
        ,cp.allStrings)
    }

    @Test
    fun `cartesian product and family to cartesian product`(){
        val cp:CartesianProduct = doubleFamily3.toCartesianProduct(intStringTuple1)
        val family:Family<Char> = sequenceOf('X', 'Y')
        val newCp:CartesianProduct = cp.addFamily(family)
        assertEquals("1 A 1.1 X|1 A 1.1 Y|1 A 2.2 X|1 A 2.2 Y|1 A 3.3 X|1 A 3.3 Y", newCp.allStrings)
        assertEquals(cp.cpArity!! + 1, newCp.cpArity!!)
        assertEquals(cp.cpSize * family.familySize, newCp.cpSize)
    }

    @Test
    fun `family and tuple to cartesian product`() {
        assertEquals("1 A 1.1|1 A 2.2|1 A 3.3", doubleFamily3.toCartesianProduct(intStringTuple1).allStrings)
        val family:Family<Int> = IntArray(10).asSequence()
        val tuple:Tuple = IntArray(7).asSequence()
        val cp:CartesianProduct = family.toCartesianProduct(tuple)
        assertEquals(family.familySize, cp.cpSize)
        assertEquals(tuple.tupleArity + 1, cp.cpArity)
    }

    @Test
    fun basic() {
        assertEquals(5, intFamily5.familySize)
        assertEquals(cartesianProductDimension, families.cartesianProductSize)
        assertEquals(cartesianProductArity, families.cartesianProductArity)
        assertEquals(2, intStringTuple1.tupleArity)
    }
}
