package com.example.repeatingalarmfoss.helper

import java.util.*

class FixedSizeBitSet(private val bitsAmount: Int) : BitSet(bitsAmount) {
    companion object {
        fun fromBinaryString(binaryString: String) = FixedSizeBitSet(binaryString.length).apply {
            binaryString.toCharArray().forEachIndexed { index, char ->
                when(char) {
                    '0' -> clear(index)
                    '1' -> set(index)
                }
            }
        }
    }
    override fun toString(): String = IntRange(0, bitsAmount).map { if(get(it)) '1' else '0' }.joinToString(separator = "")
}