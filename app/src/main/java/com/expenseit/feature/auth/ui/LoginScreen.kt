package com.expenseit.feature.auth.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenseit.feature.splitter.ui.friends.majorCountries
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }

    var localPhone by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(majorCountries[0]) }
    var otpCode by remember { mutableStateOf("") }
    
    var isOtpSent by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "expenseIt",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in to track and split expenses with friends.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (!isOtpSent) {
            // Step 1: Input Phone Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var dropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(0.35f)) {
                    OutlinedTextField(
                        value = "${selectedCountry.flag} ${selectedCountry.code}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        majorCountries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text("${country.flag} ${country.country} (${country.code})") },
                                onClick = {
                                    selectedCountry = country
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = localPhone,
                    onValueChange = { localPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.weight(0.65f),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (localPhone.trim().isEmpty()) {
                        errorMessage = "Phone number is required."
                        return@Button
                    }
                    if (activity == null) {
                        errorMessage = "Failed to obtain activity context."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = ""
                    
                    val cleanLocal = localPhone.trim()
                        .removePrefix("+")
                        .removePrefix(selectedCountry.code.removePrefix("+"))
                    val fullPhone = selectedCountry.code + cleanLocal

                    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = task.exception?.localizedMessage ?: "Verification failed."
                                    isLoading = false
                                }
                            }
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            errorMessage = e.localizedMessage ?: "Verification failed."
                            isLoading = false
                        }

                        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                            verificationId = id
                            resendToken = token
                            isOtpSent = true
                            isLoading = false
                        }
                    }

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(fullPhone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(callbacks)
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.background,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send OTP", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Step 2: Input OTP Code
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("6-Digit OTP Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (otpCode.trim().length != 6) {
                        errorMessage = "OTP must be 6 digits."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = ""

                    val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.trim())
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLoginSuccess()
                        } else {
                            errorMessage = task.exception?.localizedMessage ?: "Invalid OTP code."
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.background,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verify & Login", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isOtpSent = false; otpCode = "" }) {
                    Text("Change Number", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                TextButton(
                    onClick = {
                        if (activity == null || resendToken == null) return@TextButton
                        isLoading = true
                        errorMessage = ""

                        val cleanLocal = localPhone.trim()
                            .removePrefix("+")
                            .removePrefix(selectedCountry.code.removePrefix("+"))
                        val fullPhone = selectedCountry.code + cleanLocal

                        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = task.exception?.localizedMessage ?: "Verification failed."
                                        isLoading = false
                                    }
                                }
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                errorMessage = e.localizedMessage ?: "Verification failed."
                                isLoading = false
                            }

                            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                verificationId = id
                                resendToken = token
                                isLoading = false
                            }
                        }

                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(fullPhone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(callbacks)
                            .setForceResendingToken(resendToken!!)
                            .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    },
                    enabled = !isLoading && resendToken != null
                ) {
                    Text("Resend OTP", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
