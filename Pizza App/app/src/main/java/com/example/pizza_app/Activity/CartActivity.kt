package com.example.pizza_app.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pizza_app.Adapter.CartAdapter
import com.example.pizza_app.Helper.ChangeNumberItemsListener
import com.example.pizza_app.Helper.ManagmentCart
import com.example.pizza_app.databinding.ActivityCartBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class CartActivity : AppCompatActivity() {
    lateinit var binding: ActivityCartBinding
    lateinit var managementCart: ManagmentCart
    private var tax: Double=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementCart= ManagmentCart(this)

        calulateCart()
        setVariable()
        initCartList()
    }

    private fun initCartList() {
        binding.apply {
            listView.layoutManager=
                LinearLayoutManager(this@CartActivity, LinearLayoutManager.VERTICAL, false)
            // NOTE: Assumes CartAdapter and managementCart.getListCart() return a list of items with 'title'
            listView.adapter= CartAdapter(
                managementCart.getListCart(),
                this@CartActivity,
                object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        calulateCart()
                    }
                }
            )
        }
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.proceedBtn.setOnClickListener {
            val newOrder = generateNewOrderFromCart()

            val intent = OrderActivity.newIntent(this, newOrder)
            startActivity(intent)

            managementCart.clearCart()
            finish()
        }
    }


    private fun generateNewOrderFromCart(): OrderActivity.Order {
        val orderId = Random.nextInt(100000, 999999).toString()
        val itemsSummary = managementCart.getListCart()
            .joinToString(limit = 3, truncated = "...") { it.title }

        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        val finalTotal = ((managementCart.getTotalFee() + tax + 15) * 100) / 100.0

        return OrderActivity.Order(
            id = orderId,
            items = itemsSummary,
            date = date,
            total = "$$finalTotal",
            status = "Processing",
            isSelected = false
        )
    }

    private fun  calulateCart(){
        val percentTax=0.02
        val delivery=15
        tax=((managementCart.getTotalFee()*percentTax)*100)/100.0
        val total=((managementCart.getTotalFee()+tax+delivery)*100)/100.0
        val itemtotal=(managementCart.getTotalFee()*100)/100.0
        binding.apply {
            totalFeeTxt.text="$$itemtotal"
            totalTaxTxt.text="$$tax"
            deliveryTxt.text="$$delivery"
            totalTxt.text="$$total"
        }
    }
}