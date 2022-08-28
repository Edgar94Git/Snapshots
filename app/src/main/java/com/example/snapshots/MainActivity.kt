package com.example.snapshots

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupButtonNav()
    }

    private fun setupButtonNav(){
        mFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val profileFragment = ProfileFragment()
        val addFragment = AddFragment()

        mActiveFragment = homeFragment

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, addFragment, AddFragment::class.java.name)
            .hide(addFragment)
            .commit()

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment)
            .commit()

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, homeFragment, HomeFragment::class.java.name)
            .commit()
    }
}