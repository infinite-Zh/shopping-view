package com.infinite.shoppingview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        target.setOnClickListener {
            ShoppingView(this)
                .addToShoppingCar(target,car)
        }
    }
}
