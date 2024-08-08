package com.edfapg.sdk.views.edfacardpay

import android.content.Context
import com.edfapg.sdk.core.EdfaPgSdk
import com.edfapg.sdk.model.api.EdfaPgResult
import com.edfapg.sdk.model.api.EdfaPgStatus
import com.edfapg.sdk.model.request.card.EdfaPgCard
import com.edfapg.sdk.model.request.order.EdfaPgSaleOrder
import com.edfapg.sdk.model.request.payer.EdfaPgPayer
import com.edfapg.sdk.model.response.base.error.EdfaPgError
import com.edfapg.sdk.model.response.gettransactiondetails.EdfaPgGetTransactionDetailsSuccess
import com.edfapg.sdk.model.response.sale.EdfaPgSaleCallback
import com.edfapg.sdk.model.response.sale.EdfaPgSaleResponse
import com.edfapg.sdk.model.response.sale.EdfaPgSaleResult
import com.edfapg.sdk.toolbox.EdfaPgUtil

class EdfaCardPayTransaction(var context: Context) {
    var saleResponse: EdfaPgSaleResponse? = null
    fun doSaleTransaction(payer: EdfaPgPayer?, order: EdfaPgSaleOrder?, card: EdfaPgCard?, callback: (cardTransactionData: CardTransactionData) -> Unit) {
        if (order != null && payer != null && card != null) {
            saleResponse = null
            EdfaPgSdk.Adapter.SALE.execute(
                order = order,
                card = card,
                payer = payer,
                termUrl3ds = EdfaPgUtil.ProcessCompleteCallbackUrl,
                options = null,
                auth = false,
                callback = handleSaleResponse(CardTransactionData(order, payer, card, null), callback)
            )
        } else {
            println("Something was empty")
        }
    }

    private fun handleSaleResponse(cardTransactionData: CardTransactionData, callback: (cardTransactionData: CardTransactionData) -> Unit): EdfaPgSaleCallback {
        return object : EdfaPgSaleCallback {
            override fun onResponse(response: EdfaPgSaleResponse) {
                saleResponse = response
                super.onResponse(response)
            }

            override fun onResult(result: EdfaPgSaleResult) {

                val saleResult = result

                if (result is EdfaPgSaleResult.Recurring) {
                    print(">> EdfaPgSaleResult.Recurring")
                    print(">> $saleResult")

                } else if (result is EdfaPgSaleResult.Secure3d) {
                    print(">> EdfaPgSaleResult.Secure3d")
                    print(">> $saleResult")

                } else if (result is EdfaPgSaleResult.Redirect) {
                    print(">> EdfaPgSaleResult.Redirect")
                    print(">> $saleResult")

                    cardTransactionData.response = result.result

                    callback.invoke(cardTransactionData)

                } else if (result is EdfaPgSaleResult.Decline) {
                    print(">> EdfaPgSaleResult.Decline")
                    print(">> $saleResult")

                } else if (result is EdfaPgSaleResult.Success) {

                    print(">> EdfaPgSaleResult.Success")
                    print(">> $saleResult")

                    val successResult = result.result
                    if (result.result.result == EdfaPgResult.SUCCESS) {
                        print(">> >> EdfaPgResult.SUCCESS")
                        print(">> >> $successResult")
                    } else if (result.result.result == EdfaPgResult.ACCEPTED) {
                        print(">> >> EdfaPgResult.ACCEPTED")
                        print(">> >> $successResult")
                    } else if (result.result.result == EdfaPgResult.DECLINED) {
                        print(">> >> EdfaPgResult.DECLINED")
                        print(">> >> $successResult")
                    } else if (result.result.result == EdfaPgResult.ERROR) {
                        print(">> >> EdfaPgResult.ERROR")
                        print(">> >> $successResult")
                    }
                }
            }

            override fun onError(error: EdfaPgError) {
                print(error.message)
                EdfaCardPay.shared()!!._onTransactionFailure?.let { failure ->
                    failure(
                        saleResponse,
                        error
                    )
                }
            }

            override fun onFailure(throwable: Throwable) {
                super.onFailure(throwable)
                print(throwable.message)
                EdfaCardPay.shared()!!._onTransactionFailure?.let { failure ->
                    failure(
                        saleResponse,
                        throwable
                    )
                }

            }
        }
    }

    fun transactionCompleted(
        data: EdfaPgGetTransactionDetailsSuccess?, error: EdfaPgError?
    ) {

        if (error != null)
            EdfaCardPay.shared()!!._onTransactionFailure?.let { failure ->
                failure(
                    saleResponse,
                    error
                )
            }
        else if (data == null)
            EdfaCardPay.shared()!!._onTransactionFailure?.let { failure ->
                failure(
                    saleResponse,
                    error
                )
            }
        else
            with(data) {
                when (status) {
                    EdfaPgStatus.SETTLED -> EdfaCardPay.shared()!!._onTransactionSuccess?.let { success ->
                        success(
                            saleResponse,
                            data
                        )
                    }

                    else -> EdfaCardPay.shared()!!._onTransactionFailure?.let { failure ->
                        failure(
                            saleResponse,
                            data
                        )
                    }

//                EdfaPgStatus.SECURE_3D -> TODO()
//                EdfaPgStatus.REDIRECT -> TODO()
//                EdfaPgStatus.PENDING -> TODO()
//                EdfaPgStatus.REVERSAL -> TODO()
//                EdfaPgStatus.REFUND -> TODO()
//                EdfaPgStatus.CHARGEBACK -> TODO()
//                EdfaPgStatus.DECLINED -> TODO()
                }
            }
    }
}