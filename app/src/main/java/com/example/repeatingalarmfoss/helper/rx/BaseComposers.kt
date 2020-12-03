package com.example.repeatingalarmfoss.helper.rx

import com.example.repeatingalarmfoss.helper.FlightRecorder
import io.reactivex.CompletableTransformer
import io.reactivex.MaybeTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import javax.inject.Inject

class BaseComposers @Inject constructor(private val schedulers: SchedulersProvider, private val logger: FlightRecorder) {
    fun <T> applySingleSchedulers(): SingleTransformer<T, T> =
        SingleTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
        }

    fun <T> applyMaybeSchedulers(): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
        }

    fun <T> applyObservableSchedulers(): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
        }

    fun applyCompletableSchedulers(): CompletableTransformer =
        CompletableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
        }

    fun <T> commonSingleFetchTransformer(): SingleTransformer<T, T> =
        SingleTransformer {
            it.retry(RxHttpErrorHandler())
                .compose(applySingleSchedulers())
                .doOnError { error -> logger.e(stackTrace = error.stackTrace) }
        }

    fun commonCompletableFetchTransformer(): CompletableTransformer =
        CompletableTransformer {
            it.retry(RxHttpErrorHandler())
                .compose(applyCompletableSchedulers())
                .doOnError { error -> logger.e(stackTrace = error.stackTrace) }
        }

    fun <T> commonObservableFetchTransformer(): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.retry(RxHttpErrorHandler())
                .compose(applyObservableSchedulers())
                .doOnError { error -> logger.e(stackTrace = error.stackTrace) }
        }
}