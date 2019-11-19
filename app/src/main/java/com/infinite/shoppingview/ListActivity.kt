package com.infinite.shoppingview

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author bug小能手
 * Created on 2019/11/19.
 */
class ListActivity : AppCompatActivity() {
    private val items = mutableListOf<Pair<Int, String>>()
        .apply {
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
            add(Pair(R.mipmap.ic_good, "title"))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter = ListAdapter()
    }


    inner class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(layoutInflater.inflate(R.layout.item_list, parent, false))
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pair = items[position]
            holder.img.setImageResource(pair.first)
            holder.title.text = pair.second
        }


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val img = itemView.findViewById<ImageView>(R.id.img)
            val title = itemView.findViewById<TextView>(R.id.title)

            init {
                itemView.setOnClickListener {
                    ShoppingView(this@ListActivity)
                        .addToShoppingCar(img, shoppingCar)
                }
            }
        }
    }
}