package com.major.buddytracker.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.major.buddytracker.Screen
import com.major.buddytracker.ui.theme.BuddyTrackerTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onNavigateMain: () -> Unit, onNavigateRegister: () -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogging by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val state = rememberScrollState()
    val ctx = LocalContext.current

    BuddyTrackerTheme {
        Scaffold {
            Row(
                modifier = Modifier
                    .padding(it)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                        .verticalScroll(state)
                ) {
                    Text("Login", fontSize = 32.sp)

                    Spacer(modifier = Modifier.padding(8.dp))

                    OutlinedTextField(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        value = email,
                        onValueChange = { s -> email = s },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        placeholder = {
                            Text(
                                text = "Email"
                            )
                        },
                        prefix = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.padding(8.dp))

                    OutlinedTextField(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        value = password,
                        singleLine = true,
                        onValueChange = { s -> password = s },
                        placeholder = {
                            Text(
                                text = "Password"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        prefix = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Lock Icon"
                            )
                        },
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.padding(8.dp))

                    AnimatedVisibility(
                        visible = !isLogging,
                        enter = fadeIn(),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isLogging = true
                                    FirebaseAuth.getInstance()
                                        .signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                onNavigateMain()
                                            } else {
                                                Toast.makeText(
                                                    ctx,
                                                    "Error: ${task.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            isLogging = false
                                        }
                                }
                            },
                            enabled = email.trim().isNotEmpty() && password.trim().isNotEmpty()
                        ) {
                            Text("Login")
                        }
                    }

                    AnimatedVisibility(
                        visible = isLogging,
                        enter = fadeIn(),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.padding(8.dp))

                    TextButton(
                        onClick = { onNavigateRegister() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Not a user ? Register here")
                    }
                }
            }
        }
    }
}