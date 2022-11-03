package com.example.arrayshow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.fragment_press_action.*

/**
 * A simple [Fragment] subclass.
 * Use the [PressActionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PressActionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_press_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 设置启动JavaScript的支持
        webView1.settings.javaScriptEnabled = true
        // 设置运行JS中的弹窗
        webView1.settings.javaScriptCanOpenWindowsAutomatically = true
        // 从一个网页跳转到另一个网页时，跳转的网页也在webview中显示
        webView1.webViewClient= WebViewClient()
        // 传入网页
        webView1.loadUrl("file:///android_asset/Bar3D.html")
    }
}