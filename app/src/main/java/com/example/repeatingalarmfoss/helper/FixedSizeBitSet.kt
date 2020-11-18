package com.example.repeatingalarmfoss.helper

import java.util.*

class FixedSizeBitSet(private val bitsAmount: Int) : BitSet(bitsAmount) {
    companion object {
        fun fromBinaryString(binaryString: String) = FixedSizeBitSet(binaryString.length).apply {
            binaryString.toCharArray().forEachIndexed { index, char ->
                when (char) {
                    '0' -> clear(index)
                    '1' -> set(index)
                }
            }
        }
    }

    override fun toString(): String = IntRange(0, bitsAmount).map { if (get(it)) '1' else '0' }.joinToString(separator = "")

    fun getChosenIndices(): List<Int> {
        val indices = mutableListOf<Int>()
        var i: Int = nextSetBit(0)
        while (i != -1) {
            indices.add(i)
            i = nextSetBit(i + 1)
        }
        return indices
    }

    /*fixme should be outlined?*/
    fun getNextIndex(currentIndex: Int): Int {
        if (currentIndex !in 1..7) throw IllegalArgumentException()
        val indices = getChosenIndices()
        return if (indices.indexOf(currentIndex) == indices.lastIndex) indices[0] else indices.getOrElse(indices.indexOf(currentIndex) + 1) { indices[0] }
    }
}