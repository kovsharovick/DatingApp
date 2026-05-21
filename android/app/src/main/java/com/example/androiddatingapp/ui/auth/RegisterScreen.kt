package com.example.androiddatingapp.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import com.example.androiddatingapp.ui.components.DateOfBirthTextField
import com.example.androiddatingapp.ui.components.PasswordTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.model.Gender

@Composable
fun RegisterScreen(
    onRegister: (
        email: String,
        password: String,
        name: String,
        dateOfBirth: String,
        gender: Gender,
        city: String,
    ) -> Unit,
    onBackToLogin: () -> Unit,
    errorMessage: String?,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<Gender?>(null) }
    var city by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = scaleDp(20f), vertical = scaleDp(24f))
    ) {
        Text(
            text = if (step == 0) "Регистрация" else "О вас",
            fontSize = scaleSp(24f),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(scaleDp(8f)))
        Text(
            text = if (step == 0) "Шаг 1 из 2 — email и пароль" else "Шаг 2 из 2 — личные данные",
            fontSize = scaleSp(14f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(scaleDp(20f)))

        if (step == 0) {
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
        } else {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; localError = null },
                label = { Text("Имя", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(scaleDp(12f)))
            DateOfBirthTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it; localError = null },
                scaleSp = scaleSp,
            )
            Spacer(Modifier.height(scaleDp(12f)))
            Text(
                text = "Пол",
                fontSize = scaleSp(14f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(scaleDp(6f)))
            Gender.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = gender == option,
                            onClick = { gender = option; localError = null },
                            role = Role.RadioButton
                        )
                        .padding(vertical = scaleDp(4f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = gender == option,
                        onClick = null
                    )
                    Text(
                        text = option.labelRu,
                        fontSize = scaleSp(14f),
                        modifier = Modifier.padding(start = scaleDp(8f))
                    )
                }
            }
            Spacer(Modifier.height(scaleDp(12f)))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it; localError = null },
                label = { Text("Город", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        val displayError = localError ?: errorMessage
        if (displayError != null) {
            Spacer(Modifier.height(scaleDp(10f)))
            Text(text = displayError, fontSize = scaleSp(13f), color = Color(0xFFEF4444))
        }

        Spacer(Modifier.height(scaleDp(20f)))
        if (step == 0) {
            Button(
                onClick = {
                    val err = AuthValidation.validateRegisterCredentials(email, password)
                    if (err != null) localError = err else step = 1
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Далее", fontSize = scaleSp(14f))
            }
        } else {
            Button(
                onClick = {
                    val err = AuthValidation.validateRegisterProfile(name, dateOfBirth, gender, city)
                    if (err != null) {
                        localError = err
                    } else {
                        onRegister(
                            email.trim(),
                            password,
                            name.trim(),
                            dateOfBirth.trim(),
                            gender!!,
                            city.trim()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать аккаунт", fontSize = scaleSp(14f))
            }
            Spacer(Modifier.height(scaleDp(10f)))
            OutlinedButton(
                onClick = { step = 0; localError = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Назад", fontSize = scaleSp(14f))
            }
        }
        Spacer(Modifier.height(scaleDp(10f)))
        OutlinedButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Уже есть аккаунт", fontSize = scaleSp(14f))
        }
    }
}
