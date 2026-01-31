package com.example.helloworld.ui.viewmodel

import com.example.helloworld.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var repository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(AuthRepository::class.java)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success sets Success state`() = runTest(testDispatcher) {
        val email = "test@example.com"
        val password = "password"
        
        // Mock success
        `when`(repository.loginWithEmail(email, password)).thenReturn(Unit)
        
        viewModel.login(email, password)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(LoginState.Success, viewModel.loginState.value)
    }

    @Test
    fun `login failure sets Error state`() = runTest(testDispatcher) {
        val email = "test@example.com"
        val password = "password"
        val errorMessage = "Invalid credentials"
        
        // Mock failure
        `when`(repository.loginWithEmail(email, password)).thenThrow(RuntimeException(errorMessage))
        
        viewModel.login(email, password)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assert(viewModel.loginState.value is LoginState.Error)
        assertEquals(errorMessage, (viewModel.loginState.value as LoginState.Error).message)
    }
}
