package com.example.arrayshow

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_bt.*
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

class BtActivity:AppCompatActivity() {
    // 权限请求列表
    var requestList = mutableListOf<String>()
    // 蓝牙列表
    var Device_list = mutableListOf<String>()
    // recycleView适配器
    var myAdapter = MyRecycleAdapter(Device_list)
//    // UUID
//    var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    // 蓝牙服务
//    var mBluetoothSocket: BluetoothSocket? = null
//    // 蓝牙连接判断
//    var isBlueConnected: Boolean = false
//    // 蓝牙控制器
    var BTController = BlueToothController()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt)


        // 搜索蓝牙广播
        var foundFilter: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, foundFilter)

        recycle_view.layoutManager = LinearLayoutManager(this)
        recycle_view.adapter = myAdapter


        btn1.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }

        btn2.setOnClickListener {
            if (BTController.isSupportBT()) {
                showMsg("支持蓝牙")
            } else {
                showMsg("不支持蓝牙")
            }
        }

        btn3.setOnClickListener {
            if (BTController.getBTStatus()) {
                showMsg("蓝牙已打开")
            } else {
                showMsg("蓝牙已关闭")
            }
        }

        btn4.setOnClickListener {
            if (BTController.getBTStatus()) {
                BTController.closeBT()
                showMsg("蓝牙已关闭")
            } else {
                BTController.openBT()
                showMsg("蓝牙已打开")
            }
        }

        btn5.setOnClickListener {
            if (BTController.getBTStatus()) {
                getPermision()
//                BTController.stopFindDevice()
                Device_list.clear()
                myAdapter.notifyDataSetChanged()
                registerReceiver(receiver, foundFilter)
                BTController.findDevice()
            } else {
                showMsg("蓝牙未打开")
            }
        }

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var s: String = ""
            val action: String = intent.action.toString()
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    var device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (device.name != null) {
                        if (device.bondState == 12) {
                            s =
                                "设备名：" + device.name + "\n" + "设备地址：" + device.address + "\n" + "连接状态：已配对" + "\n";
                        } else if (device.bondState == 10) {
                            s =
                                "设备名：" + device.name + "\n" + "设备地址：" + device.address + "\n" + "连接状态：未配对" + "\n";
                        } else {
                            s =
                                "设备名：" + device.name + "\n" + "设备地址：" + device.address + "\n" + "连接状态：未知" + "\n";
                        }
                        if (!Device_list.contains(s)) {
                            Device_list.add(s)
                            myAdapter.notifyDataSetChanged()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    showMsg("开始搜索")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    showMsg("搜索结束")
                    unregisterReceiver(this)
                }
            }
        }
    }

    fun showMsg(inf: String) {
        Toast.makeText(this, inf, Toast.LENGTH_SHORT).show()
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

    fun BTChoose(btAddress: String){
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("btAddress", btAddress)
        startActivity(intent)
    }

    //相比于ArrayAdapter这里应该也是有几个参数的,只不过把参数给写到了方法里面，只留下一个数据传输即可
    inner class MyRecycleAdapter(var Device_list:List<String> ) : RecyclerView.Adapter<MyRecycleAdapter.ViewHolder>() {

        //在内部类里面获取到item里面的组件
        inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
            var newBTName:TextView=view.findViewById(R.id.btName)
            var newBTAddress:TextView=view.findViewById(R.id.btAddress)
            var newBTStatus:TextView=view.findViewById(R.id.btStatus)
        }

        //重写的第一个方法，用来给制定加载那个类型的Recycler布局
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view=LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
            var viewHolder=ViewHolder(view)
            //单机事件
            viewHolder.itemView.setOnClickListener {
                getPermision()
                var position= viewHolder.adapterPosition
                var device = Device_list[position].split("\n")
                BTChoose(device[1].substring(5))
            }
            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device =Device_list[position]
            val info = device.split("\n")
            holder.newBTName.text    = info[0]
            holder.newBTAddress.text = info[1]
            holder.newBTStatus.text  = info[2]
        }

        override fun getItemCount(): Int {
            return Device_list.size
        }
    }

}