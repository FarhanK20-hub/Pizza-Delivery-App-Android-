package com.example.pizza_app.Domain

data class OrderModel(
    val id: String,
    val items: String,
    val date: String,
    val total: String,
    val status: String,
    var isSelected: Boolean = false
)
