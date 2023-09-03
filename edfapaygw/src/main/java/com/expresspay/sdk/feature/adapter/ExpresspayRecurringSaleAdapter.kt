/*
 * Property of EdfaPg (https://edfapay.com).
 */

package com.edfapaygw.sdk.feature.adapter

import androidx.annotation.NonNull
import androidx.annotation.Size
import com.edfapaygw.sdk.core.EdfaPgCredential
import com.edfapaygw.sdk.feature.deserializer.EdfaPgSaleDeserializer
import com.edfapaygw.sdk.feature.service.EdfaPgRecurringSaleService
import com.edfapaygw.sdk.model.api.EdfaPgAction
import com.edfapaygw.sdk.model.api.EdfaPgOption
import com.edfapaygw.sdk.toolbox.EdfaPgValidation
import com.edfapaygw.sdk.model.request.options.EdfaPgRecurringOptions
import com.edfapaygw.sdk.model.request.order.EdfaPgOrder
import com.edfapaygw.sdk.model.response.sale.EdfaPgSaleCallback
import com.edfapaygw.sdk.model.response.sale.EdfaPgSaleResponse
import com.edfapaygw.sdk.toolbox.EdfaPgAmountFormatter
import com.edfapaygw.sdk.toolbox.EdfaPgHashUtil
import com.google.gson.GsonBuilder

/**
 * The API Adapter for the RECURRING_SALE operation.
 * @see EdfaPgRecurringSaleService
 * @see EdfaPgSaleDeserializer
 * @see EdfaPgSaleCallback
 * @see EdfaPgSaleResponse
 */
object EdfaPgRecurringSaleAdapter : EdfaPgBaseAdapter<EdfaPgRecurringSaleService>() {

    private val edfapayAmountFormatter = EdfaPgAmountFormatter()

    override fun provideServiceClass(): Class<EdfaPgRecurringSaleService> =
        EdfaPgRecurringSaleService::class.java

    override fun configureGson(builder: GsonBuilder) {
        super.configureGson(builder)
        builder.registerTypeAdapter(
            responseType<EdfaPgSaleResponse>(),
            EdfaPgSaleDeserializer()
        )
    }

    /**
     * Executes the [EdfaPgRecurringSaleService.recurringSale] request.
     *
     * @param order the [EdfaPgOrder].
     * @param options the [EdfaPgRecurringOptions].
     * @param payerEmail customer’s email. String up to 256 characters.
     * @param cardNumber the credit card number.
     * @param auth indicates that transaction must be only authenticated, but not captured.
     * @param callback the [EdfaPgSaleCallback].
     */
    fun execute(
        @NonNull
        order: EdfaPgOrder,
        @NonNull
        options: EdfaPgRecurringOptions,
        @NonNull
        @Size(max = EdfaPgValidation.Text.REGULAR)
        payerEmail: String,
        @NonNull
        @Size(min = EdfaPgValidation.Card.CARD_NUMBER_MIN, max = EdfaPgValidation.Card.CARD_NUMBER_MAX)
        cardNumber: String,
        @NonNull
        auth: Boolean,
        @NonNull
        callback: EdfaPgSaleCallback
    ) {
        val hash = EdfaPgHashUtil.hash(
            email = payerEmail,
            cardNumber = cardNumber
        )

        execute(order, options, hash, auth, callback)
    }

    /**
     * Executes the [EdfaPgRecurringSaleService.recurringSale] request.
     *
     * @param order the [EdfaPgOrder].
     * @param options the [EdfaPgRecurringOptions].
     * @param hash special signature to validate your request to payment platform.
     * @param auth indicates that transaction must be only authenticated, but not captured.
     * @param callback the [EdfaPgSaleCallback].
     * @see com.edfapaygw.sdk.toolbox.EdfaPgHashUtil
     */
    fun execute(
        @NonNull
        order: EdfaPgOrder,
        @NonNull
        options: EdfaPgRecurringOptions,
        @NonNull
        hash: String,
        @NonNull
        auth: Boolean,
        @NonNull
        callback: EdfaPgSaleCallback
    ) {
        service.recurringSale(
            url = EdfaPgCredential.paymentUrl(),
            action = EdfaPgAction.RECURRING_SALE.action,
            clientKey = EdfaPgCredential.clientKey(),
            orderId = order.id,
            orderAmount = edfapayAmountFormatter.amountFormat(order.amount),
            orderDescription = order.description,
            recurringFirstTransactionId = options.firstTransactionId,
            recurringToken = options.token,
            auth = EdfaPgOption.map(auth).option,
            hash = hash
        ).edfapayEnqueue(callback)
    }
}
