package com.example.kelineyt.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kelineyt.data.User
import com.example.kelineyt.util.Constants.USER_COLLECTIONS
import com.example.kelineyt.util.RegisterFieldState
import com.example.kelineyt.util.RegisterValidation
import com.example.kelineyt.util.Resource
import com.example.kelineyt.util.validateEmail
import com.example.kelineyt.util.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
):ViewModel() {

    private  val _register= MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> =_register
    private val _validation=Channel<RegisterFieldState>()
    val validation= _validation.receiveAsFlow()

    fun createAccountWithEmailAndPassword(user: User, password:String){

        if(checkValidation(user, password)) {


            runBlocking {
                _register.emit(Resource.Loading())
            }

            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener {
                    it.user?.let {
                        saveInfo(it.uid,user)

                    }

                }.addOnFailureListener {
                    _register.value = Resource.Error(it.message.toString())
                }
        }
        else{
            val registerFeildState=RegisterFieldState(validateEmail(user.email),validatePassword(password))
            runBlocking {
                _validation.send(registerFeildState)
            }


        }

    }

    private fun saveInfo(userUid: String,user:User) {
        db.collection(USER_COLLECTIONS).document(userUid).set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }.addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
    }

    private fun checkValidation(user: User, password: String):Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        val shouldRegister = emailValidation is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success
        return shouldRegister
    }
}