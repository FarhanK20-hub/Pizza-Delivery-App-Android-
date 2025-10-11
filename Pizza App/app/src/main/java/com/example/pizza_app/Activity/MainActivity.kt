package com.example.pizza_app.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.pizza_app.Adapter.CategoryAdapter
import com.example.pizza_app.Adapter.PopularAdapter
import com.example.pizza_app.R
import com.example.pizza_app.ViewModel.MainViewModel
import com.example.pizza_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initBanner()
        initCategory()
        initPopular()
        initBottomMenu()
    }


    private fun initBottomMenu() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.cartBtn -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.orderBtn -> {
                    startActivity(Intent(this, OrderActivity::class.java))
                    true
                }
                R.id.profileBtn -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.nav_wishlist -> {
                    startActivity(Intent(this, WishlistActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.bottomNav.selectedItemId = R.id.nav_home
    }


    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.loadPopular().observe(this) { popularItems ->
            binding.recyclerViewPopular.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerViewPopular.adapter = PopularAdapter(popularItems)
            binding.progressBarPopular.visibility = View.GONE
        }

    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE

        viewModel.loadCategory().observe(this) { categories ->
            binding.categoryView.layoutManager = LinearLayoutManager(
                this@MainActivity, LinearLayoutManager.HORIZONTAL, false
            )
            binding.categoryView.adapter = CategoryAdapter(categories)
            binding.progressBarCategory.visibility = View.GONE
        }
    }


    private fun initBanner() {
        binding.progressBarBanner.visibility = View.VISIBLE

        viewModel.loadBanner().observe(this, Observer { bannerList ->
            if (bannerList.isNotEmpty()) {
                Glide.with(this)
                    .load(bannerList[0].url)
                    .into(binding.banner)
            }
            binding.progressBarBanner.visibility = View.GONE
        })
    }
}