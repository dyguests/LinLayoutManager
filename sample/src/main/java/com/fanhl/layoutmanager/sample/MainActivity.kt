package com.fanhl.layoutmanager.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fanhl.layoutmanager.CurveLayoutManger
import com.fanhl.layoutmanager.curve.FerrisWheel
import com.fanhl.layoutmanager.curve.Parabola
import com.fanhl.layoutmanager.curve.Slash
import com.fanhl.layoutmanager.sample.curve.ACurve
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_view.view.*

class MainActivity : AppCompatActivity() {
    private val adapter by lazy { MainAdapter() }

    private val curves by lazy {
        listOf(
                Parabola(),
                Parabola(1f),
                Parabola(-.5f),
                Slash(),
                Slash(-1f),
                FerrisWheel(),
                ACurve()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignViews()
        initData()
        refreshData()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        curves.forEachIndexed { index, curve ->
            menu?.add(0, Menu.FIRST + index, Menu.NONE, curve.toString())
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        curves.getOrNull(item.itemId - Menu.FIRST)?.let { bindCurve(it) }
        return super.onOptionsItemSelected(item)
    }

    private fun assignViews() {
    }

    private fun initData() {
        bindCurve(curves[0])
        recycler_view.adapter = adapter
    }

    private fun refreshData() {
        adapter.setNewData(List(20) { "$it" })
    }

    private fun bindCurve(curve: CurveLayoutManger.Curve) {
        supportActionBar?.title = curve.toString()
        recycler_view.layoutManager = CurveLayoutManger(curve)
    }

    class MainAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_view2) {
        override fun convert(helper: BaseViewHolder?, item: String?) {
            helper?.itemView?.apply {
                tv_1.text = item
            }
        }
    }
}
