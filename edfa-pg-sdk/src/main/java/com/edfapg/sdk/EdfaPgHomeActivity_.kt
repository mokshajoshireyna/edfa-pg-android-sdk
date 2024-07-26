package com.edfapg.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.edfapg.sdk.core.EdfaPgSdk
import com.edfapg.sdk.databinding.ActivityEdfaPayHomeBinding
import com.edfapg.sdk.model.request.card.EdfaPgCard
import com.edfapg.sdk.model.request.order.EdfaPgSaleOrder
import com.edfapg.sdk.model.request.payer.EdfaPgPayer
import com.edfapg.sdk.views.edfacardpay.EdfaCardPay
import java.util.*

class EdfaPgHomeActivity_ : AppCompatActivity() {
    private lateinit var binding: ActivityEdfaPayHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdfaPayHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EdfaPgSdk.enableDebug()

        binding.btnSaleWithCardUi.setOnClickListener {
            EdfaPgSdk.init(
                this,
                "f6534bd9-d4e8-431f-93d2-592b1b112e9b",
                "59338f04447a23f15f749a4bd3bb0e9f",
                "https://api.edfapay.com/payment/post"
            )
            payWithCard()
        }
    }

    fun payWithCard() {

        val order = EdfaPgSaleOrder(
            id = UUID.randomUUID().toString(),
            amount = 1.00,
            currency = "SAR",
            description = "Test Order"
        )

        val payer = EdfaPgPayer(
            "Zohaib",
            "Kambrani",
            "Riyadh",
            "SA",
            "Riyadh",
            "123123",
            "a2zzuhaib@gmail.com",
            "966500409598",
            "171.100.100.123"
        )

        val edfaCardPay = EdfaCardPay()
            .setOrder(order)
            .setPayer(payer)
            .onTransactionFailure { res, data ->
                print("$res $data")
                Toast.makeText(this, "Transaction Failure", Toast.LENGTH_LONG).show()
            }.onTransactionSuccess { res, data ->
                print("$res $data")
                Toast.makeText(this, "Transaction Success", Toast.LENGTH_LONG).show()
            }

        /*
        * Precise way to start card payment (ready to use)
        * */
        edfaCardPay.initialize(
            this,
            onError = {

            },
            onPresent = {

            }
        )


        /*
        * To get intent of card screen activity to present in your own choice (ready to use)
        * */
//        startActivity(edfaCardPay.intent(
//            this,
//            onError = {
//
//            },
//            onPresent = {
//
//            })
//        )


        /*
        * To get fragment of card screen to present in your own choice (ready to use)
        * */
//        edfaCardPay.fragment(
//            onError = {
//
//            },
//            onPresent = {
//
//            }
//        )
    }
}