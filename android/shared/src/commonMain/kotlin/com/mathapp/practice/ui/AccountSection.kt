package com.mathapp.practice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// 설정 화면 내 "계정" 섹션 컴포저블
// ParentSettingsScreen 에서 호출됩니다.
@Composable
fun AccountSection(
    appLanguage: AppLanguage,
    authState: AuthState,
    onAuthStateChange: (AuthState) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    fun L(key: String) = L10n.string(key, appLanguage)

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm  by remember { mutableStateOf(false) }
    var syncMsg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ── 섹션 헤더 ──
    Text(
        text = L("parent_account"),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.SecondaryPurple,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            when (authState) {
                // ── 게스트 상태 ──────────────────────────────────────────────
                is AuthState.Guest -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👤", fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = L("account_guest_status"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = L("account_guest_data_notice"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(L("account_link_button"), fontWeight = FontWeight.Bold)
                    }
                }

                // ── 로그인 상태 ──────────────────────────────────────────────
                is AuthState.LoggedIn -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = L("account_linked_status"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = authState.email,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (authState.provider == "google")
                                    L("account_provider_google")
                                else
                                    L("account_provider_email"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 동기화 메시지
                    if (syncMsg.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = syncMsg,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // 데이터 동기화 버튼
                    OutlinedButton(
                        onClick = {
                            loading = true
                            syncMsg = ""
                            scope.launch {
                                val ok = SyncRepository.downloadAndApply(authState.userId)
                                syncMsg = if (ok) L("account_sync_success") else L("account_sync_fail")
                                loading = false
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(L("account_sync_button"))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 로그아웃 버튼
                    OutlinedButton(
                        onClick = { showLogoutConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(L("parent_logout"))
                    }

                    Spacer(Modifier.height(8.dp))

                    // 계정 삭제 버튼
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        enabled = !deleteLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (deleteLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = L("account_delete"),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // ── 로그아웃 확인 다이얼로그 ────────────────────────────────────────────
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title   = { Text(L("parent_logout")) },
            text    = { Text(L("account_logout_confirm_msg")) },
            confirmButton = {
                Button(onClick = {
                    showLogoutConfirm = false
                    scope.launch {
                        AuthRepository.signOut()
                        onAuthStateChange(AuthState.Guest)
                    }
                }) { Text(L("parent_logout")) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirm = false }) {
                    Text(L("cancel"))
                }
            }
        )
    }

    // ── 계정 삭제 확인 다이얼로그 ───────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title   = { Text(L("account_delete")) },
            text    = { Text(L("account_delete_confirm_msg")) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        deleteLoading = true
                        syncMsg = ""
                        scope.launch {
                            when (val result = AuthRepository.requestAccountDeletion()) {
                                is AuthResult.Success -> onAuthStateChange(AuthState.Guest)
                                is AuthResult.Error -> syncMsg = result.message
                            }
                            deleteLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(L("account_delete")) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(L("cancel"))
                }
            }
        )
    }
}
