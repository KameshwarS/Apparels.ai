package com.example.kelineyt.fragments.loginRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.kelineyt.R
import com.example.kelineyt.activities.ShoppingActivity
import com.example.kelineyt.databinding.FragmentLoginBinding
import com.example.kelineyt.databinding.FragmentRegisterBinding
import com.example.kelineyt.util.Resource
import com.example.kelineyt.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {
    private  val viewModel by viewModels<LoginViewModel>()
    private  lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDontHaveAnAccount.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment2)

        }



        binding.apply {
            btnLoginFragment.setOnClickListener{
                val email=emailLogin.text.toString().trim()
                val password=edPasswordLogin.text.toString()
                viewModel.login(email,password)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.login.collect{
                when(it){
                    is Resource.Loading->{
                        binding.btnLoginFragment.startAnimation()
                    }
                    is Resource.Success->{
                        binding.btnLoginFragment.revertAnimation()
                        Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }

                    }
                    is Resource.Error->{
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                        binding.btnLoginFragment.revertAnimation()
                    }
                    else -> Unit

                }
            }
        }
    }
}