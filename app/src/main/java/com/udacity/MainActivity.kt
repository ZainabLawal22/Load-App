package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding
import com.udacity.utils.sendNotification
import android.Manifest


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selectedGitHubRepository: String? = null
    private var selectedGitHubFileName: String? = null
    private lateinit var loadingButton: LoadingButton
    private lateinit var notificationManager: NotificationManager
    private lateinit var binding: ActivityMainBinding
    private val requestNotificationPermissionCode = 1001

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        loadingButton = binding.contentMain.customButton
        loadingButton.setLoadingButtonState(ButtonState.Completed)
        loadingButton.setOnClickListener { download() }
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                handleDownloadCompletion(context)
            }
        }
    }


    @SuppressLint("Range")
    private fun handleDownloadCompletion(context: Context?) {
        val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadID)
        val cursor = manager?.query(query)

        cursor?.use {
            if (it.moveToFirst()) {
                val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                val notificationMessage = if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    "Download successful"
                } else {
                    "Download failed"
                }
                updateUIAfterDownload(notificationMessage)
            }
        }
    }


    private fun download() {
        if (selectedGitHubRepository.isNullOrEmpty()) {
            showToast(getString(R.string.noRepotSelectedText))
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            requestNotificationPermission()
        } else {

            startDownload()
        }
    }


    private fun startDownload() {
        loadingButton.setLoadingButtonState(ButtonState.Loading)
        setupNotificationManager()


        val request = DownloadManager.Request(Uri.parse(selectedGitHubRepository))
            .setTitle(getString(R.string.app_name))
            .setDescription(getString(R.string.app_description))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "repos/repository.zip"
            )

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)
    }


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    requestNotificationPermissionCode
                )
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestNotificationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startDownload()
            } else {

                showToast("Notification permission is required to display notifications")
            }
        }
    }


    private fun setupNotificationManager() {
        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        createNotificationChannel()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.githubRepo_notification_channel_id),
                getString(R.string.githubRepo_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = "Download is done!"
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    private fun updateUIAfterDownload(message: String) {
        loadingButton.setLoadingButtonState(ButtonState.Completed)
        notificationManager.sendNotification(
            selectedGitHubFileName.orEmpty(),
            applicationContext,
            message
        )
    }


    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton && view.isChecked) {
            when (view.id) {
                R.id.glide_button -> setRepository(R.string.glideGithubURL, R.string.glide_text)
                R.id.load_app_button -> setRepository(R.string.loadAppGithubURL, R.string.load_app_text)
                R.id.retrofit_button -> setRepository(R.string.retrofitGithubURL, R.string.retrofit_text)
            }
        }
    }


    private fun setRepository(repoUrlResId: Int, fileNameResId: Int) {
        selectedGitHubRepository = getString(repoUrlResId)
        selectedGitHubFileName = getString(fileNameResId)
    }


    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}


