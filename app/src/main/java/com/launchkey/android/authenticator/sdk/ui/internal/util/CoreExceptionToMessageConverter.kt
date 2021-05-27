package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.content.Context
import com.launchkey.android.authenticator.sdk.core.exception.*
import com.launchkey.android.authenticator.sdk.ui.R
import java.util.*

object CoreExceptionToMessageConverter {
    fun convert(exception: Exception, context: Context): String {
        Objects.requireNonNull(exception)
        Objects.requireNonNull(context)
        return try {
            throw exception
        } catch (e: CommunicationException) {
            context.getString(R.string.ioa_error_c_format, e.message)
        } catch (e: DataDecryptionException) {
            context.getString(R.string.ioa_error_unknown_format, e.message)
        } catch (e: DeviceAlreadyLinkedException) {
            context.getString(R.string.ioa_error_unknown_format, e.message)
        } catch (e: DeviceNotFoundException) {
            context.getString(R.string.ioa_error_dnf)
        } catch (e: DeviceNotLinkedException) {
            context.getString(R.string.ioa_error_dnl)
        } catch (e: DeviceNameAlreadyUsedException) {
            context.getString(R.string.ioa_error_api_devicenameused)
        } catch (e: InvalidLinkingCodeException) {
            context.getString(R.string.ioa_error_api_wronglinkingcode)
        } catch (e: IncorrectSdkKeyException) {
            context.getString(R.string.ioa_error_api_wrongsdkkey)
        } catch (e: LaunchKeyApiException) {
            context.getString(R.string.ioa_error_api_unknown_format, e.message)
        } catch (e: MalformedJwtException) {
            context.getString(R.string.ioa_error_unknown_format, e.message)
        } catch (e: MalformedLinkingCodeException) {
            context.getString(R.string.ioa_error_mlc)
        } catch (e: NoInternetConnectivityException) {
            context.getString(R.string.ioa_error_nic)
        } catch (e: NoSignatureHeaderException) {
            context.getString(R.string.ioa_error_unknown_format, e.message)
        } catch (e: RequestArgumentException) {
            context.getString(R.string.ioa_error_ra_format, e.message)
        } catch (e: UnexpectedCertificateException) {
            context.getString(R.string.ioa_error_uc)
        } catch (e: UnexpectedPolicyFormatException) {
            context.getString(R.string.ioa_error_upfe)
        } catch (e: UnexpectedPolicyTypeException) {
            context.getString(R.string.ioa_error_upte)
        } catch (e: Exception) {
            context.getString(R.string.ioa_error_unknown_format, e.message)
        }
    }
}