package com.tetocaApp.tetoca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val usuario: FirebaseUser? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoso: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState(
        usuario = auth.currentUser
    ))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** true si ya hay una sesión activa al abrir la app */
    val haySession: Boolean get() = auth.currentUser != null

    fun registrar(email: String, password: String) {
        val emailLimpio = email.trim()
        val passLimpio = password.trim()

        if (emailLimpio.isBlank() || passLimpio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa todos los campos")
            return
        }
        if (passLimpio.length < 6) {
            _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            try {
                val result = auth.createUserWithEmailAndPassword(emailLimpio, passLimpio).await()
                result.user?.sendEmailVerification()
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    usuario = result.user,
                    exitoso = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = mensajeError(e)
                )
            }
        }
    }

    fun login(email: String, password: String) {
        val emailLimpio = email.trim()
        val passLimpio = password.trim()

        if (emailLimpio.isBlank() || passLimpio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            try {
                val result = auth.signInWithEmailAndPassword(emailLimpio, passLimpio).await()
                guardarTokenFcm(result.user?.uid)
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    usuario = result.user,
                    exitoso = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = mensajeError(e)
                )
            }
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState(usuario = null)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun limpiarExitoso() {
        _uiState.value = _uiState.value.copy(exitoso = false)
    }

    /** Guarda el token FCM del dispositivo en Firestore para notificaciones push dirigidas. */
    private fun guardarTokenFcm(uid: String?) {
        uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .update("fcmToken", token)
        }
    }

    /** Convierte excepciones de Firebase en mensajes legibles en español */
    private fun mensajeError(e: Exception): String = when {
        e.message?.contains("email address is already in use") == true ->
            "Ya existe una cuenta con ese correo"
        e.message?.contains("no user record") == true ||
                e.message?.contains("password is invalid") == true ->
            "Correo o contraseña incorrectos"
        e.message?.contains("badly formatted") == true ->
            "El formato del correo no es válido"
        e.message?.contains("network") == true ->
            "Sin conexión a internet"
        else -> "Ocurrió un error. Intenta de nuevo."
    }
}