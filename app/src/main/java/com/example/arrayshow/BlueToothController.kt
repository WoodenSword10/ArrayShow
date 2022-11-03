package com.example.arrayshow

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import java.util.*

class BlueToothController {
    var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var mBluetoothSocket: BluetoothSocket? = null
    var mBluetoothAdapter: BluetoothAdapter= BluetoothAdapter.getDefaultAdapter()

    /**
    * 判断是否支持蓝牙
     * */
    fun isSupportBT():Boolean{

        return mBluetoothAdapter != null
    }

    /**
    * 获取当前蓝牙状态
    * true:打开
    * false:关闭
     * */
    fun getBTStatus():Boolean{

        return mBluetoothAdapter.isEnabled
    }

    /**
    * 打开蓝牙
    * */
    fun openBT(){
        if (!getBTStatus()){
            mBluetoothAdapter.enable()
        }

    }

    /**
    * 关闭蓝牙
    * */
    fun closeBT(){
        if(getBTStatus()){
            mBluetoothAdapter.disable()
        }
    }


    /**
     * 查找设备
     */
    fun findDevice(){
        if (getBTStatus()){
            mBluetoothAdapter.startDiscovery()
        }
    }

    /**
     * 关闭搜索
     */
    fun stopFindDevice(){
        mBluetoothAdapter.cancelDiscovery()
    }

    fun getDevice(btAddress:String):BluetoothDevice{
        return mBluetoothAdapter.getRemoteDevice(btAddress)
    }

}