package com.example.shopinglist.di

import android.app.Application
import com.example.shopinglist.data.provider.ShopListProvider
import com.example.shopinglist.presentation.view.MainActivity
import com.example.shopinglist.presentation.view.ShopItemFragment
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)
    fun inject(fragment: ShopItemFragment)
    fun inject(provider: ShopListProvider)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}