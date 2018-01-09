package com.github.giacomoparisi.droidbox.architecture.model.ui

import android.app.Application
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.annotation.StringRes
import android.widget.Toast
import com.github.giacomoparisi.droidbox.R
import com.github.giacomoparisi.droidbox.architecture.model.exception.ManagedException
import javax.inject.Inject

/**
 * Created by Giacomo Parisi on 30/06/2017.
 * https://github.com/giacomoParisi
 */
open class DroidUIManager @Inject constructor(private val application: Application) {

    /* ============= FIELDS ============= */

    // True if error view is needed
    var error = ObservableBoolean()

    // Class type of the last error produced
    var lastError: Class<Throwable>? = null

    // Id of the last error produced
    var lastErrorCode: Int = 0

    // Error message text
    var errorMessage = ObservableField<String>()

    // Retry button text
    var retryButtonMessage = ObservableField<String>()

    // True if loading view is needed
    var loading = ObservableBoolean()

    // Title text
    var title = ObservableInt()

    var defaultErrorMessage = R.string.ERROR_DefaultMessage
    var defaultRetryMessage = R.string.ERROR_Retry


    /* ============= ERROR / LOADING  ============= */

    fun showError(throwable: Throwable, errorCode: Int = 0) {
        hideLoading()
        errorMessage.set(getErrorMessage(throwable))
        retryButtonMessage.set(getRetryMessage(throwable))
        lastError = throwable.javaClass
        lastErrorCode = errorCode
        error.set(true)
    }

    fun showLoading() {
        loading.set(true)
    }

    fun hideLoading() {
        loading.set(false)
    }

    fun hideError() {
        error.set(false)
    }

    fun getErrorMessage(throwable: Throwable): String? {
        return if (throwable is ManagedException) {
            if (throwable.errorMessageRes != 0) {
                application.getString(throwable.errorMessageRes)
            } else {
                throwable.errorMessage
            }
        } else {
            application.getString(defaultErrorMessage)
        }
    }

    fun getRetryMessage(throwable: Throwable): String? {
        return if (throwable is ManagedException) {
            if (throwable.retryButtonLabelId != 0) {
                application.getString(throwable.retryButtonLabelId)
            } else {
                throwable.retryButtonLabel
            }
        } else {
            application.getString(defaultRetryMessage)
        }
    }

    /* ============= ANDROID NATIVE UI ============= */

    /**
     * Show toast message in the activities that observe the current droidViewModel
     *
     * @param message String resource id of the toast message
     * @param toastDuration Duration id of toast, it can be Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    fun showToast(@StringRes message: Int, toastDuration: Int, droidUIActions: DroidUIActions) {
        droidUIActions {
            Toast.makeText(it, message, toastDuration).show()
        }
    }

    /**
     * Show toast message in the activities that observe the current droidViewModel
     *
     * @param message String message
     * @param toastDuration Duration id of toast, it can be Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    fun showToast(message: String, toastDuration: Int, droidUIActions: DroidUIActions) {
        droidUIActions {
            Toast.makeText(it, message, toastDuration).show()
        }
    }
}