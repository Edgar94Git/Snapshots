package com.example.snapshots

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.snapshots.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupButtonNav()
    }

    private fun setupButtonNav(){
        val fragmentManger = supportFragmentManager
        fragmentManger.beginTransaction().add(R.id.hostFragment, HomeFragment()).commit()
    }
}