package com.example.repeatingalarmfoss.helper.rx

import com.example.repeatingalarmfoss.helper.FlightRecorder
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BaseComposers @Inject constructor(private val schedulers: SchedulersProvider, private val logger: FlightRecorder) {
    fun <T> applySingleSchedulers(): SingleTransformer<T, T> =
        SingleTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(stackTrace = error.stackTrace) }
        }

    fun <T> applyMaybeSchedulers(): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(stackTrace = error.stackTrace) }
        }

    fun <T> applyObservableSchedulers(): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(stackTrace = error.stackTrace) }
        }

    fun applyCompletableSchedulers(): CompletableTransformer =
        CompletableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(stackTrace = error.stackTrace) }
        }

    fun <T> commonSingleFetchTransformer(): SingleTransformer<T, T> =
        SingleTransformer {
            it.retry(RxHttpErrorHandler())
                .compose(applySingleSchedulers())
        }

    fun commonCompletableFetchTransformer(): CompletableTransformer =
        CompletableTransformer {
            it.retry(RxHttpErrorHandler())
                .compose(applyCompletableSchedulers())
        }

    fun <T> commonMaybeFetchTransformer(): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.retry(RxHttpErrorHandler())
                .compose(applyMaybeSchedulers())
        }

    fun <T> commonObservableFetchTransformer(): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.retry(RxHttpErrorHandler())
                .compose(applyObservableSchedulers())
        }
}