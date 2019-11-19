package com.infinite.shoppingview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        target.setOnClickListener {
            ShoppingView(this)
                .addToShoppingCar(target,car)
        }

        startActivity(Intent(this,ListActivity::class.java))
    }
}
