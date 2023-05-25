package com.fgr.dicodingstory.ui.authentication.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fgr.dicodingstory.R
import com.fgr.dicodingstory.component.CustomProgressDialog
import com.fgr.dicodingstory.databinding.ActivityRegisterBinding
import com.fgr.dicodingstory.ui.authentication.AuthenticationViewModel
import com.fgr.dicodingstory.ui.home.list_story.ListStoryActivity

class RegisterActivity : AppCompatActivity() {
    private val viewModel: AuthenticationViewModel by viewModels()
    private val progressDialog by lazy { CustomProgressDialog(this) }
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.registerLoginButton.setOnClickListener {
            finish()
        }

        registerUsers()

        viewModel.registerMessage.observe(this) { callback ->
            if (callback.isError) {
                // User registration failed
                if (callback.message.contains("400")) {
                    Toast.makeText(this, getString(R.string.email_already_use), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.time_out), Toast.LENGTH_SHORT).show()
                }
            } else {
                // User registration success
                Toast.makeText(this, callback.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isRegisterLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.registerButton.isEnabled = false
                progressDialog.start(resources.getString(R.string.please_wait))
            } else {
                binding.registerButton.isEnabled = true
                progressDialog.stop()
            }
        }
    }

    private fun registerUsers() {
        val name = binding.registerName
        val email = binding.registerEmail
        val password = binding.registerPassword

        binding.registerButton.setOnClickListener {
            val isError = name.error == null && email.error == null && password.error == null
            val isEmpty = !name.text.isNullOrEmpty() && !email.text.isNullOrEmpty() && !password.text.isNullOrEmpty()
            if (isError && isEmpty) {
                viewModel.register(name.text.toString().trim(), email.text.toString().trim(), password.text.toString().trim())
            } else {
                Toast.makeText(this, getString(R.string.fill_all_field), Toast.LENGTH_SHORT).show()
            }

        }
    }
}