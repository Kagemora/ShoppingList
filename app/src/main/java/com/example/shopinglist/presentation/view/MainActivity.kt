package com.example.shopinglist.presentation.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.shopinglist.ShopApplication
import com.example.shopinglist.domain.entities.ShopItem
import com.example.shopinglist.presentation.adapter.ShopListAdapter
import com.example.shopinglist.presentation.viewmodel.MainViewModel
import com.example.shopinglist.presentation.viewmodel.ViewModelFactory
import com.kagemora.shoppinglist.R
import com.kagemora.shoppinglist.databinding.ActivityMainBinding
import javax.inject.Inject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), ShopItemFragment.OnEditingFinishedListener {
    private lateinit var viewModel: MainViewModel
    private lateinit var shopListAdapter: ShopListAdapter
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var viewmodelFactory: ViewModelFactory

    private val component by lazy {
        (application as ShopApplication).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        viewModel = ViewModelProvider(this, viewmodelFactory)[MainViewModel::class.java]
        viewModel.shopList.observe(this) {
            shopListAdapter.submitList(it)//новый метод submitList для ресайклервью
            //если нужно обновить список вызываем submitList
        }
        binding.buttonAddShopItem.setOnClickListener {
            if (isOnePaneMode()) {
                val intent = ShopItemActivity.newIntentAddItem(this)
                startActivity(intent)
            } else {
                lauchFragment(ShopItemFragment.newInstanceAddItem())
            }
        }
        thread {
            val cursor = contentResolver.query(
                Uri.parse("content://com.example.shopinglist/shop_items"),
                null,
                null,
                null,
                null
            )
            while (cursor?.moveToNext() == true) {//изначально курсор стоит на -1, потом он проваливается
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
                val enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabled")) > 0
                val shopItem = ShopItem(
                    id = id,
                    name = name,
                    count = count,
                    enabled = enabled
                )
                Log.d("MainActivity", shopItem.toString())
            }
            cursor?.close()
        }
    }

    override fun onEditingFinished() {
        Toast.makeText(this@MainActivity, "Succes", Toast.LENGTH_SHORT).show()
        supportFragmentManager.popBackStack()
    }

    private fun isOnePaneMode(): Boolean {
        return binding.shopItemContainer == null
    }

    private fun lauchFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction()
            .add(R.id.shop_item_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupRecyclerView() {
        with(binding.rvShopList) {
            shopListAdapter = ShopListAdapter()
            adapter = shopListAdapter
            recycledViewPool.setMaxRecycledViews(
                ShopListAdapter.VIEW_TYPE_ENABLE,
                ShopListAdapter.MAX_POOL_SIZE
            )
            recycledViewPool.setMaxRecycledViews(
                ShopListAdapter.VIEW_TYPE_DISABLED,
                ShopListAdapter.MAX_POOL_SIZE
            )
            setupLongClickListener()
            setupClickListener()
            setupSwipeLisnere(binding.rvShopList)
        }

    }

    private fun setupClickListener() {
        shopListAdapter.onShopItemClickListener = {
            if (isOnePaneMode()) {
                val intent = ShopItemActivity.newIntentEditItem(this, it.id)
                startActivity(intent)
            } else {
                lauchFragment(ShopItemFragment.newInstanceEditItem(it.id))
            }

        }
    }

    private fun setupSwipeLisnere(rvShopList: RecyclerView?) {
        val callbakc = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            //параметр 0 говорит, что мы не поддерживаем перетаскивание элементво списка
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {//игнорируем метод перетаскивания
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item =
                    shopListAdapter.currentList[viewHolder.adapterPosition] //используем новый метод для получения текущей позиции элемента
                //currentList если нужно получить текущий список с которым работает адаптер 
//                viewModel.deleteShopItem(item)//удаляем из базы
                thread {
                    contentResolver.delete(
                        Uri.parse("content://com.example.shopinglist/shop_items"),
                        null,
                        arrayOf(item.id.toString())
                    )
                }
            }

        }
        val itemTouchHelper =
            ItemTouchHelper(callbakc)//Создание экземпляра ItemTouchHelper, передавая в качестве параметра ранее созданный callback.
        itemTouchHelper.attachToRecyclerView(rvShopList)//Привязка ItemTouchHelper к RecyclerView, таким образом, активируя функциональность свайпа для элементов списка.
        //передали в attachToRecyclerView как должен работать спайп
    }

    private fun setupLongClickListener() {
        shopListAdapter.onShopItemLongClickListener = {
            viewModel.changeEnableState(it)
        }
    }
}
