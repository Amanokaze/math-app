# 스토어 메타데이터

이 문서는 App Store와 Google Play에 입력할 문구의 기준이다. Claude Code는 앱 내부 문구를 정리할 때 이 문서의 톤과 단어를 따른다.

## 공통 원칙

- 앱 이름: `셈토끼`
- 브랜드 구조: `셈토끼`는 대표 안내자, 곰돌이/토끼/여우/부엉이는 사용자가 고르는 `모험 친구들`
- 표현: `게임`보다 `연산 놀이`, `모험`, `학습`을 우선 사용
- 대상: 초등 저학년 자녀와 부모
- 금지 표현: `1위`, `최고`, `완벽 보장`, `성적 향상 보장`, `무료 이벤트`, `다운로드하세요`
- 광고/결제가 없다는 점은 설명에 명확히 쓴다. 계정은 선택 사항(부모 게이트 후 연동 가능)으로 표현한다.
- Play 짧은 설명에는 이모지와 특수 장식을 쓰지 않는다.

## App Store

### App Name

```text
셈토끼
```

### Subtitle

```text
모험 친구와 배우는 사칙연산
```

### Promotional Text

```text
셈토끼의 안내를 따라 별을 모으고 보물을 열며 사칙연산을 차근차근 익혀요.
```

### Description

```text
셈토끼는 모험 친구들과 함께 사칙연산을 연습하는 어린이 연산 모험 앱입니다.

덧셈, 뺄셈, 곱셈, 나눗셈을 스테이지 방식으로 풀고, 별과 별코인을 모으며 자연스럽게 연산 연습을 이어갈 수 있습니다. 아이는 셈토끼의 안내를 따라 오늘의 퀘스트와 별보물함을 즐기고, 부모는 탐험 기록에서 학습 흐름을 간단히 확인할 수 있습니다.

주요 기능

• 덧셈, 뺄셈, 곱셈, 나눗셈 스테이지
• 객관식과 주관식 문제 풀이
• 오늘의 퀘스트와 별 보상
• 별보물함과 친구 꾸미기
• 곰돌이, 토끼, 여우, 부엉이 모험 친구 선택
• 학습 기록과 주간 진행 확인
• 한국어, 영어, 중국어, 일본어 지원

안심하고 사용할 수 있도록

• 광고 없음
• 인앱 결제 없음
• 계정 연동은 선택 사항 (부모 인증 후 설정 가능)
• 채팅과 친구 기능 없음
• 게스트 모드에서는 학습 기록이 기기 안에만 저장
• 계정 연동 시 기기 간 학습 데이터 백업 가능

셈토끼는 매일 짧게 풀고, 조금씩 성취를 쌓아가는 첫 사칙연산 경험을 목표로 합니다.
```

### Keywords

100 bytes 이하를 유지한다.

```text
연산,수학,덧셈,뺄셈,곱셈,나눗셈,사칙연산,초등,학습,놀이
```

### Support URL

```text
https://YOUR_DOMAIN/support
```

### Privacy Policy URL

```text
https://YOUR_DOMAIN/privacy
```

### App Review Notes

```text
셈토끼는 어린이가 사칙연산을 연습하는 교육 앱입니다. 인앱 결제, 광고, 채팅, 사용자 생성 콘텐츠는 없습니다. 기본 모드(게스트)에서는 학습 데이터가 기기 안에만 저장됩니다. 선택적 계정 연동 기능이 있으며, 부모 인증(수학 문제 풀기) 통과 후 설정 화면에서만 접근 가능합니다. 계정 연동 시 Supabase에 이메일과 학습 데이터가 저장됩니다. iOS UserDefaults 사용 사유는 PrivacyInfo.xcprivacy에 NSPrivacyAccessedAPICategoryUserDefaults / CA92.1로 선언했습니다.
```

## Google Play

### App Name

```text
셈토끼: 어린이 연산 놀이
```

### Short Description

80자 이하, 이모지 없음.

```text
셈토끼와 모험 친구들이 스테이지를 탐험하며 사칙연산을 익혀요
```

### Full Description

```text
셈토끼는 모험 친구들과 함께 별을 모으고 스테이지를 깨며 덧셈, 뺄셈, 곱셈, 나눗셈을 익히는 어린이 연산 학습 앱입니다.

아이들은 셈토끼의 안내를 따라 문제를 풀고 별, 별코인, 보물을 얻으며 다음 스테이지로 나아갑니다. 객관식 문제와 숫자 키패드 입력 문제를 함께 제공해 연산을 다양한 방식으로 연습할 수 있습니다.

주요 기능

- 덧셈, 뺄셈, 곱셈, 나눗셈 연습
- 스테이지별 10문제 구성
- 오늘의 퀘스트와 별 보상
- 별보물함과 친구 꾸미기
- 곰돌이, 토끼, 여우, 부엉이 모험 친구 선택
- 학습 기록과 주간 진행 확인
- 한국어, 영어, 중국어, 일본어 지원

부모가 안심할 수 있는 구성

- 광고 없음
- 인앱 결제 없음
- 계정 연동은 선택 사항 (부모 인증 후 설정 가능)
- 채팅과 사용자 생성 콘텐츠 없음
- 게스트 모드에서는 학습 기록이 기기 안에만 저장
- 계정 연동 시 기기 간 학습 데이터 백업 가능

셈토끼는 매일 짧게 풀고, 조금씩 성취를 쌓아가는 첫 사칙연산 경험을 목표로 합니다.
```

## 영어 로컬라이제이션 초안

출시 1차에서 한국어만 운영해도 되지만, 앱이 영어 UI를 지원하므로 App Store/Play Console에 영어 메타데이터도 추가하면 자동 번역보다 안정적이다.

### App Name

```text
Semtokki
```

### Subtitle

```text
Arithmetic with adventure friends
```

### Short Description

```text
Practice arithmetic through stages, stars, treasures, and Semtokki's guidance
```

### Full Description

```text
Semtokki is a child-friendly arithmetic practice app where kids solve addition, subtraction, multiplication, and division problems with Semtokki and a group of adventure friends.

Kids move through stages, collect stars and coins, unlock treasures, and build a gentle daily learning habit. Semtokki guides the journey, while kids can choose their own adventure friend to study with. Parents can check simple learning progress without accounts, ads, or in-app purchases.

Features

- Addition, subtraction, multiplication, and division stages
- Multiple-choice and keypad-answer questions
- Daily quests and star rewards
- Treasure collection and character customization
- Bear, bunny, fox, and owl adventure friends
- Simple learning progress view
- Korean, English, Chinese, and Japanese UI support

Designed for a safer learning experience

- No ads
- No in-app purchases
- Optional account linking (set up by parents after parental gate)
- No chat or user-generated content
- Guest mode: learning progress stays on the device
- With account: sync learning data across devices
```

## 앱 내부 문구 변경 기준

수정 대상:

- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/Localization.kt`
- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/OnboardingScreen.kt`
- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/MainScreen.kt`
- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/LearningReportScreen.kt`
- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/TreasureChestScreen.kt`
- `android/shared/src/commonMain/kotlin/com/mathapp/practice/ui/ShopScreen.kt`

권장 변경:

| 현재 | 변경 |
|---|---|
| `수학 모험을 시작해요!` | `셈토끼와 연산 모험을 시작해요!` |
| `재미있는 문제를 풀면서 별을 모아보아요!` | `셈토끼의 안내를 따라 문제를 풀고 별을 모아 보아요!` |
| `내 친구를 선택해요` | `오늘 함께할 모험 친구를 골라요` |
| `진행상황` | `탐험 기록` |
| `학습 리포트` | `탐험 기록` |
| `보물 상자` | `별보물함` |
| `캐릭터 꾸미기` | `친구 꾸미기` |
| `상점` | `꾸미기 상점` |
| `코인` | `별코인` |

완료 기준:

- 앱 첫 실행부터 브랜드명이 보인다.
- 셈토끼는 안내자, 선택 캐릭터는 모험 친구로 보인다.
- 스토어 설명과 앱 내부 용어가 충돌하지 않는다.
- Play 짧은 설명에 이모지/특수 장식이 없다.
- 광고 없음/인앱 결제 없음 설명이 실제 앱과 일치한다.
- 계정 연동은 "선택 사항, 부모 인증 후 설정 가능"으로 일관되게 표현한다.

## 정책 출처

- Apple App information: https://developer.apple.com/help/app-store-connect/reference/app-information/app-information
- Apple platform version information: https://developer.apple.com/help/app-store-connect/reference/app-information/platform-version-information
- Google Play store listing setup: https://support.google.com/googleplay/android-developer/answer/9859152
- Google Play metadata policy: https://support.google.com/googleplay/android-developer/answer/9898842
