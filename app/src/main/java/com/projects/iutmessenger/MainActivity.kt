package com.projects.iutmessenger

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.projects.iutmessenger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.findNavController()
        setSupportActionBar(binding.toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        binding.bottomnavigation.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp() =
        navController.navigateUp() || super.onSupportNavigateUp()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_add_group, menu)
        return true
    }

    fun hideBottomNavigation() {
        YoYo.with(Techniques.SlideOutDown).duration(1000).playOn(binding.bottomnavigation)
        binding.bottomnavigation.visibility = View.GONE
    }

    fun hideBottomAndToolbar() {
        YoYo.with(Techniques.SlideOutDown).duration(1000).playOn(binding.bottomnavigation)
        YoYo.with(Techniques.SlideOutUp).duration(1000).playOn(binding.toolbar)
        binding.bottomnavigation.visibility = View.GONE
        binding.toolbar.visibility = View.GONE
    }

    fun showBottomNavigation() {
        YoYo.with(Techniques.SlideInUp).duration(1000).playOn(binding.bottomnavigation)
        binding.bottomnavigation.visibility = View.VISIBLE
    }

    fun showBottomAndToolbar() {
        YoYo.with(Techniques.SlideInUp).duration(1000).playOn(binding.bottomnavigation)
        YoYo.with(Techniques.SlideInDown).duration(1000).playOn(binding.toolbar)
        binding.bottomnavigation.visibility = View.VISIBLE
        binding.toolbar.visibility = View.VISIBLE
    }

    fun hideNavigationIcon() {
        binding.toolbar.navigationIcon = null;
    }

    fun showNavigationIcon() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
    }
}