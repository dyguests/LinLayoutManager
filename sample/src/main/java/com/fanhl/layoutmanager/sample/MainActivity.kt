package com.fanhl.layoutmanager.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fanhl.layoutmanager.DemoLayoutManager
import com.fanhl.layoutmanager.ZoomLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_view.view.*

class MainActivity : AppCompatActivity() {
    private val adapter = MainAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignViews()
        initData()
        refreshData()
    }

    private fun assignViews() {
        LinearLayoutManager::class.java
        GridLayoutManager::class.java

        recycler_view.layoutManager = ZoomLayoutManager()

        fab.setOnClickListener {
            recycler_view.layoutManager = ZoomLayoutManager()
        }
    }

    private fun initData() {
        recycler_view.adapter = adapter
    }

    private fun refreshData() {
        adapter.setNewData(
            List(100) {
                "$it"
            }
        )
    }

    class MainAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_view) {
        override fun convert(helper: BaseViewHolder?, item: String?) {
            helper?.itemView?.apply {
                tv_1.text = item
            }
        }
    }
}
