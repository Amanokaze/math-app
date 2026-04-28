# 스토어 배포 로드맵

이 문서는 Claude Code가 가장 먼저 읽는 실행 인덱스다. 각 항목은 별도 문서로 분리되어 있으며, 이 순서대로 진행하면 프로토타입을 App Store와 Google Play 제출 가능한 상태로 만들 수 있다.

- 기준일: 2026-04-25
- 현재 앱 이름: `Math`
- 출시 브랜드: `셈토끼`
- 현재 Android applicationId: `com.mathapp.practice`
- 현재 iOS bundle identifier: `com.mathapp.practice`
- 현재 Android targetSdk: `34`
- 현재 iOS deployment target: `17.0`
- 현재 데이터 구조: 로컬 저장만 사용, Android `SharedPreferences`, iOS `NSUserDefaults`
- 현재 리스크: 표시 이름/아이콘/패키지명/targetSdk/스토어 소재가 출시 수준이 아님 (개인정보 매니페스트·부모 게이트는 P1에서 완료)

## 문서 지도

| 문서 | 목적 | 주요 산출물 |
|---|---|---|
| [brand-strategy.md](./brand-strategy.md) | 브랜드와 톤 확정 | 이름, 아이콘 방향, 앱 내부 용어 |
| [privacy-and-kids-compliance.md](./privacy-and-kids-compliance.md) | 어린이/개인정보 정책 대응 | 개인정보 처리방침, 데이터 안전성 답변, 부모 게이트 요구사항 |
| [release-build-signing.md](./release-build-signing.md) | 배포 빌드와 서명 | Android AAB, iOS archive, versioning, privacy manifest |
| [ios-app-store-release.md](./ios-app-store-release.md) | App Store 제출 | App Store Connect 필드, Kids 카테고리, Review Notes |
| [google-play-release.md](./google-play-release.md) | Google Play 제출 | Play Console 필드, targetSdk, Families, Data safety |
| [store-listing-metadata.md](./store-listing-metadata.md) | 스토어 문구 | 앱 이름, 설명, 키워드, 심사 노트 |
| [store-creative-assets.md](./store-creative-assets.md) | 아이콘/스크린샷/영상 | 제작 스펙, 카피, 파일명 규칙 |
| [qa-beta-test-plan.md](./qa-beta-test-plan.md) | 출시 전 검증 | 기능 QA, 기기 매트릭스, 베타 피드백 |
| [launch-operations.md](./launch-operations.md) | 출시 운영 | 롤아웃, 리뷰 대응, 업데이트 운영 |

## 출시 전략 결론

1차 출시는 `교육 앱`, `무료`, `광고 없음`, `인앱 결제 없음`, `선택적 계정 연동`, `Supabase 백엔드`, `개인정보 최소 수집(이메일, 부모 게이트 후 입력)`으로 간다.

계정 연동은 1차 출시 범위에 포함된다 (브랜치 `15-account-linking`, 2026-04-27 구현 완료). 게스트 모드는 유지되며, 계정 연동은 부모 선택 사항이다.

**스토어 선언 변경 필요 항목 (계정 연동 추가로 인해):**
- Google Play Data safety: 이메일 주소(수집됨, 앱 기능, 사용자 삭제 가능), 앱 활동(학습 기록, 계정 연동 시 전송) 추가
- App Store App Privacy: Contact Info > Email Address 추가 (계정 연동 사용자)
- App Store Privacy Manifest: 네트워크 도메인 추가 (Supabase 프로젝트 URL)

Google Play에서는 어린이 대상 앱으로 Families 정책을 충족한다. App Store에서는 `Made for Kids` 선택이 승인 후 되돌리기 어려우므로, 출시자가 장기적으로 어린이 전용 정책을 지킬 수 있을 때만 선택한다. 계정 연동 기능이 있더라도 외부 인증 화면은 부모 게이트 뒤에 배치되어 있으며, 이동 전 안내 다이얼로그가 표시된다. 선택 전에 [privacy-and-kids-compliance.md](./privacy-and-kids-compliance.md)의 항목을 모두 점검해야 한다.

**Supabase 프로젝트 설정 필요 (출시 전 완료):**
1. supabase.com 에서 프로젝트 생성 → URL과 anon key를 `SupabaseConfig.kt`에 입력
2. Auth > Providers > Email 활성화
3. Auth > Providers > Google 활성화 → Google Cloud Console에서 Web OAuth 클라이언트 생성, Supabase Callback URL 등록
4. SQL 편집기에서 `user_data` 테이블 생성:
```sql
create table user_data (
  user_id uuid primary key references auth.users(id) on delete cascade,
  data jsonb not null default '{}',
  deletion_requested boolean default false,
  updated_at timestamptz default now()
);
alter table user_data enable row level security;
create policy "user_data_self" on user_data for all using (auth.uid() = user_id);
```

## 실제 실행 순서

Claude Code는 아래 순서를 따른다. 앞 단계의 결정이 끝나기 전에는 다음 단계의 코드 변경을 시작하지 않는다.

| 순서 | 단계 | 담당 | 작업 문서 | 완료 조건 |
|---:|---|---|---|---|
| 0 | 출시 이름 최종 확정 | 운영자 | [brand-strategy.md](./brand-strategy.md) | `셈토끼` 확정, 셈토끼는 안내자/대표 마스코트로 정의 |
| 1 | 앱 식별자 확정 | 운영자 | [release-build-signing.md](./release-build-signing.md) | Android `applicationId`, iOS Bundle ID 확정 |
| 2 | 정책 안전장치 구현 | Claude Code | [privacy-and-kids-compliance.md](./privacy-and-kids-compliance.md) | iOS Privacy Manifest, 부모 게이트, 금지 SDK 없음 **✓ 완료 2026-04-27** |
| 3 | 빌드/서명 기반 정리 | Claude Code | [release-build-signing.md](./release-build-signing.md) | Android targetSdk, release signing, iOS version/build 정리 |
| 4 | 브랜드 코드 반영 | Claude Code | [brand-strategy.md](./brand-strategy.md), [store-listing-metadata.md](./store-listing-metadata.md) | 앱 표시 이름, 내부 문구, 용어 정리 |
| 5 | 아이콘과 스토어 소재 제작 | Claude Code/디자이너 | [store-creative-assets.md](./store-creative-assets.md) | 앱 아이콘, Play feature graphic, 스크린샷 준비 |
| 6 | 스토어 메타데이터 확정 | 운영자/Claude Code | [store-listing-metadata.md](./store-listing-metadata.md) | 이름, 설명, 키워드, 심사 노트 최종본 |
| 7 | QA와 베타 테스트 | 운영자/Claude Code | [qa-beta-test-plan.md](./qa-beta-test-plan.md) | 핵심 플로우 통과, release 빌드 생성 |
| 8 | 스토어 콘솔 제출 | 운영자 | [ios-app-store-release.md](./ios-app-store-release.md), [google-play-release.md](./google-play-release.md) | App Store Connect/Play Console 제출 |
| 9 | 출시 후 운영 | 운영자 | [launch-operations.md](./launch-operations.md) | 리뷰/문의/크래시 확인, 1.0.1 후보 정리 |

최우선 게이트:

- 0단계 이름과 캐릭터 체계 확정 전에는 아이콘, 스크린샷, 스토어 레코드를 만들지 않는다.
- 1단계 식별자 확정 전에는 App Store Connect/Play Console 앱 레코드를 만들지 않는다.
- 2단계 정책 안전장치 전에는 어린이 대상 스토어 심사를 제출하지 않는다.

## 우선순위

### P0. 스토어 레코드 생성 전 결정

Claude Code가 코드 수정 전에 확인해야 할 결정:

- 최종 브랜드명
- 최종 Android `applicationId`
- 최종 iOS `PRODUCT_BUNDLE_IDENTIFIER`
- 개발자 표시 이름
- 지원 이메일
- 개인정보 처리방침 URL
- 지원 URL
- App Store Kids 카테고리 선택 여부
- Google Play 대상 연령대

주의: Android package name과 iOS Bundle ID는 첫 배포 후 변경이 어렵거나 불가능하다. `com.mathapp.practice`는 프로토타입명이라 첫 출시 전에 최종 값으로 바꾸는 것을 권장한다.

### P1. 정책 리스크 제거 ✓ 완료 2026-04-27

작업 문서: [privacy-and-kids-compliance.md](./privacy-and-kids-compliance.md), [release-build-signing.md](./release-build-signing.md)

- iOS `PrivacyInfo.xcprivacy` 추가 ✓
- `NSUserDefaults` 사용 사유 `CA92.1` 선언 ✓
- Android `targetSdk`를 Google Play 현재 요구사항에 맞춤 (TODO: P3에서 34→35 상향)
- 광고/분석/결제/소셜/UGC SDK 미포함 상태 유지 ✓
- 부모 설정 진입 전 부모 게이트 추가 ✓
- 실제 알림 기능이 없는 알림 설정 UI 제거 ✓
- 계정 기능이 없는 계정/로그아웃 UI 제거 ✓
- `android:allowBackup="false"` 적용 ✓

### P2. 브랜드 적용

작업 문서: [brand-strategy.md](./brand-strategy.md), [store-creative-assets.md](./store-creative-assets.md)

- 앱 표시 이름 변경
- 앱 아이콘 교체
- 온보딩/홈/탭/꾸미기 상점/탐험 기록 용어 정리
- 셈토끼를 대표 안내자로 아이콘과 스토어 소재에 반영

### P3. 배포 빌드 준비

작업 문서: [release-build-signing.md](./release-build-signing.md)

- Android release signing 설정
- Android App Bundle 생성
- iOS Archive 생성
- versionCode/build number 증가 규칙 고정
- release 빌드에서 디버그/임시 문구 제거

### P4. 스토어 등록 준비

작업 문서: [ios-app-store-release.md](./ios-app-store-release.md), [google-play-release.md](./google-play-release.md), [store-listing-metadata.md](./store-listing-metadata.md), [store-creative-assets.md](./store-creative-assets.md)

- App Store/Google Play 메타데이터 입력
- 스크린샷 제작
- 개인정보 처리방침과 지원 페이지 공개
- 심사 노트 작성
- Google Play Data safety와 Target audience 답변 작성

### P5. 베타와 런칭

작업 문서: [qa-beta-test-plan.md](./qa-beta-test-plan.md), [launch-operations.md](./launch-operations.md)

- Android 내부 테스트
- iOS TestFlight
- Google 개인 개발자 계정이 2023-11-13 이후 생성된 경우 12명 이상 14일 연속 closed testing 요구사항 확인
- 스토어 심사 제출
- 단계적 출시 또는 수동 출시

## Claude Code 작업 방식

Claude Code는 각 문서를 읽고 아래 형식으로 작업한다.

1. 문서의 `수정 대상 파일`을 먼저 연다.
2. `금지 사항`을 확인한다.
3. `작업 순서`대로 작은 단위로 수정한다.
4. `검증 명령`을 실행한다.
5. `완료 기준`을 체크한다.
6. 스토어 콘솔처럼 코드로 처리할 수 없는 항목은 `운영자 수동 작업`으로 남긴다.

## 정책 출처

- Apple App Review Guidelines, last updated 2026-02-06: https://developer.apple.com/app-store/review/guidelines/
- Apple App privacy details: https://developer.apple.com/app-store/app-privacy-details/
- Apple App Store Connect metadata reference: https://developer.apple.com/help/app-store-connect/reference/app-information/app-information
- Apple screenshot specifications: https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications
- Google Play target API requirement, last updated 2026-04-22: https://developer.android.com/google/play/requirements/target-sdk
- Google Play store listing setup: https://support.google.com/googleplay/android-developer/answer/9859152
- Google Play preview assets: https://support.google.com/googleplay/android-developer/answer/9866151
- Google Play Families policies: https://support.google.com/googleplay/android-developer/answer/9898834
- Google Play Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
