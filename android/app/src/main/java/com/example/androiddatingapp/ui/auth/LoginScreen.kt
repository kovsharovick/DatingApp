package com.example.androiddatingapp.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.example.androiddatingapp.ui.theme.AppButtonDefaults
import com.example.androiddatingapp.ui.theme.AppRedLight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.example.androiddatingapp.ui.components.PasswordTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onGoToRegister: () -> Unit,
    errorMessage: String?,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = scaleDp(20f), vertical = scaleDp(24f))
    ) {
        Text(
            text = "Вход",
            fontSize = scaleSp(24f),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(scaleDp(8f)))
        Text(
            text = "Войдите в аккаунт или создайте новый",
            fontSize = scaleSp(14f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(scaleDp(20f)))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null },
            label = { Text("Email", fontSize = scaleSp(12f)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(scaleDp(12f)))
        PasswordTextField(
            value = password,
            onValueChange = { password = it; localError = null },
            label = "Пароль",
            scaleSp = scaleSp,
        )

        val displayError = localError ?: errorMessage
        if (displayError != null) {
            Spacer(Modifier.height(scaleDp(10f)))
            Text(
                text = displayError,
                fontSize = scaleSp(13f),
                color = AppRedLight
            )
        }

        Spacer(Modifier.height(scaleDp(20f)))
        Button(
            onClick = {
                val err = AuthValidation.validateLogin(email, password)
                if (err != null) {
                    localError = err
                } else {
                    onLogin(email.trim(), password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = AppButtonDefaults.blue(),
        ) {
            Text("Войти", fontSize = scaleSp(14f))
        }
        Spacer(Modifier.height(scaleDp(10f)))
        OutlinedButton(
            onClick = onGoToRegister,
            modifier = Modifier.fillMaxWidth(),
            colors = AppButtonDefaults.outlinedMuted(),
        ) {
            Text("Создать аккаунт", fontSize = scaleSp(14f))
        }
    }
}
