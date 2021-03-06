package com.dicoding.fcmapplication.ui.register

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.dicoding.core.abstraction.BaseActivityBinding
import com.dicoding.core.exception.Failure
import com.dicoding.core.vo.RequestResults
import com.dicoding.fcmapplication.R
import com.dicoding.fcmapplication.databinding.ActivityRegisterBinding
import com.dicoding.fcmapplication.ui.login.LoginActivity
import com.dicoding.fcmapplication.ui.login.LoginViewModel
import com.dicoding.fcmapplication.utils.extensions.fancyToast
import com.dicoding.fcmapplication.utils.extensions.gone
import com.dicoding.fcmapplication.utils.extensions.isValidEmail
import com.dicoding.fcmapplication.utils.extensions.visible
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : BaseActivityBinding<ActivityRegisterBinding>(),
    Observer<RegisterViewModel.RegisterUiState> {

    @Inject
    lateinit var mViewModel: RegisterViewModel

    override val bindingInflater: (LayoutInflater) -> ActivityRegisterBinding
        get() = { ActivityRegisterBinding.inflate(layoutInflater) }

    override fun setupView() {
        mViewModel.uiState.observe(this, this)

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.btnRegister.setOnClickListener {
            doRegister()
        }
    }

    override fun onChanged(state: RegisterViewModel.RegisterUiState?) {
        when (state) {
            is RegisterViewModel.RegisterUiState.SuccessApi -> {
                binding.cvLottieLoading.gone()
                fancyToast(getString(R.string.register_succesfull), FancyToast.SUCCESS)
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
            is RegisterViewModel.RegisterUiState.Loading -> {
                with(binding) {
                    cvLottieLoading.visible()
                }
            }

            is RegisterViewModel.RegisterUiState.Failed -> {
                with(binding) {
                    etUsername.setText("")
                    etPassword.setText("")
                    etEmail.setText("")
                    etConfirmPassword.setText("")
                    cvLottieLoading.gone()
                }
                handleFailure(state.failure)
            }
        }
    }

    private fun doRegister() {

        var isEmpty = false
        var isEmailValid = false
        var isPasswordValid = false

        with(binding) {

            if (etEmail.text.isNullOrEmpty()) {
                etEmail.error = "This field is required"
                etEmail.requestFocus()
                isEmpty = true
            }else{
                val emailCheck = etEmail.text.toString().isValidEmail()
                if(!emailCheck){
                    etEmail.error = "Enter the email with the correct format"
                    etEmail.requestFocus()
                }else{
                    isEmailValid = true
                }
            }
            if (etUsername.text.isNullOrEmpty()) {
                etUsername.error = "This field is required"
                etUsername.requestFocus()
                isEmpty = true
            }
            if (etPassword.text.isNullOrEmpty()) {
                binding.etPassword.error = "This field is required"
                binding.etPassword.requestFocus()
                isEmpty = true
            }
            if (etConfirmPassword.text.isNullOrEmpty()) {
                etEmail.error = "This field is required"
                etEmail.requestFocus()
                isEmpty = true
            }else {
                if (etConfirmPassword.text.toString() != etPassword.text.toString()) {
                    etConfirmPassword.setText("")
                    etConfirmPassword.error = "Password does not match"
                    etConfirmPassword.requestFocus()
                }else{
                    isPasswordValid =true
                }
            }

            // check everything is valid
            if(isEmpty){
                return
            }else{
                if (isEmailValid){
                    if(isPasswordValid){
                        mViewModel.doRegister(
                            email = etEmail.text.toString(),
                            userName = etUsername.text.toString(),
                            password = etPassword.text.toString()
                        )
                    }
                }else{
                    return
                }

            }
        }
    }

    private fun handleFailure(failure: Failure) {
        if (failure.requestResults == RequestResults.NO_CONNECTION) {
            fancyToast(getString(R.string.error_unstable_network), FancyToast.ERROR)
        } else {
            if (failure.code == "400") {
                fancyToast("Email and username have been registered", FancyToast.ERROR)
            } else {
                fancyToast(getString(R.string.error_unknown_error), FancyToast.ERROR)
            }
        }
    }
}