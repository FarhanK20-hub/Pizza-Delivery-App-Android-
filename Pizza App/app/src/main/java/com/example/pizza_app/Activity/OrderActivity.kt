package com.example.pizza_app.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza_app.R // Required for R.raw.cache
import com.example.pizza_app.databinding.ActivityOrderBinding
import com.example.pizza_app.databinding.ItemOrderHistoryBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.Serializable
import java.lang.reflect.Type

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private val orderList = mutableListOf<Order>()
    private lateinit var orderAdapter: OrderAdapter

    private val ORDER_HISTORY_FILE = "order_history.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadOrderData(this)

        processIncomingOrder()

        setupRecyclerView()
        setupActionButtons()

        updateEmptyState()
        updateButtonState(0)
    }


    private fun processIncomingOrder() {
        if (intent.hasExtra(EXTRA_NEW_ORDER)) {
            val newOrder = intent.getSerializableExtra(EXTRA_NEW_ORDER) as? Order
            if (newOrder != null) {
                orderList.add(0, newOrder)
                saveOrderList()
                Toast.makeText(this, "Order #${newOrder.id} Placed!", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun loadOrderData(context: Context) {
        if (orderList.isNotEmpty()) return

        val file = context.getFileStreamPath(ORDER_HISTORY_FILE)

        if (file.exists() && file.length() > 0) {
            try {
                val jsonString = file.bufferedReader().use { it.readText() }
                val gson = Gson()
                val listType: Type = object : TypeToken<List<Order>>() {}.type
                val savedOrders: List<Order> = gson.fromJson(jsonString, listType)
                orderList.addAll(savedOrders)
                Log.d("OrderActivity", "Loaded ${orderList.size} orders from internal storage.")
                return
            } catch (e: Exception) {
                Log.e("OrderActivity", "Failed to load from $ORDER_HISTORY_FILE. Falling back to R.raw.cache.", e)
            }
        }


        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.cache)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            val gson = Gson()
            val listType: Type = object : TypeToken<List<Order>>() {}.type
            val cachedOrders: List<Order> = gson.fromJson(jsonString, listType)
            orderList.addAll(cachedOrders)
            Log.d("OrderActivity", "Loaded ${orderList.size} orders from R.raw.cache.")

        } catch (e: Exception) {
            Log.e("OrderActivity", "Failed to load R.raw.cache. Loading hardcoded fallback.", e)
            loadHardcodedFallback()
        }
    }


    private fun saveOrderList() {
        try {
            val gson = Gson()
            val jsonString = gson.toJson(orderList)

            // openFileOutput uses internal storage which is writable
            openFileOutput(ORDER_HISTORY_FILE, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
            Log.i("OrderActivity", "Order history successfully saved to $ORDER_HISTORY_FILE.")
        } catch (e: Exception) {
            Log.e("OrderActivity", "Error saving order history file.", e)
        }
    }

    private fun loadHardcodedFallback() {
        orderList.add(Order("54321", "Margherita Pizza, Coke", "15 Sept 2025", "$350", "Delivered", false))
    }

    private fun setupRecyclerView() {
        // Initialize adapter, passing the list and a callback for selection changes
        orderAdapter = OrderAdapter(orderList) { selectedCount ->
            updateButtonState(selectedCount)
        }
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = orderAdapter
        }
    }


    private fun setupActionButtons() {

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.btnSelectAll.setOnClickListener {
            val selectedCount = orderAdapter.getSelectedOrders().size
            if (selectedCount == orderList.size && orderList.isNotEmpty()) {
                orderAdapter.deselectAll()
            } else {
                orderAdapter.selectAll()
            }
        }

        binding.btnDeleteSelected.setOnClickListener {
            val selectedCount = orderAdapter.getSelectedOrders().size
            if (selectedCount == 0) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Delete Orders")
                .setMessage("Are you sure you want to delete the selected $selectedCount order(s)?")
                .setPositiveButton("Delete") { _, _ ->
                    orderAdapter.deleteSelectedOrders()
                    // NEW: Save the updated list after deletion
                    saveOrderList()
                    Toast.makeText(this, "$selectedCount order(s) deleted", Toast.LENGTH_SHORT).show()

                    updateEmptyState()
                    updateButtonState(0)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun updateButtonState(selectedCount: Int) {
        binding.btnDeleteSelected.isEnabled = selectedCount > 0

        binding.btnDeleteSelected.text =
            if (selectedCount > 0) "Delete ($selectedCount)" else "Delete Selected"

        binding.btnSelectAll.text =
            if (orderList.isNotEmpty() && selectedCount == orderList.size) "Deselect All" else "Select All"
    }

    private fun updateEmptyState() {
        val isEmpty = orderList.isEmpty()

        binding.rvOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tvEmptyOrders.visibility = if (isEmpty) View.VISIBLE else View.GONE

        // Hide/Show selection buttons when list is empty
        binding.btnSelectAll.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.btnDeleteSelected.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }


    companion object {
        const val EXTRA_NEW_ORDER = "extra_new_order"

        // Provides a static method for CartActivity to create an Intent containing the new order
        fun newIntent(context: Context, newOrder: Order): Intent {
            return Intent(context, OrderActivity::class.java).apply {
                putExtra(EXTRA_NEW_ORDER, newOrder)
            }
        }
    }


    data class Order(
        val id: String,
        val items: String,
        val date: String,
        val total: String,
        val status: String,
        var isSelected: Boolean = false
    ) : Serializable


    class OrderAdapter(
        private val orders: MutableList<Order>,
        private val onSelectionChanged: (Int) -> Unit
    ) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

        fun getSelectedOrders(): List<Order> = orders.filter { it.isSelected }
        fun selectAll() { orders.forEach { it.isSelected = true }; notifyDataSetChanged(); onSelectionChanged(orders.size) }
        fun deselectAll() { orders.forEach { it.isSelected = false }; notifyDataSetChanged(); onSelectionChanged(0) }
        fun deleteSelectedOrders() {
            // Remove selected items from the list
            orders.removeAll(getSelectedOrders())
            notifyDataSetChanged()
            // Reset selection count
            onSelectionChanged(0)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
            // Assumes ItemOrderHistoryBinding is available
            val binding = ItemOrderHistoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return OrderViewHolder(binding)
        }

        override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
            holder.bind(orders[position])
        }

        override fun getItemCount(): Int = orders.size

        inner class OrderViewHolder(private val binding: ItemOrderHistoryBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(order: Order) {
                binding.tvOrderId.text = "Order ID: #${order.id}"
                binding.tvOrderItems.text = order.items
                binding.tvOrderDate.text = "Ordered on: ${order.date}"
                binding.tvOrderTotal.text = "Total: ${order.total}"
                binding.tvOrderStatus.text = order.status
                binding.checkboxSelectOrder.isChecked = order.isSelected

                binding.checkboxSelectOrder.setOnCheckedChangeListener { _, isChecked ->
                    order.isSelected = isChecked
                    onSelectionChanged(getSelectedOrders().size)
                }

                binding.root.setOnClickListener {
                    // Toggles the checkbox state when the row is clicked
                    binding.checkboxSelectOrder.isChecked = !binding.checkboxSelectOrder.isChecked
                }
            }
        }
    }
}