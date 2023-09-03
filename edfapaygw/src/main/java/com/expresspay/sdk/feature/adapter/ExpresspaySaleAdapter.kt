/*
 * Property of EdfaPg (https://edfapay.com).
 */

package com.edfapaygw.sdk.feature.adapter

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.Size
import com.edfapaygw.sdk.core.EdfaPgCredential
import com.edfapaygw.sdk.feature.deserializer.EdfaPgSaleDeserializer
import com.edfapaygw.sdk.feature.service.EdfaPgSaleService
import com.edfapaygw.sdk.model.api.EdfaPgAction
import com.edfapaygw.sdk.model.api.EdfaPgOption
import com.edfapaygw.sdk.model.request.card.EdfaPgCard
import com.edfapaygw.sdk.model.request.card.EdfaPgCardFormatter
import com.edfapaygw.sdk.model.request.options.EdfaPgSaleOptions
import com.edfapaygw.sdk.model.request.order.EdfaPgSaleOrder
import com.edfapaygw.sdk.model.request.payer.EdfaPgPayer
import com.edfapaygw.sdk.model.request.payer.EdfaPgPayerOptionsFormatter
import com.edfapaygw.sdk.model.response.sale.EdfaPgSaleCallback
import com.edfapaygw.sdk.model.response.sale.EdfaPgSaleResponse
import com.edfapaygw.sdk.toolbox.EdfaPgAmountFormatter
import com.edfapaygw.sdk.toolbox.EdfaPgHashUtil
import com.edfapaygw.sdk.toolbox.EdfaPgValidation
import com.google.gson.GsonBuilder

/**
 * The API Adapter for the SALE operation.
 * @see EdfaPgSaleService
 * @see EdfaPgSaleDeserializer
 * @see EdfaPgSaleCallback
 * @see EdfaPgSaleResponse
 */
object EdfaPgSaleAdapter : EdfaPgBaseAdapter<EdfaPgSaleService>() {

    private val edfapayAmountFormatter = EdfaPgAmountFormatter()
    private val edfapayCardFormatter = EdfaPgCardFormatter()
    private val edfapayPayerOptionsFormatter = EdfaPgPayerOptionsFormatter()

    override fun provideServiceClass(): Class<EdfaPgSaleService> = EdfaPgSaleService::class.java

    override fun configureGson(builder: GsonBuilder) {
        super.configureGson(builder)
        builder.registerTypeAdapter(
            responseType<EdfaPgSaleResponse>(),
            EdfaPgSaleDeserializer()
        )
    }

    /**
     * Executes the [EdfaPgSaleService.sale] request.
     *
     * @param order the [EdfaPgSaleOrder].
     * @param card the [EdfaPgCard].
     * @param payer the [EdfaPgPayer].
     * @param termUrl3ds URL to which Customer should be returned after 3D-Secure. String up to 1024 characters.
     * @param options the [EdfaPgSaleOptions]. Optional.
     * @param auth indicates that transaction must be only authenticated, but not captured.
     * @param callback the [EdfaPgSaleCallback].
     */
    fun execute(
        @NonNull
        order: EdfaPgSaleOrder,
        @NonNull
        card: EdfaPgCard,
        @NonNull
        payer: EdfaPgPayer,
        @NonNull
        @Size(max = EdfaPgValidation.Text.LONG)
        termUrl3ds: String,
        @Nullable
        options: EdfaPgSaleOptions? = null,
        @NonNull
        auth: Boolean,
        @NonNull
        callback: EdfaPgSaleCallback
    ) {
        val hash = EdfaPgHashUtil.hash(
            email = payer.email,
            cardNumber = card.number
        )
        val payerOptions = payer.options

        service.sale(
            url = EdfaPgCredential.paymentUrl(),
            action = EdfaPgAction.SALE.action,
            clientKey = EdfaPgCredential.clientKey(),
            orderId = order.id,
            orderAmount = edfapayAmountFormatter.amountFormat(order.amount),
            orderCurrency = order.currency,
            orderDescription = order.description,
            cardNumber = card.number,
            cardExpireMonth = edfapayCardFormatter.expireMonthFormat(card),
            cardExpireYear = edfapayCardFormatter.expireYearFormat(card),
            cardCvv2 = card.cvv,
            payerFirstName = payer.firstName,
            payerLastName = payer.lastName,
            payerAddress = payer.address,
            payerCountry = payer.country,
            payerCity = payer.city,
            payerZip = payer.zip,
            payerEmail = payer.email,
            payerPhone = payer.phone,
            payerIp = payer.ip,
            termUrl3ds = termUrl3ds,
            hash = hash,
            auth = EdfaPgOption.map(auth).option,
            channelId = if (options?.channelId.isNullOrEmpty()) null else options?.channelId,
            recurringInit = options?.recurringInit?.let { EdfaPgOption.map(it).option },
            payerMiddleName = if (payerOptions?.middleName.isNullOrEmpty()) null else payerOptions?.middleName,
            payerAddress2 = if (payerOptions?.address2.isNullOrEmpty()) null else payerOptions?.address2,
            payerState = if (payerOptions?.state.isNullOrEmpty()) null else payerOptions?.state,
            payerBirthDate = edfapayPayerOptionsFormatter.birthdateFormat(payerOptions)
        ).edfapayEnqueue(callback)
    }
}
