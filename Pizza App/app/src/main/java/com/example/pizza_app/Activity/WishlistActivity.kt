package com.example.pizza_app.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pizza_app.R
// Assuming you have a shared model, but we'll define a placeholder here for completeness
// import com.example.pizza_app.Model.ItemModel
import com.example.pizza_app.databinding.ActivityWishlistBinding
import com.example.pizza_app.databinding.ViewholderWishlistItemBinding


data class ItemModel(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val pic: String
)

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding
    private lateinit var wishlistAdapter: WishlistAdapter
    private val hardcodedWishlist = mutableListOf<ItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setOnClickListener {
            finish()
        }

        initData()
        initWishlist()
        initBottomActions()
    }

    private fun initData() {
        hardcodedWishlist.addAll(listOf(
            ItemModel(
                id = 1,
                title = "Pepperoni Supreme",
                description = "Classic pepperoni pizza with extra mozzarella.",
                price = 15.99,
                pic = "https://picsum.photos/id/1025/200/200"
            ),
            ItemModel(
                id = 2,
                title = "Four Cheese Deluxe",
                description = "A blend of cheddar, parmesan, mozzarella, and gouda.",
                price = 12.50,
                pic = "https://picsum.photos/id/1018/200/200"
            ),
            ItemModel(
                id = 3,
                title = "Chicken BBQ",
                description = "Smoked chicken, red onions, and sweet BBQ sauce.",
                price = 14.75,
                pic = "https://picsum.photos/id/10/200/200"
            )
        ))
    }

    private fun initWishlist() {
        wishlistAdapter = WishlistAdapter(hardcodedWishlist, ::removeItem)

        binding.wishlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WishlistActivity)
            adapter = wishlistAdapter
        }

        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (hardcodedWishlist.isEmpty()) {
            binding.tvEmptyWishlist.visibility = android.view.View.VISIBLE
            binding.bottomActionsContainer.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyWishlist.visibility = android.view.View.GONE
            binding.bottomActionsContainer.visibility = android.view.View.VISIBLE
        }
    }

    private fun removeItem(item: ItemModel, position: Int) {
        hardcodedWishlist.remove(item)
        wishlistAdapter.notifyItemRemoved(position)
        updateEmptyState()
        Toast.makeText(this, "${item.title} removed from wishlist", Toast.LENGTH_SHORT).show()
    }

    private fun initBottomActions() {
        binding.btnMoveAllToCart.setOnClickListener {
            Toast.makeText(this, "Simulating moving all items to cart!", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearWishlist.setOnClickListener {
            val count = hardcodedWishlist.size
            hardcodedWishlist.clear()
            wishlistAdapter.notifyDataSetChanged()
            updateEmptyState()
            Toast.makeText(this, "$count items cleared from wishlist!", Toast.LENGTH_SHORT).show()
        }
    }
}

class WishlistAdapter(
    private val items: MutableList<ItemModel>,
    private val removeCallback: (ItemModel, Int) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(val binding: ViewholderWishlistItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = ViewholderWishlistItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WishlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            titleTxt.text = item.title
            priceTxt.text = String.format("$$%.2f", item.price)

            Glide.with(holder.itemView.context)
                .load(item.pic) // Use the 'pic' field for the URL
                .placeholder(R.drawable.ic_launcher_background)
                .into(pic)

            removeBtn.setOnClickListener {
                removeCallback(item, position)
            }

            root.setOnClickListener {
                Toast.makeText(it.context, "Opening details for ${item.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = items.size
}