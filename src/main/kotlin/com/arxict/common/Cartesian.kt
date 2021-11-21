package com.arxict.common

typealias Family<T> = Sequence<T> // https://en.wikipedia.org/wiki/Indexed_family
typealias Tuple = Sequence<Any?> // https://en.wikipedia.org/wiki/Tuple
typealias CartesianProduct = Sequence<Tuple> // https://en.wikipedia.org/wiki/Cartesian_product

fun <T> Family<T>.toCartesianProduct(tuple: Tuple): CartesianProduct =
    map(tuple::plus)

fun <T> CartesianProduct.addFamily(family: Family<T>): CartesianProduct =
    flatMap(family::toCartesianProduct)

fun Sequence<Family<*>>.toCartesianProduct(): CartesianProduct =
    fold(zeroDCartesianProduct, CartesianProduct::addFamily)

fun <T, U> Family<T>.cartesianProduct(other: Family<U>): Sequence<Pair<T, U>> =
    flatMap { other.map(it::to) }

private val emptyTuple: Tuple = emptySequence()
private val zeroDCartesianProduct: CartesianProduct = sequenceOf(emptyTuple)
