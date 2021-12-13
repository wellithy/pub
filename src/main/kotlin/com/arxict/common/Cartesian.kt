/**
 * Cartesian Product related functions.  Using Sequence instead of Set to allow duplicate and infinite source.
 * Some type aliases closely following the math naming:
 *  * [Singleton][https://en.wikipedia.org/wiki/Singleton_(mathematics)]
 *  * [Family][https://en.wikipedia.org/wiki/Indexed_family]
 *  * [Tuple][https://en.wikipedia.org/wiki/Tuple]
 *  * [CartesianProduct][https://en.wikipedia.org/wiki/Cartesian_product]
 */

package com.arxict.common

// Using "private typealias" is to avoid corrupting global name space
private typealias Element = Any?
private typealias Family = Sequence<Element>
private typealias Tuple = Sequence<Element>
private typealias CartesianProduct = Sequence<Tuple>

private val zeroDCartesianProduct: CartesianProduct = sequenceOf(emptySequence())

val Element.asSingleton: Tuple
    get() = sequenceOf(this)

val Family.asCartesianProduct: CartesianProduct
    get() = map { it.asSingleton }

fun Family.appendTo(tuple: Tuple): CartesianProduct =
    map(tuple::plus)

fun CartesianProduct.addFamily(family: Family): CartesianProduct =
    flatMap(family::appendTo)

fun Sequence<Family>.toCartesianProduct(): CartesianProduct =
    fold(zeroDCartesianProduct, CartesianProduct::addFamily)

fun Family.cartesianProduct(dimension: Int): CartesianProduct =
    generateSequence { this }.take(dimension).toCartesianProduct()

fun <T, U> Sequence<T>.cartesianProduct(that: Sequence<T>): Sequence<Pair<T, U>> =
    sequenceOf(this, that).toCartesianProduct().map {
        val iterator = it.iterator()
        @Suppress("UNCHECKED_CAST")
        iterator.next() as T to iterator.next() as U
    }
