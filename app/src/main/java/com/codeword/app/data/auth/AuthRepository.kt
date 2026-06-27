package com.codeword.app.data.auth

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun ensureSignedIn(): FirebaseUser
    suspend fun signOut()
}
