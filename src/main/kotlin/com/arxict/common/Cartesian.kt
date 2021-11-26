/**
 * Cartesian Product related functions.  Using Sequence instead of Set to allow duplicate and infinite source.
 * Some type aliases closely following the math naming:
 *  * [Family][https://en.wikipedia.org/wiki/Indexed_family]
 *  * [Tuple][https://en.wikipedia.org/wiki/Tuple]
 *  * [CartesianProduct][https://en.wikipedia.org/wiki/Cartesian_product]
 */

package com.arxict.common

// Using "private typealias" is to avoid corrupting global name space
private typealias Family<T> = Sequence<T>
private typealias Tuple = Sequence<Any?>
private typealias CartesianProduct = Sequence<Tuple>

private val emptyTuple: Tuple = emptySequence()
private val zeroDCartesianProduct: CartesianProduct = sequenceOf(emptyTuple)

val <T> T.asSingleton: Tuple
    get() = sequenceOf(this)

val <T> Family<T>.asCartesianProduct: CartesianProduct
    get() = map { it.asSingleton }

fun <T> Family<T>.appendTo(tuple: Tuple): CartesianProduct =
    map(tuple::plus)

fun <T> CartesianProduct.addFamily(family: Family<T>): CartesianProduct =
    flatMap(family::appendTo)

fun Sequence<Family<*>>.toCartesianProduct(): CartesianProduct =
    fold(zeroDCartesianProduct, CartesianProduct::addFamily)

fun <T, U> Family<T>.cartesianProduct(other: Family<U>): Sequence<Pair<T, U>> =
    flatMap { other.map(it::to) }
