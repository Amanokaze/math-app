# QA 및 베타 테스트 계획

이 문서는 스토어 제출 전 기능, 정책, 기기 호환성을 검증하기 위한 체크리스트다.

## QA 목표

- 앱이 첫 실행부터 학습 완료까지 중단 없이 동작한다.
- 어린이 앱 정책을 깨는 권한, SDK, 링크, 결제, 광고가 없다.
- Android/iOS 모두 스토어 제출 가능한 release 빌드가 나온다.
- 스크린샷에 들어간 모든 화면이 실제 앱에서 재현된다.

## 사전 빌드 검증

Android:

```bash
cd android
./gradlew :app:clean :app:assembleDebug
./gradlew :app:bundleRelease
./gradlew :app:processReleaseMainManifest
```

iOS:

```bash
plutil -lint ios/Math/PrivacyInfo.xcprivacy
xcodebuild -project ios/Math.xcodeproj -scheme Math -configuration Release -sdk iphonesimulator build
```

정책 grep:

```bash
rg -n "AD_ID|AdvertisingId|AdMob|Analytics|Firebase|Billing|StoreKit|ACCESS_FINE_LOCATION|CAMERA|RECORD_AUDIO" android/app android/shared ios/Math
rg -n "<uses-permission" android/app/src/main/AndroidManifest.xml
rg -n "https?://" android/shared/src ios/Math -g '!Info.plist'
```

HTML 기획 파일의 외부 링크는 앱 바이너리에 포함되지 않는 참고 문서이므로 별도 판단한다.

## 기기 매트릭스

최소:

| 플랫폼 | 기기 |
|---|---|
| Android | 작은 화면 1대, 일반 폰 1대, 태블릿 또는 큰 화면 1대 |
| iOS | iPhone 작은 화면 1개, iPhone 큰 화면 1개 |
| iPad | 앱이 iPad를 지원하면 iPad 1개 |

권장 OS:

- Android 8.0 API 26
- Android 13
- Android 15 이상
- iOS 17
- 최신 iOS

## 핵심 플로우 테스트

### 첫 실행

- [ ] 앱 표시 이름이 `셈토끼`이다.
- [ ] 아이콘이 셈토끼 기반이다.
- [ ] 온보딩에 `Math`가 남아 있지 않다.
- [ ] 언어가 기기 언어에 맞게 초기화된다.
- [ ] 캐릭터 선택 후 홈으로 이동한다.

### 홈

- [ ] 오늘의 퀘스트 카드가 보인다.
- [ ] 하트, 별점수, 별코인 표시가 깨지지 않는다.
- [ ] 연산별 진행률이 보인다.
- [ ] 설정 버튼 동작이 부모 게이트 정책과 일치한다.

### 스테이지

- [ ] 덧셈 1단계 진입 가능
- [ ] 뺄셈/곱셈/나눗셈 잠금/해금 정책 정상
- [ ] 하트가 부족할 때 진입이 막힌다.
- [ ] 스테이지 완료 시 다음 스테이지가 열린다.
- [ ] 최고 별 기록이 낮아지지 않는다.

### 문제 풀이

- [ ] 3-2-1 카운트다운
- [ ] 객관식 선택
- [ ] 주관식 숫자 키패드
- [ ] 하드웨어 키보드 입력
- [ ] 가로/세로 회전
- [ ] 뒤로가기/종료 확인

### 결과

- [ ] 별 1/2/3개 조건이 정확하다.
- [ ] 별점수와 별코인이 지급된다.
- [ ] 다시 하기
- [ ] 다음 단계로
- [ ] 홈으로

### 퀘스트

- [ ] 오늘의 퀘스트 생성
- [ ] 퀘스트 진행률 증가
- [ ] 완료 보너스 지급
- [ ] 날짜 변경 시 새 퀘스트 생성

### 탐험 기록

- [ ] 오늘 푼 문제 수
- [ ] 정확도
- [ ] 연속 학습일
- [ ] 주간 막대 차트
- [ ] 배지 표시

### 별보물함과 친구 꾸미기

- [ ] 별점수 기준 보물 해금
- [ ] 대표 보물 설정
- [ ] 별코인으로 아이템 구매
- [ ] 보유 아이템 장착
- [ ] 별코인 부족 상태

### 설정

- [ ] 언어 변경
- [ ] 라이트/다크 모드
- [ ] 기록 초기화
- [ ] 부모 게이트 통과/실패 (랜덤 문제, 오답 시 이동 차단)
- [ ] 알림 설정 UI가 노출되지 않음 (P1에서 제거됨)

## 접근성 및 사용성

- [ ] 작은 화면에서 버튼 텍스트가 잘리지 않는다.
- [ ] 다크 모드에서 대비가 충분하다.
- [ ] 중요한 정보가 색상만으로 전달되지 않는다.
- [ ] 숫자 키패드 버튼 터치 영역이 충분하다.
- [ ] 아이가 실수로 결제/외부 링크/개인정보 입력으로 이동할 경로가 없다.

## 베타 테스트

### TestFlight

- 내부 테스터 3-5명
- 부모 1명 이상, 실제 대상 연령 아이 1명 이상 관찰 권장
- 피드백 질문:
  - 아이가 첫 스테이지까지 혼자 갈 수 있었나?
  - 문제 난이도가 너무 어렵거나 쉽지 않았나?
  - 보상/별/보물 구조를 이해했나?
  - 부모가 광고/결제/개인정보 측면에서 안심했나?

### Google Play

- Internal testing으로 먼저 설치 검증
- 개인 개발자 계정이 2023-11-13 이후 생성된 경우 closed testing 12명 이상, 14일 연속 opt-in 요구사항 확인
- closed testing 중 crash, ANR, 기기 호환성, 사용자 피드백 기록

## 출시 차단 기준

아래 항목 중 하나라도 있으면 제출하지 않는다.

- 온보딩에서 crash 발생
- 스테이지 완료가 불가능
- 기록 저장/복원이 깨짐
- release 빌드 실패
- 개인정보 처리방침 URL 없음
- App Store/Play 설명과 실제 기능 불일치
- 광고/분석/결제 SDK가 의도치 않게 포함됨
- Android targetSdk 요구사항 미달
- iOS Privacy Manifest 누락

## 완료 산출물

- Android release AAB
- iOS archive
- QA 체크리스트 결과
- 베타 피드백 요약
- 알려진 이슈 목록
- 스토어 제출 여부 결정

## 정책 출처

- Apple App Review Guidelines: https://developer.apple.com/app-store/review/guidelines/
- Google Play app testing requirements: https://support.google.com/googleplay/android-developer/answer/14151465
- Google Play target API requirement: https://developer.android.com/google/play/requirements/target-sdk
