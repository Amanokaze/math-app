package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    appLanguage: AppLanguage,
    onAuthSuccess: (AuthState.LoggedIn) -> Unit,
    onBack: () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }   // 0=로그인, 1=회원가입
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showGoogleNotice by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetMsg by remember { mutableStateOf("") }
    var resetLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    fun L(key: String) = L10n.string(key, appLanguage)

    fun validate(): Boolean {
        return when {
            email.isBlank()    -> { errorMsg = L("account_email_required");   false }
            !email.contains("@") -> { errorMsg = L("account_email_invalid"); false }
            password.length < 6  -> { errorMsg = L("account_password_min");  false }
            else -> true
        }
    }

    fun submitEmail() {
        if (!validate()) return
        loading = true
        errorMsg = ""
        scope.launch {
            val result = if (tabIndex == 0) AuthRepository.signIn(email, password)
                         else              AuthRepository.signUp(email, password)
            loading = false
            when (result) {
                is AuthResult.Success -> onAuthSuccess(result.state as AuthState.LoggedIn)
                is AuthResult.Error   -> errorMsg = result.message
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 헤더 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(AppColors.GradientPurple))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = { SoundPlayer.play(SoundEffect.Back); onBack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                text = L("account_login_title"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── 부모 게이트 통과 후 단계임을 안내 ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = L("account_parent_notice"),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── 탭: 로그인 / 회원가입 ──
        TabRow(
            selectedTabIndex = tabIndex,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0; errorMsg = "" }) {
                Text(L("account_tab_login"), modifier = Modifier.padding(vertical = 12.dp))
            }
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1; errorMsg = "" }) {
                Text(L("account_tab_signup"), modifier = Modifier.padding(vertical = 12.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── 이메일 필드 ──
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMsg = "" },
            label = { Text(L("account_email")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // ── 비밀번호 필드 ──
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMsg = "" },
            label = { Text(L("account_password")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = L(if (passwordVisible) "account_password_hide" else "account_password_show"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        // ── 비밀번호 재설정 링크 (로그인 탭에서만 표시) ──
        if (tabIndex == 0) {
            TextButton(
                onClick = { resetEmail = email; resetMsg = ""; showResetDialog = true },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = L("account_reset_password"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(Modifier.height(8.dp))
        }

        // ── 오류 메시지 ──
        if (errorMsg.isNotEmpty()) {
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── 이메일 로그인/가입 버튼 ──
        Button(
            onClick = { submitEmail() },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (loading) CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            else Text(
                text = if (tabIndex == 0) L("account_tab_login") else L("account_tab_signup"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── OR 구분선 ──
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  ${L("account_or")}  ",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // ── Google 로그인 버튼 ──
        OutlinedButton(
            onClick = { showGoogleNotice = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("🔵  ${L("account_google")}", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(32.dp))
    }

    // ── 비밀번호 재설정 다이얼로그 ──
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { if (!resetLoading) showResetDialog = false },
            title = { Text(L("account_reset_password")) },
            text = {
                Column {
                    if (resetMsg.isEmpty()) {
                        Text(L("account_email"), fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(resetMsg, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                if (resetMsg.isEmpty()) {
                    Button(
                        enabled = !resetLoading,
                        onClick = {
                            resetLoading = true
                            scope.launch {
                                val result = AuthRepository.resetPassword(resetEmail.trim())
                                resetMsg = if (result is AuthResult.Success)
                                    L("account_reset_sent")
                                else
                                    L("account_reset_fail")
                                resetLoading = false
                            }
                        }
                    ) {
                        if (resetLoading) CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        else Text(L("confirm"))
                    }
                } else {
                    Button(onClick = { showResetDialog = false; resetMsg = "" }) {
                        Text(L("confirm"))
                    }
                }
            },
            dismissButton = {
                if (resetMsg.isEmpty()) {
                    OutlinedButton(onClick = { showResetDialog = false }) {
                        Text(L("cancel"))
                    }
                }
            }
        )
    }

    // ── Google 브라우저 이동 확인 다이얼로그 ──
    if (showGoogleNotice) {
        AlertDialog(
            onDismissRequest = { showGoogleNotice = false },
            title   = { Text(L("account_google")) },
            text    = { Text(L("account_google_browser_notice")) },
            confirmButton = {
                Button(onClick = {
                    showGoogleNotice = false
                    if (!openExternalUrl(AuthRepository.googleOAuthUrl())) {
                        errorMsg = L("account_google_open_fail")
                    }
                }) { Text(L("confirm")) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showGoogleNotice = false }) {
                    Text(L("cancel"))
                }
            }
        )
    }
}
