package utils


fun <A> List<A>.createUniquePairList(): List<Pair<A, A>> {
    return this.flatMapIndexed { index, a ->
        this.subList(index + 1, this.size).map { b -> Pair(a, b) }
    }
}

fun <T> List<T>.removeAtNew(index: Int): List<T> {
    return this.toMutableList().apply { removeAt(index) }
}

fun <T> Set<T>.removeAtNew(index: Int): Set<T> {
    return this.toMutableList().apply { removeAt(index) }.toSet()
}

fun <T> List<T>.setNew(index: Int, element: T): List<T> {
    return this.toMutableList().apply { set(index, element) }
}

fun <T> Set<T>.setNew(index: Int, element: T): Set<T> {
    return this.toMutableList().apply { set(index, element) }.toSet()
}