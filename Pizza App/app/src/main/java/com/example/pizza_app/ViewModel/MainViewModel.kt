package com.example.pizza_app.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.pizza_app.Domain.BannerModel
import com.example.pizza_app.Domain.CategoryModel
import com.example.pizza_app.Domain.ItemsModel
import com.example.pizza_app.Domain.OrderModel
import com.example.pizza_app.Repository.MainRepository

class MainViewModel: ViewModel() {
    private val repository= MainRepository()

    fun loadBanner(): LiveData<MutableList<BannerModel>>{
        return repository.loadBanner()
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }

    fun loadPopular(): LiveData<MutableList<ItemsModel>>{
        return repository.loadPopular()
    }

    fun loadItems(categoryId: String): LiveData<MutableList<ItemsModel>> {
        return repository.loadItemCategory(categoryId)
    }




}