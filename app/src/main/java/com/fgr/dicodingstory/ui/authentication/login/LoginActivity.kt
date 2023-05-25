package com.fgr.dicodingstory.ui.authentication.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fgr.dicodingstory.R
import com.fgr.dicodingstory.component.CustomProgressDialog
import com.fgr.dicodingstory.databinding.ActivityLoginBinding
import com.fgr.dicodingstory.ui.authentication.AuthenticationViewModel
import com.fgr.dicodingstory.ui.authentication.register.RegisterActivity
import com.fgr.dicodingstory.ui.home.list_story.ListStoryActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthenticationViewModel by viewModels()
    private val progressDialog by lazy { CustomProgressDialog(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginRegisterButton.setOnClickListener {
            navigateRegister()
        }
        loginUsers()

        viewModel.loginMessage.observe(this) { callback ->
            if (callback.error) {
                // User login failed
                if (callback.message.contains("401")) {
                    Toast.makeText(
                        this,
                        getString(R.string.wrong_email_or_password),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (callback.message.contains("400")) {
                    Toast.makeText(this, getString(R.string.empty_email), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.time_out), Toast.LENGTH_SHORT).show()
                }
            } else {
                // User login success
                Toast.makeText(this, callback.message, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ListStoryActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

        viewModel.isLoginLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loginButton.isEnabled = false
                progressDialog.start(getString(R.string.please_wait))
            } else {
                binding.loginButton.isEnabled = true
                progressDialog.stop()
            }
        }
    }

    private fun navigateRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun loginUsers() {
        val email = binding.loginEmail
        val password = binding.loginPassword

        binding.loginButton.setOnClickListener {
            val isError = email.error == null && password.error == null
            val isEmpty = !email.text.isNullOrEmpty() && !password.text.isNullOrEmpty()
            if (isError && isEmpty) {
                viewModel.login(this, email.text.toString().trim(), password.text.toString().trim())
            } else {
                Toast.makeText(this, getString(R.string.fill_all_field), Toast.LENGTH_SHORT).show()
            }
        }
    }
}