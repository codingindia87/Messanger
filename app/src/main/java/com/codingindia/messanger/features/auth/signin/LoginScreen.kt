package com.codingindia.messanger.features.auth.signin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codingindia.messanger.R

@Composable
fun LoginScreen(
    navController: NavController, viewModel: LoginViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel.loginResult) {
        viewModel.loginResult.collect { result ->
            if (result.isSuccess) {
                navController.navigate("home") {
                    popUpTo(0)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "An unknown error occurred"
                Log.d("Login-error", error)
                Toast.makeText(
                    context, error, Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }

    val lightBlue = Color(0xFF03A9F4)

    Column(modifier = Modifier.padding()) {

        Box(contentAlignment = Alignment.TopCenter) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                color = lightBlue,
                shape = CurvedHeaderShape()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.messenger),
                            contentDescription = null,
                            Modifier.size(90.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Messanger", style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White, fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        "LOGIN", style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White, fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Enter your email and password",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                value = uiState.email,
                label = { Text("Enter Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                value = uiState.password,
                label = { Text("Enter Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                })
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.onEvent(LoginEvent.Login)
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(45.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }
        }
    }
}

class CurvedHeaderShape : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val path = Path().apply {
            val curveHeight = 60f
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - curveHeight)
            quadraticBezierTo(
                x1 = size.width / 2,
                y1 = size.height + curveHeight / 2,
                x2 = 0f,
                y2 = size.height - curveHeight
            )
            close()
        }
        return Outline.Generic(path)
    }
}




