package com.example.helloworld.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<Any?>(null)
    val currentUser: StateFlow<Any?> = _currentUser.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            val loggedIn = repository.isUserLoggedIn()
            _isLoggedIn.value = loggedIn
            if (loggedIn) {
                _currentUser.value = repository.getCurrentUser()
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                repository.loginWithEmail(email, password)
                _loginState.value = LoginState.Success
                _isLoggedIn.value = true
                _currentUser.value = repository.getCurrentUser()
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "登录失败")
            }
        }
    }

    fun loginWithGitHub() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                repository.loginWithGitHub()
                // Success state will be handled by AuthStateChange if implemented globally, 
                // but for simplicity we'll assume it's fine here.
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "GitHub 登录失败")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = LoginState.Idle
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
