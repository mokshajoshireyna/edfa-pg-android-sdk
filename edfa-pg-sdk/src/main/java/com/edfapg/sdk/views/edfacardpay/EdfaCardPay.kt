package com.edfapg.sdk.views.edfacardpay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.edfapg.sdk.model.request.card.EdfaPgCard
import com.edfapg.sdk.model.request.order.EdfaPgSaleOrder
import com.edfapg.sdk.model.request.payer.EdfaPgPayer
import com.edfapg.sdk.model.response.sale.EdfaPgSaleResponse

internal var instance:EdfaCardPay? = null
class EdfaCardPay {
    constructor(){
        instance = this
    }

    var _order:EdfaPgSaleOrder? = null
    var _payer:EdfaPgPayer? = null
    var _card:EdfaPgCard? = null
    var _onTransactionFailure:((EdfaPgSaleResponse?, Any?) -> Unit)? = null
    var _onTransactionSuccess:((EdfaPgSaleResponse?, Any?) -> Unit)? = null
    var _onError:((Any) -> Unit)? = null
    var _onPresent:((Activity) -> Unit)? = null
    var edfaCardPayTransaction: EdfaCardPayTransaction? = null

    fun setOrder(order:EdfaPgSaleOrder) : EdfaCardPay{
        _order = order
        return this
    }

    fun setPayer(payer:EdfaPgPayer) : EdfaCardPay{
        _payer = payer
        return this
    }

    fun setCard(card:EdfaPgCard) : EdfaCardPay{
        _card = card
        return this
    }

    fun onTransactionFailure(callback:(EdfaPgSaleResponse?, Any?) -> Unit) : EdfaCardPay{
        _onTransactionFailure = callback
        return this
    }

    fun onTransactionSuccess(callback:(EdfaPgSaleResponse?, Any?) -> Unit) : EdfaCardPay{
        _onTransactionSuccess = callback
        return this
    }

    fun initialize(context:Context, onError:(Any) -> Unit, onPresent:(Activity) -> Unit){
        if(_card == null) {
            _onError = onError
            _onPresent = onPresent
            context.startActivity(intent(context, onError, onPresent))
        }else{
            edfaCardPayTransaction = EdfaCardPayTransaction(context)
            edfaCardPayTransaction!!.doSaleTransaction(_payer, _order, _card ) { cardTransactionData ->
                Log.e("cardTransactionData",cardTransactionData.toString());
                val intent =
                    EdfaPgSaleWebRedirectActivity.intent(context, cardTransactionData)
//                sale3dsRedirectLauncher.launch(intent)
            }
        }
    }

    fun intent(context:Context, onError:(Any) -> Unit, onPresent:(Activity) -> Unit) : Intent {
        _onError = onError
        _onPresent = onPresent

        val intent = Intent(context, EdfaCardPayActivity::class.java)
        return  intent
    }

    fun fragment(onError:(Any) -> Unit, onPresent:(Activity) -> Unit) : Fragment {
        _onError = onError
        _onPresent = onPresent

        return  EdfaCardPayFragment()
    }

    companion object{
        internal fun shared() : EdfaCardPay?{
            return instance
        }
    }
}