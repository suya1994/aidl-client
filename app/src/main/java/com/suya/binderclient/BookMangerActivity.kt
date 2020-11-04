package com.suya.binderclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.suya.binderservice.Book
import com.suya.binderservice.IBookManager
import com.suya.binderservice.IOnNewBookArrivedlInterface

class BookMangerActivity : AppCompatActivity() {

    private var bookManager: IBookManager? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //这里是主线程
            println("onServiceConnected:${Thread.currentThread()}")
            bookManager = IBookManager.Stub.asInterface(service)
            val bookList = bookManager?.bookList
            println(bookList)
            bookManager?.registerListener(listener)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bookManager = null
            println("onServiceDisconnected:${Thread.currentThread()}")
        }

    }

    private val listener = object : IOnNewBookArrivedlInterface.Stub() {
        override fun onNewBookArrieved(newBook: Book?) {
            //这里不是主线程
            println("onNewBookArrieved:${Thread.currentThread()}")
            runOnUiThread {
                println(newBook)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_manger)

        findViewById<Button>(R.id.button).setOnClickListener(View.OnClickListener {
            val intent = Intent()
            intent.action = "com.suya.binderservice.bookservice"
            intent.`package` = "com.suya.binderservice"
            val b = bindService(
                    intent,
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
            )
            println("b=$b")
        })



    }

    override fun onDestroy() {
        super.onDestroy()
        if (bookManager != null && bookManager!!.asBinder().isBinderAlive) {
            bookManager!!.unregisterListener(listener)
        }
        unbindService(serviceConnection)
    }
}
