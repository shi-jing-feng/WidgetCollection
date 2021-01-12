package com.shijingfeng.app

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.shijingfeng.widget_collection.R
import com.shijingfeng.app.util.dp2px
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
        initAction()
    }

    private fun initView() {
        Handler().postDelayed({
            particle_diffuse_view.apply {
                radius = dp2px(100F).toFloat()
                color = Color.WHITE
                refresh()
            }
        }, 5000)
    }

    private fun initData() {

    }

    private fun initAction() {

    }

}