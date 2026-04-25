# 스토어 크리에이티브 소재

이 문서는 아이콘, 스크린샷, feature graphic, preview video 제작 기준이다. 제작물은 실제 앱 화면과 [brand-strategy.md](./brand-strategy.md)의 톤을 따라야 한다.

## 현재 상태

- iOS AppIcon: `ios/Math/Assets.xcassets/AppIcon.appiconset/AppIcon.png`, 1024x1024 PNG
- Android adaptive icon: 검은 배경 + 흰색 `M` 벡터
- 출시 브랜드 `셈토끼`와 현재 아이콘이 맞지 않음
- 스토어 스크린샷/feature graphic 없음

## 아이콘

### 콘셉트

- 셈토끼 얼굴 클로즈업
- 별 배지
- 작은 `+` 기호
- 밝은 하늘색/민트 배경
- 텍스트 없음

### 파일 산출물

| 플랫폼 | 파일 | 요구 |
|---|---|---|
| iOS | `ios/Math/Assets.xcassets/AppIcon.appiconset/AppIcon.png` | 1024x1024 PNG, RGB, alpha 없음 |
| Android | `android/app/src/main/res/drawable/ic_launcher_foreground.xml` 또는 PNG/vector 세트 | adaptive icon foreground |
| Android | `android/app/src/main/res/drawable/ic_launcher_background.xml` | 밝은 배경 |
| Google Play | `store-assets/google-play/icon-512.png` | 512x512 PNG |

Android는 작은 화면 식별성을 위해 벡터보다 고품질 PNG foreground를 써도 된다. 텍스트가 없는지 반드시 확인한다.

## Google Play Feature Graphic

필수 산출물:

```text
store-assets/google-play/feature-graphic-1024x500.png
```

요구:

- 1024x500
- JPEG 또는 24-bit PNG
- alpha 없음
- 핵심 요소가 중앙과 좌측에 오도록 배치
- 작은 화면에서 읽기 어려운 세부 묘사 금지

권장 구성:

- 왼쪽: 셈토끼 대표 마스코트와 별
- 오른쪽: 실제 앱 화면 1-2개를 카드처럼 배치
- 문구: `셈토끼와 떠나는 첫 연산 모험`
- 과한 CTA, 가격, `1위`, `최고` 문구 금지

## App Store 스크린샷

Apple은 iPhone 앱에 1-10장 스크린샷을 요구한다. iPad를 지원하면 iPad 스크린샷도 필요하다.

권장 산출물:

```text
store-assets/app-store/iphone-6-9/01-home.png
store-assets/app-store/iphone-6-9/02-stage-map.png
store-assets/app-store/iphone-6-9/03-game.png
store-assets/app-store/iphone-6-9/04-result.png
store-assets/app-store/iphone-6-9/05-report.png
store-assets/app-store/iphone-6-9/06-treasure.png
store-assets/app-store/ipad-13/01-home.png
store-assets/app-store/ipad-13/02-stage-map.png
store-assets/app-store/ipad-13/03-game.png
store-assets/app-store/ipad-13/04-result.png
```

권장 해상도:

- iPhone 6.9 inch portrait: 1320x2868 또는 App Store Connect가 허용하는 해당 기기 해상도
- iPad 13 inch portrait: 2064x2752 또는 2048x2732

스크린샷 카피:

| 순서 | 화면 | 카피 |
|---|---|---|
| 1 | 홈/오늘의 퀘스트 | `오늘의 연산 모험을 시작해요` |
| 2 | 스테이지 맵 | `덧셈부터 나눗셈까지 차근차근` |
| 3 | 문제 풀이 | `객관식과 숫자 입력으로 연습` |
| 4 | 결과 | `별을 모으며 성취감을 쌓아요` |
| 5 | 탐험 기록 | `학습 흐름을 한눈에 확인` |
| 6 | 별보물함 | `보물을 열고 모험 친구를 꾸며요` |

규칙:

- 실제 앱 화면을 기반으로 한다.
- 상태바/기기 프레임 사용 여부는 세트 전체에서 통일한다.
- 아이 이름이나 실제 개인정보를 넣지 않는다.
- 과장된 성과 문구를 쓰지 않는다.
- Android 또는 Google Play 로고를 iOS 스크린샷에 넣지 않는다.

## Google Play 스크린샷

필수:

- 최소 2장
- JPEG 또는 24-bit PNG, alpha 없음
- 최소 320px, 최대 3840px
- 긴 변은 짧은 변의 2배를 넘지 않음

권장:

- 최소 4장
- portrait 1080x1920 이상
- 첫 3장에 핵심 UI 노출

산출물:

```text
store-assets/google-play/phone/01-home.png
store-assets/google-play/phone/02-stage-map.png
store-assets/google-play/phone/03-game.png
store-assets/google-play/phone/04-result.png
store-assets/google-play/phone/05-report.png
store-assets/google-play/phone/06-treasure.png
```

## Preview Video

1차 출시는 선택 사항이다. 영상이 있으면 전환에는 도움이 되지만 제작/정책 부담이 늘어난다.

App Store preview:

- 선택 사항
- 15-30초
- 최대 3개
- 실제 앱 화면 캡처 기반

Google Play preview video:

- YouTube public 또는 unlisted URL 필요
- 광고 꺼짐
- age-restricted 아님
- embeddable 가능

1차 출시 권장:

- 영상 없이 스크린샷 품질에 집중
- 앱 안정화 후 15초 영상 추가

## Claude Code 작업

수정 대상:

- `ios/Math/Assets.xcassets/AppIcon.appiconset/AppIcon.png`
- `android/app/src/main/res/drawable/ic_launcher_background.xml`
- `android/app/src/main/res/drawable/ic_launcher_foreground.xml`
- `android/app/src/main/res/drawable/ic_launcher_legacy.xml`
- `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- 새 디렉터리: `store-assets/`

작업 순서:

1. 최종 아이콘 이미지를 준비한다.
2. iOS 1024x1024 앱 아이콘을 교체한다.
3. Android adaptive icon을 교체한다.
4. 앱 설치 후 홈 화면에서 작은 아이콘 식별성을 확인한다.
5. 스토어용 512x512 아이콘과 1024x500 feature graphic을 만든다.
6. iPhone/Android 주요 화면 스크린샷을 촬영한다.
7. 스크린샷 카피를 오버레이한다면 모든 플랫폼에서 문구와 줄바꿈을 검수한다.

완료 기준:

- 아이콘에 글자가 없다.
- 작은 크기에서도 셈토끼와 별이 보인다.
- feature graphic은 1024x500이고 alpha가 없다.
- 스크린샷은 실제 앱 화면 중심이다.
- 모든 소재가 브랜드 팔레트와 충돌하지 않는다.

## 정책 출처

- Apple screenshot specifications: https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications
- Apple upload screenshots and previews: https://developer.apple.com/help/app-store-connect/manage-app-information/upload-app-previews-and-screenshots
- Apple app preview specifications: https://developer.apple.com/help/app-store-connect/reference/app-information/app-preview-specifications
- Google Play preview assets: https://support.google.com/googleplay/android-developer/answer/9866151
