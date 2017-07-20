package com.github.jackparisi.droidbox.network

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


/**
 * Created by Giacomo Parisi on 09/07/2017.
 * https://github.com/JackParisi
 */
abstract class DroidRxRepository<ResultType> {

    private var result: Flowable<DroidResource<ResultType>>

    init {

        result = Flowable.create({ emitter: FlowableEmitter<DroidResource<ResultType>> ->
            startRepository(emitter)
        }, BackpressureStrategy.BUFFER)

    }

    private fun startRepository(emitter: FlowableEmitter<DroidResource<ResultType>>) {
        val dbSource = loadFromDb()
        if(dbSource != null) {
            dbSource.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ data: ResultType ->
                        if (shouldFetch(data)) {
                            fetchFromNetwork(data, emitter)
                        } else {
                            emitter.onNext(DroidResource.NetworkSuccess(data))
                        }
                    }, { throwable ->
                        emitter.onNext(DroidResource.NetworkError(throwable))
                        Timber.e(throwable.message)
                    })
        }else if(shouldFetch(null)){
            fetchFromNetwork(null, emitter)
        }
    }

    private fun fetchFromNetwork(dbSource: ResultType?, emitter: FlowableEmitter<DroidResource<ResultType>>) {
        val apiResponse = createCall()

        //TODO check if repository wants emit database value
        if(dbSource != null) {
            emitter.onNext(DroidResource.NetworkSuccess(dbSource))
        }

        apiResponse?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ data ->
                    if(data != null) {
                        saveResultAndReInit(data)
                    }
                }, { throwable -> emitter.onNext(DroidResource.NetworkError(throwable)) })
    }

    private fun saveResultAndReInit(apiResponse: ResultType) {
        val save = Single.create(SingleOnSubscribe<ResultType> { e ->
            saveCallResult(apiResponse)
            e.onSuccess(apiResponse)
        })

        save.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ loadFromDb() }, { })
    }

    // Called to save the result of the API response into the database
    @WorkerThread
    protected abstract fun saveCallResult(data: ResultType?)

    // Called with the data in the database to decide whether it should be
    // fetched from the network.
    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    // Called to get the cached data from the database
    @MainThread
    protected abstract fun loadFromDb(): Flowable<ResultType>?

    // Called to create the API call.
    @MainThread
    protected abstract fun createCall(): Single<ResultType>?

    // Called when the fetch fails. The child class may want to reset components
    // like rate limiter.
    @MainThread
    protected fun onFetchFailed() {
    }

    // returns a LiveData that represents the resource
    fun getAsObservable(): Flowable<DroidResource<ResultType>> {
        return result
    }
}