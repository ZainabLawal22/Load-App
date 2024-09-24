package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var fileName = ""
    private var status = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.contentDetail.okButton.setOnClickListener {
            returnToMainActivity()
        }

        fileName = intent.getStringExtra("fileName").toString()
        status = intent.getStringExtra("status").toString()

        binding.contentDetail.fileName.text = fileName
        binding.contentDetail.statusText.text = status
    }

    private fun returnToMainActivity() {
        val  mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)
    }
}


