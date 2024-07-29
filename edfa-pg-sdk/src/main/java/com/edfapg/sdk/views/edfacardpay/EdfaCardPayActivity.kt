package com.edfapg.sdk.views.edfacardpay

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.edfapg.sdk.databinding.ActivityEdfaCardPayBinding
import com.edfapg.sdk.model.request.card.EdfaPgCard

internal class EdfaCardPayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEdfaCardPayBinding
    var edfaCardPay: EdfaCardPay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdfaCardPayBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        loadFragment()
        edfaCardPay = EdfaCardPay.shared()
    }

    override fun onResume() {
        super.onResume()
        EdfaCardPay.shared()?._onPresent?.let { it(this) }
    }

    private fun loadFragment() {
        val edfaCardPayFragment = EdfaCardPayFragment()

        if(intent != null) {
            if (intent.hasExtra("cardNumber") &&
                intent.hasExtra("expireMonth") &&
                intent.hasExtra("expireYear") &&
                intent.hasExtra("cvv")
            ) {
                val cardNumber: String? = intent.getStringExtra("cardNumber")
                val expireMonth: Int = intent.getIntExtra("expireMonth", 0)
                val expireYear: Int = intent.getIntExtra("expireYear", 0)
                val cvv: String? = intent.getStringExtra("cvv")

                if (cardNumber != null && expireMonth != 0 && expireYear != 0 && cvv != null) {
                    val bundle = Bundle()
                    bundle.putString("cardNumber", cardNumber)
                    bundle.putInt("expireMonth", expireMonth)
                    bundle.putInt("expireYear", expireYear)
                    bundle.putString("cvv", cvv)

                    edfaCardPayFragment.arguments = bundle
                }
            }
        }

        supportFragmentManager
            .beginTransaction()
            .add(binding.container.id, edfaCardPayFragment, EdfaCardPayFragment::class.java.name)
            .commit()
    }
}