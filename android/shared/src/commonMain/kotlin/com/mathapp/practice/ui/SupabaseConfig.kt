package com.mathapp.practice.ui

// ── Supabase 프로젝트 설정 ────────────────────────────────────────────────────
// supabase.com 에서 프로젝트를 생성한 뒤 아래 세 상수를 채워주세요.
// 1. SUPABASE_URL  : Project Settings > API > Project URL
// 2. SUPABASE_ANON_KEY : Project Settings > API > anon/public key
// 3. Google OAuth  : Supabase Dashboard > Auth > Providers > Google 에서 활성화 후
//    Google Cloud Console 에서 "Web 애플리케이션" OAuth 클라이언트를 생성하고
//    승인된 리디렉션 URI 에 Supabase 대시보드가 알려주는 Callback URL 을 추가합니다.
internal const val SUPABASE_URL = "https://hngtkpyfgtzaaklbrdel.supabase.co"
internal const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhuZ3RrcHlmZ3R6YWFrbGJyZGVsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzczMTIyMzYsImV4cCI6MjA5Mjg4ODIzNn0.VnWbMswfxT6YyoM95UHJXOmNobdRWJ1N9UEN5lWXFAU"

// 앱으로 돌아오는 딥링크 URI (AndroidManifest, Info.plist 와 일치해야 합니다)
internal const val OAUTH_REDIRECT_URI = "com.mathapp.practice://auth-callback"
