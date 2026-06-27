package com.codeword.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun ensureSignedIn(): FirebaseUser {
        // Уже авторизован (анонимно или через Google) — ничего не делаем
        auth.currentUser?.let { return it }
        // Первый запуск — тихий анонимный вход
        return auth.signInAnonymously().await().user
            ?: error("Anonymous sign-in returned null user")
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
