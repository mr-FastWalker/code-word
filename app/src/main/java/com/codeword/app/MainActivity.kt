package com.codeword.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.codeword.app.data.auth.AuthRepositoryImpl
import com.codeword.app.ui.theme.CodeWordTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authRepository = AuthRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var isReady by mutableStateOf(false)

        lifecycleScope.launch {
            try {
                val user = authRepository.ensureSignedIn()
                Log.d("Auth", "Signed in: uid=${user.uid} anonymous=${user.isAnonymous}")
            } catch (e: Exception) {
                // Нет сети или Firebase недоступен — не блокируем приложение
                Log.e("Auth", "Sign-in failed, continuing offline", e)
            } finally {
                isReady = true
            }
        }

        setContent {
            CodeWordTheme {
                if (isReady) {
                    CodeWordNavGraph()
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
