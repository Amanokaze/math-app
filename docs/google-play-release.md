# Google Play 출시 실행 문서

이 문서는 Google Play Console 제출을 위한 코드/콘솔 작업 기준이다. 실제 입력 문구는 [store-listing-metadata.md](./store-listing-metadata.md)를 따른다.

## 현재 상태 요약

- App name: `Math`
- `applicationId`: `com.mathapp.practice`
- `compileSdk`: `34`
- `targetSdk`: `34`
- `versionCode`: `1`
- `versionName`: `1.0`
- Android App Bundle signing: 미설정
- 광고/분석/결제/네트워크 권한: 없음

## Play 출시 전략

권장 콘솔 설정:

| 필드 | 값 |
|---|---|
| App or game | `App` |
| Free or paid | `Free` |
| Category | `Education` |
| Tags | `Education`, `Math`, `Kids`, `Learning` 계열 중 콘솔 제공값 선택 |
| Target audience | `Ages 6-8`, `Ages 9-12` |
| Ads | `No` |
| In-app purchases | `No` |
| Contains user-generated content | `No` |
| Designed for Families | 정책 확인 후 opt-in |
| Privacy policy | 공개 URL 입력 |

`게임`이 아니라 `교육 앱`으로 출시하는 이유:

- 현재 수익화가 없고 교육 목적이 명확하다.
- Google Play Games on PC, 게임 스토어 기대치, 추가 게임 소재 부담을 피한다.
- 스토어 문구도 `게임`보다 `연산 놀이`를 기본으로 쓴다.

## 필수 코드 작업

수정 대상:

- `android/app/build.gradle.kts`
- `android/shared/build.gradle.kts`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/res/drawable/*`
- `android/app/src/main/res/mipmap-anydpi-v26/*`
- 필요 시 앱 아이콘 리소스 추가

작업:

1. 최종 `applicationId` 확정
2. `compileSdk`와 `targetSdk`를 현재 Play 요구사항 이상으로 올림
3. `android:label`을 `셈토끼` 또는 `@string/app_name`으로 변경
4. release signing 설정
5. 셈토끼 기반 adaptive icon 교체
6. AAB 생성

2026-04-25 기준으로 Google 공식 문서는 2025-08-31부터 신규 앱과 업데이트가 Android 15, API 35 이상을 target 해야 한다고 안내한다. 현재 `targetSdk = 34`는 출시 전 업데이트가 필요하다.

검증:

```bash
cd android
./gradlew :app:clean :app:assembleDebug
./gradlew :app:bundleRelease
./gradlew :app:processReleaseMainManifest
```

완료 기준:

- `app-release.aab` 생성
- `targetSdk >= 35`
- `android.permission.AD_ID` 없음
- `android.permission.INTERNET` 없음
- 위치/카메라/마이크/연락처 권한 없음
- 앱 이름 `셈토끼`
- 아이콘이 `M` 로고가 아님

## Store listing

문구는 [store-listing-metadata.md](./store-listing-metadata.md)를 사용한다.

필드 제한:

- App name: 30자
- Short description: 80자
- Full description: 4000자

주의:

- 짧은 설명에는 이모지, 반복 특수문자, 과도한 키워드, `다운로드`, `1위`, `최고`, `무료 이벤트` 같은 표현을 넣지 않는다.
- 스크린샷은 실제 앱 경험을 보여야 한다.
- Feature graphic은 필수이며 1024x500 JPEG 또는 24-bit PNG, alpha 없음이다.

## Preview assets

필수:

- App icon: 512x512 PNG, Google Play용 별도 업로드
- Feature graphic: 1024x500 JPEG 또는 24-bit PNG, alpha 없음
- Phone screenshots: 최소 2장, 권장 4장 이상

권장:

- Portrait screenshots 1080x1920 이상
- 첫 3장은 앱 핵심 경험이 바로 보이도록 구성
- 텍스트 오버레이는 최소화하고, 앱 화면이 중심이어야 함

상세 규칙은 [store-creative-assets.md](./store-creative-assets.md)를 따른다.

## Data Safety 답변

현재 빌드 기준:

| 질문 | 답변 |
|---|---|
| 앱이 사용자 데이터를 수집하거나 공유하는가 | `No` |
| 데이터가 전송 중 암호화되는가 | 데이터 전송 없음 |
| 사용자가 데이터 삭제를 요청할 수 있는가 | 서버/계정 데이터 없음. 앱 내 기록 초기화와 앱 삭제로 로컬 데이터 삭제 가능 |
| 앱이 광고 ID를 사용하는가 | `No` |
| 앱이 위치를 수집하는가 | `No` |
| 앱이 개인 정보나 민감 정보를 수집하는가 | `No` |

주의: 앱에 네트워크 SDK, crash reporting, analytics, ads, 로그인, 결제, 문의 폼을 추가하면 이 답변은 다시 작성해야 한다.

## Target Audience and Content

권장 답변:

- Target age groups: `6-8`, `9-12`
- Store listing appeals to children: `Yes`
- Ads: `No`
- App collects personal and sensitive information from children: `No`
- App has social features: `No`
- App has UGC: `No`
- App has external links: 현재 앱 내부에는 없음. 개인정보 처리방침/지원 URL은 스토어 페이지에서 제공

Families 정책상 어린이 대상 앱은 AAID, 위치, 민감 식별자, 부적절한 SDK에 특히 엄격하다. 출시 전 `rg -n "AD_ID|AdvertisingId|Firebase|AdMob|Analytics|Billing|INTERNET|ACCESS_FINE_LOCATION|CAMERA|RECORD_AUDIO" android`를 실행해 확인한다.

## 테스트 트랙

운영자 수동 작업:

1. Internal testing에 AAB 업로드
2. 가족/지인/테스터 기기로 설치 검증
3. 개인 개발자 계정이 2023-11-13 이후 생성된 경우 closed testing 요구사항 확인
4. 필요한 경우 12명 이상이 14일 연속 opt-in 상태를 유지하도록 closed test 진행
5. production access 신청

## 제출 전 체크리스트

- [ ] 최종 package name이 확정됐다.
- [ ] `targetSdk`가 현재 요구사항 이상이다.
- [ ] AAB release 빌드가 생성됐다.
- [ ] 앱 이름과 아이콘이 브랜드와 일치한다.
- [ ] 개인정보 처리방침 URL이 열린다.
- [ ] Data safety 답변이 현재 코드와 일치한다.
- [ ] Target audience 답변이 스토어 문구와 일치한다.
- [ ] Families 정책상 금지 SDK가 없다.
- [ ] 스크린샷과 feature graphic이 준비됐다.

## 정책 출처

- Google Play target API requirement: https://developer.android.com/google/play/requirements/target-sdk
- Google Play app setup: https://support.google.com/googleplay/android-developer/answer/9859152
- Google Play preview assets: https://support.google.com/googleplay/android-developer/answer/9866151
- Google Play metadata policy: https://support.google.com/googleplay/android-developer/answer/9898842
- Google Play Families policies: https://support.google.com/googleplay/android-developer/answer/9898834
- Google Play Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play testing requirements for new personal accounts: https://support.google.com/googleplay/android-developer/answer/14151465
