# App Store 출시 실행 문서

이 문서는 App Store Connect 제출을 위한 코드/콘솔 작업 기준이다. 실제 입력 문구는 [store-listing-metadata.md](./store-listing-metadata.md)를 따른다.

## 현재 상태 요약

- 앱 표시 이름: `Math`
- Bundle ID: `com.mathapp.practice`
- 버전: `1.0`
- 빌드 번호: `1`
- 아이콘: 1024x1024 PNG는 있으나 출시 브랜드와 맞지 않음
- Privacy Manifest: 없음
- 로그인: 없음
- 결제: 없음
- 광고: 없음
- 분석 SDK: 없음
- 서버/네트워크: 없음

## App Store 전략

권장 분류:

- Primary Category: `Education`
- Secondary Category: 비워두거나 `Games`
- Made for Kids: 장기적으로 어린이 전용 정책을 지킬 수 있을 때만 선택
- 권장 age band: `6-8` 또는 `9-11`

선택 기준:

- 덧셈/뺄셈 중심으로 포지셔닝하면 `6-8`
- 곱셈/나눗셈까지 전면에 내세우면 `9-11`
- 1차 출시 문구는 `초등 저학년`, `첫 사칙연산`, `연산 놀이`를 중심으로 한다.

주의: `Made for Kids`는 승인 후 이후 업데이트에도 Kids 카테고리 요구사항이 계속 적용된다. 선택 전 [privacy-and-kids-compliance.md](./privacy-and-kids-compliance.md)를 모두 통과해야 한다.

## 코드 작업

수정 대상:

- `ios/Math/Info.plist`
- `ios/Math.xcodeproj/project.pbxproj`
- `ios/Math/Assets.xcassets/AppIcon.appiconset/AppIcon.png`
- 새 파일: `ios/Math/PrivacyInfo.xcprivacy`

작업:

1. 최종 Bundle ID 확정
2. `CFBundleDisplayName`을 `셈토끼`로 변경
3. `PrivacyInfo.xcprivacy` 추가
4. AppIcon 교체
5. Release 빌드 확인
6. Archive 생성

검증:

```bash
plutil -p ios/Math/Info.plist
plutil -lint ios/Math/PrivacyInfo.xcprivacy
xcodebuild -project ios/Math.xcodeproj -scheme Math -configuration Release -sdk iphonesimulator build
```

## App Store Connect 입력값

앱 정보:

| 필드 | 값 |
|---|---|
| Name | `셈토끼` |
| Subtitle | `모험 친구와 배우는 사칙연산` |
| Primary Language | `Korean` |
| Bundle ID | 최종 Bundle ID |
| SKU | `semtokki-ios-001` 또는 내부 규칙 |
| Privacy Policy URL | 공개된 개인정보 처리방침 URL |
| Category | `Education` |
| Age Rating | 설문에 따라 산출, Kids 선택 시 6-8 또는 9-11 |
| License Agreement | Apple Standard EULA |

버전 정보:

| 필드 | 값 |
|---|---|
| Version | `1.0.0` |
| Promotional Text | [store-listing-metadata.md](./store-listing-metadata.md)의 App Store 문구 |
| Description | 같은 문서의 App Store 설명 |
| Keywords | 같은 문서의 키워드 |
| Support URL | 실제 문의/지원 페이지 URL |
| Marketing URL | 선택, 없으면 비움 |
| Copyright | `2026 OWNER_NAME` |

## App Review Notes 초안

아래 문구를 실제 제출 시 상황에 맞게 넣는다.

```text
셈토끼는 어린이가 사칙연산을 연습하는 오프라인 학습 앱입니다.

로그인, 계정 생성, 인앱 결제, 광고, 채팅, 사용자 생성 콘텐츠는 없습니다.
앱의 모든 주요 기능은 설치 후 바로 사용할 수 있습니다.
학습 진행 상황, 별 기록, 별코인, 별점수, 보물, 설정 값은 기기 내에만 저장되며 외부 서버로 전송되지 않습니다.

설정 화면과 기록 초기화는 부모 게이트 또는 확인 절차 뒤에 배치되어 있습니다.
iOS에서 UserDefaults를 앱 자체 설정과 학습 진행 저장에 사용하므로 PrivacyInfo.xcprivacy에 NSPrivacyAccessedAPICategoryUserDefaults / CA92.1을 선언했습니다.
```

## 스크린샷

필수:

- iPhone용 1-10장
- 앱이 iPad에서 실행 가능하면 iPad용 스크린샷도 필요

권장 세트:

1. 홈/오늘의 퀘스트
2. 스테이지 맵
3. 문제 풀이 화면
4. 결과와 별 보상
5. 탐험 기록
6. 별보물함 또는 친구 꾸미기

제작 규칙은 [store-creative-assets.md](./store-creative-assets.md)를 따른다.

## 개인정보 섹션

현재 빌드 기준:

- Data collected: `Data Not Collected`
- Tracking: `No`
- Privacy Policy URL: 필수

단, Crash reporting SDK, Analytics SDK, 서버 저장, 계정, 이메일 문의 폼을 앱 안에 추가하면 답변이 바뀐다.

## 제출 전 체크리스트

- [ ] 앱 이름과 아이콘이 브랜드 문서와 일치한다.
- [ ] `Math`라는 이름이 앱 내부와 스토어 소재에 남아 있지 않다.
- [ ] `PrivacyInfo.xcprivacy`가 있다.
- [ ] 개인정보 처리방침 URL이 실제로 열린다.
- [ ] 지원 URL에 연락처가 있다.
- [ ] 모든 스크린샷은 실제 앱 화면 기반이다.
- [ ] 앱이 첫 실행부터 핵심 기능을 사용할 수 있다.
- [ ] 외부 링크/구매/광고/분석/UGC가 없다.
- [ ] 실제 기기 또는 시뮬레이터에서 온보딩부터 결과 화면까지 통과했다.
- [ ] Review Notes가 현재 기능과 일치한다.

## 정책 출처

- Apple App Review Guidelines: https://developer.apple.com/app-store/review/guidelines/
- Apple Kids apps: https://developer.apple.com/kids/
- Apple App information reference: https://developer.apple.com/help/app-store-connect/reference/app-information/app-information
- Apple platform version information: https://developer.apple.com/help/app-store-connect/reference/app-information/platform-version-information
- Apple screenshot specifications: https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications
- Apple App Privacy Details: https://developer.apple.com/app-store/app-privacy-details/
