/**
 * Cartesian Product related functions.  Using Sequence instead of Set to allow duplicate.
 * Some type aliases closely following the math naming:
 *  * [Family][https://en.wikipedia.org/wiki/Indexed_family]
 *  * [Tuple][https://en.wikipedia.org/wiki/Tuple]
 *  * [CartesianProduct][https://en.wikipedia.org/wiki/Cartesian_product]
 */

package com.arxict.common

typealias Family<T> = Sequence<T>
typealias Tuple = Sequence<Any?>
typealias CartesianProduct = Sequence<Tuple>

private val emptyTuple: Tuple = emptySequence()
private val zeroDCartesianProduct: CartesianProduct = sequenceOf(emptyTuple)

fun <T> Family<T>.toCartesianProduct(tuple: Tuple): CartesianProduct =
    map(tuple::plus)

fun <T> CartesianProduct.addFamily(family: Family<T>): CartesianProduct =
    flatMap(family::toCartesianProduct)

fun Sequence<Family<*>>.toCartesianProduct(): CartesianProduct =
    fold(zeroDCartesianProduct, CartesianProduct::addFamily)

fun <T, U> Family<T>.cartesianProduct(other: Family<U>): Sequence<Pair<T, U>> =
    flatMap { other.map(it::to) }
