package com.example.shopinglist.data.bd

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_items")
class ShopItemDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val count: Int,
    val enabled: Boolean,

    )