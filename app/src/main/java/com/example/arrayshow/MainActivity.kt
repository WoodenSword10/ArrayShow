package com.example.arrayshow

import android.Manifest
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_password.*
import kotlinx.android.synthetic.main.fragment_press_action.*
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    //设置发送和接收的字符编码格式
    private val ENCODING_FORMAT = "UTF-8"
    private val BUNDLE_RECEIVE_DATA = "ReceiveData"
    val MESSAGE_RECEIVE_TAG = 111
    // 权限请求列表
    var requestList = mutableListOf<String>()
    // UUID
    var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    // 蓝牙服务
    var mBluetoothSocket: BluetoothSocket? = null
    // 蓝牙连接判断
    var isBlueConnected: Boolean = false
    // 蓝牙控制器
    var BTController = BlueToothController()
    var mHandler = MyHandler(this)
    // 压力映射开关打开
    var isshow = true
    // 密码锁是否打开
    var ispassward = false
    // 开始记录数据标志
    var isbegin = true
    // 密码锁应用点击标记
    var isclick = false
    // 初始记录数据条数计数器
    var count = 0
    var counter = 0
    // 接收的稳态数据记录
    var init_data = mutableListOf<Int>(0,0,0,0,0,0,0,0,0,0,0,0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        // 设置启动JavaScript的支持
//        webView.settings.javaScriptEnabled = true
//        // 设置运行JS中的弹窗
//        webView.settings.javaScriptCanOpenWindowsAutomatically = true
//        // 从一个网页跳转到另一个网页时，跳转的网页也在webview中显示
////        webView.webViewClient= WebViewClient()
//        // 传入网页
//        webView.loadUrl("file:///android_asset/Bar3D.html")

//        // 绑定监听事件
//        ev1.addTextChangedListener(listener(ev1))
//        ev2.addTextChangedListener(listener(ev2))
//        ev3.addTextChangedListener(listener(ev3))
//        ev4.addTextChangedListener(listener(ev4))
//        ev5.addTextChangedListener(listener(ev5))
//        ev6.addTextChangedListener(listener(ev6))
//        ev7.addTextChangedListener(listener(ev7))
//        ev8.addTextChangedListener(listener(ev8))
//        ev9.addTextChangedListener(listener(ev9))
//        ev10.addTextChangedListener(listener(ev10))
//        ev11.addTextChangedListener(listener(ev11))
//        ev12.addTextChangedListener(listener(ev12))
        repalceFragment(PressActionFragment())
        tv1.setText("未连接")
        tv1.setTextColor(Color.RED)
        btn2.setBackgroundColor(Color.rgb(60,196,169))
        btn3.setBackgroundColor(Color.rgb(221,72,34))
         // 绑定按钮点击事件
        btn1.setOnClickListener {
            startActivity(Intent(this, BtActivity::class.java))
            this.finish()
        }

        btn2.setOnClickListener{
            isshow = !isshow
            ispassward = !ispassward
            if(isshow){
                isbegin = true
                count = 0
                btn2.setBackgroundColor(Color.rgb(60,196,169))
                btn3.setBackgroundColor(Color.rgb(221,72,34))
                repalceFragment(PressActionFragment())
            }else{
                btn3.setBackgroundColor(Color.rgb(60,196,169))
                btn2.setBackgroundColor(Color.rgb(221,72,34))
                repalceFragment(PasswordFragment())
            }
        }

        btn3.setOnClickListener{
            isshow = !isshow
            ispassward = !ispassward
            if(ispassward){
                btn3.setBackgroundColor(Color.rgb(60,196,169))
                btn2.setBackgroundColor(Color.rgb(221,72,34))
                repalceFragment(PasswordFragment())
            }else{
                btn2.setBackgroundColor(Color.rgb(60,196,169))
                btn3.setBackgroundColor(Color.rgb(221,72,34))
                repalceFragment(PressActionFragment())
            }
        }

//        btn4.setOnClickListener{
//            et1.setText("")
//        }
//
//        btn5.setOnClickListener{
//            var password = et1.text.toString()
//            if(password == "123456"){
//                showMsg("密码一致，成功解锁")
//            }else{
//                showMsg("密码不一致！！！")
//            }
//        }



        var btdata = intent.getStringExtra("btAddress")
        if(btdata != null){
//            Log.e("main", btdata)
            tv1.setText("已连接")
            tv1.setTextColor(Color.GREEN)
            BTConnect(btdata)
            BTClientStartReceive()
        }
    }


    fun repalceFragment(fragment: Fragment){
        var fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        var oldfragment = fragmentManager.findFragmentById(R.id.fragments)
        if(oldfragment ==null || oldfragment != fragment){
            transaction.add(R.id.fragments,fragment)
        }
        var ls = fragmentManager.fragments
        if(ls != null){
            ls.forEach{
                transaction.hide(it)
            }
        }
        transaction.show(fragment)
        transaction.commit()
    }

    /**
     * 显示提示消息
     */
    fun showMsg(inf: String) {
//        var toast = Toast.makeText(this, inf, Toast.LENGTH_SHORT)
//        toast.setGravity(Gravity.CENTER,0,0)
//        toast.show()
        var builder= AlertDialog.Builder(this)
        builder.setTitle("提示")
        builder.setMessage(inf)
        builder.setPositiveButton("确认"){dialog, which ->
        }
        var dialog:AlertDialog=builder.create()
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    /**
     * 动态请求权限
     */
    fun getPermision() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestList.add(Manifest.permission.BLUETOOTH_SCAN);
            requestList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            requestList.add(Manifest.permission.BLUETOOTH_CONNECT);
            requestList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            requestList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            requestList.add(Manifest.permission.BLUETOOTH);
        }
        if (requestList.size != 0) {
            ActivityCompat.requestPermissions(this, requestList.toTypedArray(), 1);
        }
    }

//    /**
//     * editText文本改变事件监听
//     */
//    fun listener(view: View): TextWatcher? {
//        return object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(editable: Editable) {
//                var a = listOf(ev1.text, ev2.text, ev3.text, ev4.text, ev5.text, ev6.text, ev7.text, ev8.text, ev9.text, ev10.text, ev11.text, ev12.text)
//                webView.loadUrl("javascript:setValue(${a})")
//            }
//        }
//    }


    /**
     * 蓝牙连接
     */
    fun BTConnect(btAddress: String) {
        thread {
            try {
                getPermision()
                if (mBluetoothSocket == null) {
                    BTController.stopFindDevice()
                    val device = BTController.getDevice(btAddress)
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)
                    mBluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                finish()
                e.printStackTrace()
            }
        }
    }

    /**
     * 蓝牙接收消息
     */
    fun BTClientStartReceive(){
        thread {
            while (true){
                try{
                    if(mBluetoothSocket!=null){
                        if(mBluetoothSocket!!.isConnected){
                            Looper.prepare()
                            Log.e("eee", "开始接收数据")
                            receiveMessage()
                        }
                    }
                }catch (e:Exception){
                    finish()
                    e.printStackTrace()
                }
            }
        }
    }

    //蓝牙接收消息的函数体
    fun receiveMessage(){
        val mmInStream: InputStream = mBluetoothSocket!!.inputStream
        var mmBuffer:ByteArray = ByteArray(1024)
        var bytes= 0
        while(true){
            try{
                bytes = mmInStream.read(mmBuffer)
//                Log.e("read", "ok")
            }catch (e:Exception){
//                Log.e("read", "error")
                break
            }
            val bundle = Bundle()
            val message = Message()
            //默认GBK编码
            val string = String(mmBuffer, 0, bytes, Charset.forName(ENCODING_FORMAT))
            bundle.putString(BUNDLE_RECEIVE_DATA, string)
            message.what = MESSAGE_RECEIVE_TAG
            message.data = bundle
            mHandler.sendMessage(message)
//            Log.e("receive", string)
        }
    }

    inner class MyHandler(activity: MainActivity):Handler(Looper.getMainLooper()){
        var sign = false
        override fun handleMessage(msg: android.os.Message) {
            super.handleMessage(msg)
            when(msg.what){
                111 -> {
                    sign = true
                    var str = msg.data.get("ReceiveData").toString()
                    var foundResults = Regex("""\d+""").findAll(str)
                    var data = mutableListOf<Int>(0)
                    data.clear()
                    for (findText in foundResults) {
                        data.add(findText.value.toInt())
                    }

//                    ev1.setText(data[0].toString())
//                    ev2.setText(data[1].toString())
//                    ev3.setText(data[2].toString())
//                    ev4.setText(data[3].toString())
//                    ev5.setText(data[4].toString())
//                    ev6.setText(data[5].toString())
//                    ev7.setText(data[6].toString())
//                    ev8.setText(data[7].toString())
//                    ev9.setText(data[8].toString())
//                    ev10.setText(data[9].toString())
//                    ev11.setText(data[10].toString())
//                    ev12.setText(data[11].toString())
                    if(ispassward){
                        if(isbegin){
                            count += 1
                            if(count < 11){
                                counter = 0
                                for (item in data){
                                    init_data[counter] =  init_data[counter] + item
                                    counter += 1
                                }
                            }else if(count == 11){
                                for (i in 0..11){
                                    init_data[i] =  init_data[i]/10
                                }
                                isbegin = false
                                Log.e("cs", init_data.toString())
                            }
                        }
                        else if(isclick){
                            isclick = false
                            for (i in 0..11){
                                if(data[i] - init_data[i] > 100){
                                    isclick = true
                                }
                            }
                        }else{
                            for(i in 0..11){
                                if(data[i] - init_data[i] > 100){
                                    isclick = true
                                    if(i == 0){
                                        ev1.setText(ev1.text.toString().dropLast(1))
                                    }
                                    if(i == 1){
                                        ev1.setText(ev1.text.toString()+"7")
                                    }
                                    if(i == 2){
                                        ev1.setText(ev1.text.toString()+"4")
                                    }
                                    if(i == 3){
                                        ev1.setText(ev1.text.toString()+"1")
                                    }
                                    if(i == 4){
                                        ev1.setText(ev1.text.toString()+"0")
                                    }
                                    if(i == 5){
                                        ev1.setText(ev1.text.toString()+"8")
                                    }
                                    if(i == 6){
                                        ev1.setText(ev1.text.toString()+"5")
                                    }
                                    if(i == 7){
                                        ev1.setText(ev1.text.toString()+"2")
                                    }
                                    if(i == 8){
                                        if(ev1.text.toString() == "873402"){
                                            showMsg("解锁成功！！")
                                            ev1.setText("")
                                            image.setImageResource(R.drawable.unlock)
                                        }else{
                                            showMsg("解锁失败！！")
                                            ev1.setText("")
                                            image.setImageResource(R.drawable.lock)
                                        }
                                    }
                                    if(i == 9){
                                        ev1.setText(ev1.text.toString()+"9")
                                    }
                                    if(i == 10){
                                        ev1.setText(ev1.text.toString()+"6")
                                    }
                                    if(i == 11){
                                        ev1.setText(ev1.text.toString()+"3")
                                    }
                                }
                            }
                        }

                    }
                    if(isshow){
                        webView1.loadUrl("javascript:setValue(${data})")
                        Log.e("Handler", data.toString())
                    }
                }
            }
        }
    }
}

