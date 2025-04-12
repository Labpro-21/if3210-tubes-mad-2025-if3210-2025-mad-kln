package com.android.purrytify.ui.screens

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.purrytify.R
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.User
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.network.LoginRequest
import com.android.purrytify.network.LoginResponse
import com.android.purrytify.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(context: Context, navController: NavController) {
    val scope = rememberCoroutineScope()
    val userRepository = RepositoryProvider.getUserRepository()

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                AsyncImage(
                    model = "",
                    contentDescription = "Top Banner",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_logo_3_white),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "Millions of Songs.\nOnly on Purrytify.",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.White) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFF121212)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1DB954),
                    unfocusedBorderColor = Color.Gray ,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFF121212)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF1DB954),
                    unfocusedBorderColor = Color.Gray
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.text.isBlank() || password.text.isBlank()) {
                        errorMessage = "Email and password cannot be empty."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    scope.launch {
//                        Log.d("LOGIN_DEBUG", "Sending request with email: ${email.text}")
                        try {
                            val response = RetrofitClient.api.login(
                                LoginRequest(email.text.trim(), password.text)
                            )

//                            Log.d("LOGIN_DEBUG", "Login successful: ${response.body()?.accessToken}")

                            response.body()?.let { TokenManager.saveToken(context, it.accessToken, it.refreshToken) }
                            val bearerToken = "Bearer ${response.body()?.accessToken}"
                            val profile = RetrofitClient.api.getProfile(bearerToken)
                            TokenManager.saveId(context, profile.id)
//                            Log.d("LOGIN_DEBUG", "Profile fetch successful: ${profile.username}")

                            val user = userRepository.getUserById(profile.id)
                            Log.d("LOGIN_DEBUG", bearerToken)



                            if (user == null){
                                val userTemp = User (
                                    id = profile.id,
                                    username = profile.username,
                                    email = profile.email,
                                    profilePhoto = profile.profilePhoto,
                                    location = profile.location,
                                    createdAt = profile.createdAt,
                                    updatedAt = profile.updatedAt
                                )
                                userRepository.insertUser(userTemp)
                            } else {
                                Log.d("LOGIN_DEBUG", "User already exist")
                            }

                            withContext(Dispatchers.Main) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("LOGIN_DEBUG", "Login error: ${e.localizedMessage}", e)
                            withContext(Dispatchers.Main) {
                                errorMessage = "Login failed. Please check your credentials or connection."
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Log In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}