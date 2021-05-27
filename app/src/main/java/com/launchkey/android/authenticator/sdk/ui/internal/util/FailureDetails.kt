package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java.util.*

class FailureDetails {
    @StringRes
    private val mErrorTitleResourceId: Int

    @StringRes
    private val mErrorDetailsStringResourceId: Int
    private val mFormatArgs: List<List<Int>>?
    private val mValueSeparatingFormatArgs: String?
    private val mValueSeparatingFormatFormatArgs: String?

    @PluralsRes
    private val mErrorDetailsPluralsResourceId: Int
    private val mQuantityArgs: Int
    private val mIsPlural: Boolean

    constructor(@StringRes errorTitleResourceid: Int,
                @StringRes errorDetailsStringResourceId: Int) {
        mErrorTitleResourceId = errorTitleResourceid
        mErrorDetailsStringResourceId = errorDetailsStringResourceId
        mValueSeparatingFormatArgs = ""
        mValueSeparatingFormatFormatArgs = ""
        mFormatArgs = ArrayList()
        mErrorDetailsPluralsResourceId = 0
        mQuantityArgs = 0
        mIsPlural = false
    }

    internal constructor(@StringRes errorTitleResourceid: Int,
                         @StringRes errorDetailsStringResourceId: Int,
                         valueSeparatingFormatArgs: String?,
                         valueSeparatingFormatFormatArgs: String?,
                         formatArgs: List<List<Int>>?) {
        mErrorTitleResourceId = errorTitleResourceid
        mErrorDetailsStringResourceId = errorDetailsStringResourceId
        mValueSeparatingFormatArgs = valueSeparatingFormatArgs
        mValueSeparatingFormatFormatArgs = valueSeparatingFormatFormatArgs
        mFormatArgs = formatArgs
        mErrorDetailsPluralsResourceId = 0
        mQuantityArgs = 0
        mIsPlural = false
    }

    internal constructor(@StringRes errorTitleResourceid: Int,
                         @PluralsRes errorDetailsPluralsResourceId: Int,
                         quantityArgs: Int) {
        mErrorTitleResourceId = errorTitleResourceid
        mErrorDetailsStringResourceId = 0
        mValueSeparatingFormatArgs = null
        mValueSeparatingFormatFormatArgs = null
        mFormatArgs = null
        mErrorDetailsPluralsResourceId = errorDetailsPluralsResourceId
        mQuantityArgs = quantityArgs
        mIsPlural = true
    }

    fun getTitle(r: Resources): String {
        return r.getString(mErrorTitleResourceId)
    }

    fun getMessage(r: Resources): String {
        return if (mIsPlural) {
            r.getQuantityString(mErrorDetailsPluralsResourceId, mQuantityArgs, mQuantityArgs)
        } else {
            val formatArgs = StringBuilder()
            val formatArgsSize = mFormatArgs!!.size
            for (i in 0 until formatArgsSize) {
                val formatFormatArgsSize = mFormatArgs[i].size
                for (j in 0 until formatFormatArgsSize) {
                    formatArgs.append(r.getString(mFormatArgs[i][j]))
                    if (j < formatFormatArgsSize - 1) {
                        formatArgs.append(mValueSeparatingFormatFormatArgs)
                    }
                }
                if (i < formatArgsSize - 1) {
                    formatArgs.append(mValueSeparatingFormatArgs)
                }
            }
            r.getString(mErrorDetailsStringResourceId, formatArgs.toString())
        }
    }
}