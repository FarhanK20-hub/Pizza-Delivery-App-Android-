package com.example.pizza_app.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.pizza_app.Domain.ItemsModel
import com.example.pizza_app.Helper.ChangeNumberItemsListener
import com.example.pizza_app.Helper.ManagmentCart
import com.example.pizza_app.databinding.ViewholderCartBinding

class CartAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    context: Context,
    private val changeNumberItemsListener: ChangeNumberItemsListener? = null
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val managementCart = ManagmentCart(context)

    inner class ViewHolder(val binding: ViewholderCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ViewholderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItemSelected[position]

        holder.binding.apply {
            titleTxt.text = item.title
            feeEachIteam.text = "₹${item.price}"
            totalEachItem.text = "₹${item.numberInCart * item.price}"
            numberInCartTxt.text = item.numberInCart.toString()

            Glide.with(holder.itemView)
                .load(item.picUrl[0])
                .apply(RequestOptions().transform(CenterCrop()))
                .into(picCart)

            plusBtn.setOnClickListener {
                managementCart.plusItem(listItemSelected, position, object :
                    ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyItemChanged(position) // Update only this item
                        changeNumberItemsListener?.onChanged()
                    }
                })
            }

            minusBtn.setOnClickListener {
                managementCart.minusItem(listItemSelected, position, object :
                    ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyItemChanged(position)
                        changeNumberItemsListener?.onChanged()
                    }
                })
            }

            removeItemBtn.setOnClickListener {
                managementCart.removeItem(listItemSelected, position, object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        if (position in listItemSelected.indices) {
                            listItemSelected.removeAt(position)
                            notifyItemRemoved(position)
                            changeNumberItemsListener?.onChanged()
                        }
                    }
                })
            }

        }
    }

    override fun getItemCount(): Int = listItemSelected.size
}
