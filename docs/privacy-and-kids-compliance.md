# 개인정보 및 어린이 정책 대응

이 문서는 출시 전 개인정보, 어린이 대상 앱, 부모 게이트, 스토어 선언을 정리한다. 코드 구현자는 이 문서를 먼저 읽고 정책을 깨는 SDK나 기능을 추가하지 않는다.

- 기준일: 2026-04-25
- 출시 기본 전략: 어린이 대상 교육 앱
- 데이터 전략: 기기 내 로컬 저장만 사용
- 수익화 전략: 광고 없음, 인앱 결제 없음
- 소셜 전략: 채팅, 사용자 생성 콘텐츠, 랭킹, 친구 초대 없음

## 현재 데이터 인벤토리

현재 코드 기준으로 확인된 저장/처리 항목:

| 항목 | 저장 위치 | 오프디바이스 전송 | 개인정보 여부 | 스토어 선언 방향 |
|---|---|---:|---:|---|
| 선택 캐릭터 | SharedPreferences/NSUserDefaults | 없음 | 아니오 | 수집 아님 |
| 언어/테마 설정 | SharedPreferences/NSUserDefaults | 없음 | 아니오 | 수집 아님 |
| 스테이지 별 기록 | SharedPreferences/NSUserDefaults | 없음 | 아니오 | 수집 아님 |
| 오늘 푼 문제 수/정답 수 | SharedPreferences/NSUserDefaults | 없음 | 아니오 | 수집 아님 |
| 연속 학습일 | SharedPreferences/NSUserDefaults | 없음 | 아니오 | 수집 아님 |
| 별코인/별점수/보물/아이템 | SharedPreferences/NSUserDefaults | 없음 | 아니오 | 수집 아님 |
| 기기 언어 조회 | OS locale | 없음 | 아니오 | 수집 아님 |

현재 Android Manifest에는 `INTERNET`, `AD_ID`, 위치, 카메라, 마이크, 연락처 권한이 없다. 현재 의존성에는 광고, 분석, 결제, Firebase, 네트워크 SDK가 없다.

## 출시 정책 원칙

출시 1차에서는 아래 원칙을 깨지 않는다.

- 서버 계정 생성 없음
- 이메일/이름/생년월일 입력 없음
- 광고 SDK 없음
- 분석 SDK 없음
- 결제/Billing/StoreKit 상품 없음
- 랭킹/친구/채팅/UGC 없음
- 외부 웹 링크는 앱 내부에 노출하지 않음
- 학습 기록은 기기 내에만 저장

이 원칙을 깨는 기능을 추가해야 한다면, 스토어 메타데이터와 개인정보 처리방침, Data safety, App Privacy를 다시 작성한다.

## 어린이 앱 리스크

### Apple

App Store Kids 카테고리를 선택하면 이후 업데이트도 계속 Kids 카테고리 가이드라인을 따라야 한다. Apple은 Kids 카테고리 앱에 대해 외부 링크, 구매 기회, 아이의 주의를 흩뜨리는 요소를 부모 게이트 뒤에 둘 것을 요구하고, 개인 식별 정보나 기기 정보를 제3자에게 보내지 않는 방향을 요구한다.

출시자가 장기적으로 아래 조건을 지킬 수 있으면 `Made for Kids`를 선택한다.

- 광고 없음 또는 어린이 정책을 충족하는 문맥 광고만 사용
- 제3자 분석 없음
- 외부 링크는 부모 게이트 뒤로 이동
- 구매는 부모 게이트 뒤로 이동
- 개인정보 수집 없음 또는 법적 동의 흐름 구축

### Google Play

Google Play에서 대상 연령에 어린이가 포함되면 Families 정책과 Data safety를 정확히 답해야 한다. 앱이 어린이 전용이면 AAID를 전송하면 안 되며, 앱 또는 라이브러리가 `AD_ID` 권한을 선언하지 않는 상태를 유지한다.

권장 Play Console 답변:

- App or game: `App`
- Category: `Education`
- Target audience: `Ages 6-8`, `Ages 9-12`
- Ads: `No`
- In-app purchases: `No`
- User-generated content: `No`
- Social features: `No`
- Location: `No`
- Data collection: 현재 빌드 기준 `No`
- Data sharing: 현재 빌드 기준 `No`

## 부모 게이트 정책

현재 앱에는 부모 설정 화면이 있지만 부모 게이트가 없다. 출시 전에는 다음 중 하나를 선택한다.

권장안 A:

- 부모 설정 화면 진입 전에 부모 게이트를 추가한다.
- 부모 게이트는 아이가 우연히 풀기 어려운 간단한 성인 확인 문제로 한다.
- 예: `7 + 8 = ?`처럼 화면에 숫자 키패드로 답을 입력하게 한다.
- 게이트 통과 후 5분 동안만 부모 영역 접근을 허용한다.

대안 B:

- 외부 링크, 구매, 개인정보 입력이 없으므로 부모 설정 화면은 유지한다.
- 단, `기록 초기화` 같은 파괴적 액션만 부모 게이트 뒤로 옮긴다.

출시 안전성 기준으로는 A를 권장한다.

수정 대상 파일:

- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/MathApp.kt`
- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/ParentSettingsScreen.kt`
- 필요 시 새 파일: `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/ParentalGateDialog.kt`
- 문자열: `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/Localization.kt`

완료 기준:

- 설정 버튼을 눌렀을 때 부모 게이트가 먼저 열린다.
- 오답이면 설정 화면으로 이동하지 않는다.
- 정답이면 설정 화면으로 이동한다.
- 기록 초기화는 별도 확인 다이얼로그를 유지한다.
- 부모 게이트 문제와 답은 매번 바뀐다.

## iOS Privacy Manifest

현재 iOS 구현은 `NSUserDefaults`를 사용한다.

수정 대상 파일:

- 새 파일: `ios/Math/PrivacyInfo.xcprivacy`
- 수정 파일: `ios/Math.xcodeproj/project.pbxproj`

추가해야 하는 내용:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>NSPrivacyTracking</key>
    <false/>
    <key>NSPrivacyCollectedDataTypes</key>
    <array/>
    <key>NSPrivacyAccessedAPITypes</key>
    <array>
        <dict>
            <key>NSPrivacyAccessedAPIType</key>
            <string>NSPrivacyAccessedAPICategoryUserDefaults</string>
            <key>NSPrivacyAccessedAPITypeReasons</key>
            <array>
                <string>CA92.1</string>
            </array>
        </dict>
    </array>
</dict>
</plist>
```

검증 명령:

```bash
plutil -lint ios/Math/PrivacyInfo.xcprivacy
```

완료 기준:

- `PrivacyInfo.xcprivacy`가 앱 타깃 리소스에 포함된다.
- Xcode archive 후 privacy manifest가 번들에 포함된다.
- `NSUserDefaults` 사용 사유는 `CA92.1`이다.
- 추적 도메인과 수집 데이터는 비어 있다.

## 개인정보 처리방침 초안

스토어에는 공개 URL이 필요하다. 아래 문구를 웹 페이지로 게시하고, `OWNER_NAME`, `SUPPORT_EMAIL`, `SUPPORT_URL`을 실제 값으로 바꾼다.

```markdown
# 셈토끼 개인정보 처리방침

시행일: 2026-04-25

셈토끼는 어린이가 사칙연산을 연습할 수 있도록 만든 학습 앱입니다. 셈토끼는 이름, 이메일, 전화번호, 위치정보, 사진, 음성, 연락처, 광고 식별자와 같은 개인정보를 수집하지 않습니다.

앱에서 생성되는 학습 진행 상황, 별 기록, 별코인, 별점수, 보물, 캐릭터 선택, 언어와 화면 설정은 사용자의 기기 안에만 저장됩니다. 이 정보는 개발자 서버나 제3자에게 전송되지 않습니다.

셈토끼는 광고를 표시하지 않으며, 제3자 분석 SDK를 사용하지 않고, 인앱 결제를 제공하지 않습니다. 앱에는 채팅, 친구 추가, 사용자 생성 콘텐츠, 공개 랭킹 기능이 없습니다.

사용자가 이메일로 문의하는 경우, 답변을 위해 이메일 주소와 문의 내용을 확인할 수 있습니다. 이 정보는 고객 지원 목적으로만 사용하며 앱 안의 학습 기록과 연결하지 않습니다.

사용자는 앱의 설정 화면에서 학습 기록을 초기화할 수 있으며, 기기에서 앱을 삭제하면 앱에 저장된 로컬 데이터도 함께 삭제됩니다.

문의가 필요하면 아래 연락처로 연락해 주세요.

- 운영자: OWNER_NAME
- 지원 이메일: SUPPORT_EMAIL
- 지원 페이지: SUPPORT_URL

정책이 변경되는 경우 이 페이지에 변경 내용을 게시합니다.
```

## 스토어 선언 초안

### App Store App Privacy

현재 빌드 기준:

- Data collected: `Data Not Collected`
- Tracking: `No`
- Privacy Policy URL: 공개 URL 입력
- Privacy Choices URL: 선택 사항, 현재는 입력하지 않아도 됨

주의: Apple은 게임 저장, 게임플레이 로직, 사용량 데이터가 오프디바이스로 수집되면 관련 데이터 유형을 선언하도록 안내한다. 현재 앱은 로컬 저장만 하므로 수집으로 보지 않는다.

### Google Play Data Safety

현재 빌드 기준:

- Does your app collect or share any of the required user data types?: `No`
- Is all of the user data collected by your app encrypted in transit?: 데이터 전송 없음
- Do you provide a way for users to request that their data is deleted?: 계정/서버 데이터 없음. 앱 내 기록 초기화와 앱 삭제로 로컬 데이터 삭제 가능
- Committed to follow Play Families Policy: 대상 연령에 어린이가 포함되면 정책 확인 후 선택

## 금지 사항

출시 전 Claude Code는 아래 작업을 하지 않는다.

- Firebase Analytics, Google Analytics, AdMob, Meta SDK, Adjust, AppsFlyer, Sentry 등 데이터 전송 SDK 추가
- Android `INTERNET`, `AD_ID`, 위치, 카메라, 마이크 권한 추가
- iOS App Tracking Transparency 문구 추가
- 외부 링크 버튼 추가
- 아이 이름, 생년월일, 이메일 입력 UI 추가
- 실제 결제 없이 `꾸미기 상점`을 현금 구매처럼 보이게 만드는 문구 추가

## 정책 출처

- Apple App Review Guidelines: https://developer.apple.com/app-store/review/guidelines/
- Apple Kids apps: https://developer.apple.com/kids/
- Apple App Privacy Details: https://developer.apple.com/app-store/app-privacy-details/
- Apple required reason API: https://developer.apple.com/documentation/bundleresources/describing-use-of-required-reason-api
- Apple `NSPrivacyAccessedAPICategoryUserDefaults`: https://developer.apple.com/documentation/bundleresources/app-privacy-configuration/nsprivacyaccessedapitypes/nsprivacyaccessedapitype
- Google Play Families Policies: https://support.google.com/googleplay/android-developer/answer/9898834
- Google Play Families data practices: https://support.google.com/googleplay/android-developer/answer/11043825
- Google Play Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
