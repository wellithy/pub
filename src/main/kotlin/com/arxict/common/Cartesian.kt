package com.arxict.common

typealias Family<T> = Sequence<T>
typealias Tuple = Sequence<Any?>
typealias CartesianProduct = Sequence<Tuple>

private val emptyCartesianProduct = sequenceOf(emptySequence<Any?>())

fun <T> Family<T>.toCartesianProduct(tuple: Tuple): CartesianProduct =
    map(tuple::plus)

fun <T> CartesianProduct.addFamily(family: Family<T>): CartesianProduct =
    flatMap(family::toCartesianProduct)

fun Sequence<Family<*>>.toCartesianProduct(): CartesianProduct =
    fold(emptyCartesianProduct, CartesianProduct::addFamily)

fun <T, U> Family<T>.cartesianProduct(other: Family<U>): Sequence<Pair<T, U>> =
    flatMap { other.map(it::to) }
