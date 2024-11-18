package com.example.uitesting

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uitesting.componentsUI.appWidget.ListAppWidget
import com.example.uitesting.databinding.ActivityMainBinding
import com.google.api.Distribution.BucketOptions.Linear
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater,null,false)
    }
    private lateinit var chatLayout: RecyclerView
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var mAdapter: ChatAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        viewModel.initSocketConnection()
        viewModel.observeDataStream()
        observeIncomingMessage()
    }
    private fun initView(){
        chatLayout = binding.layoutChat
        binding.activityWidget.setOnClickListener{
            startActivity(Intent(this@MainActivity, ListAppWidget::class.java))
        }
        mAdapter = ChatAdapter()
        chatLayout.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.sendMessage.setOnClickListener{
            val message = binding.edittextView.text.toString()

            if(!TextUtils.isEmpty(message)){
                viewModel.sendMessage(message)
            }
        }
    }

    private fun observeIncomingMessage(){
        viewModel.textReceive.observe(this){
            binding.textReceive.text = it
            mAdapter.addNew(it)
        }
    }
}