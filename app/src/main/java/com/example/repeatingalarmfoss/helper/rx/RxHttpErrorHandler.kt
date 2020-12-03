package com.example.repeatingalarmfoss.helper.rx

import io.reactivex.functions.BiPredicate

class RxHttpErrorHandler(private var numberOfRetry: Int = 3) : BiPredicate<Int, Throwable> {
    override fun test(t1: Int, t2: Throwable) = t1 < numberOfRetry
//    override fun call(integer: Int, error: Throwable) = (integer < numberOfRetry /*&& error is RetrofitException*/)/* && (error.kind == RetrofitException.Kind.NETWORK || error.response.code() == 500)*/
}

