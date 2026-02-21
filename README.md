# Math - 산수 연습 앱

덧셈과 뺄셈 문제를 풀며 시간을 측정하는 산수 연습 앱입니다. iOS와 Android를 지원합니다.

## 프로젝트 구조 (Monorepo)

```
math-app/
├── ios/                # iOS 프로젝트
│   ├── Math.xcodeproj/
│   └── Math/
├── android/            # Android 프로젝트
│   ├── app/
│   └── ...
└── README.md
```

## iOS

### 기능
- 메인 화면: 시작 버튼, 최고 기록, 다크/라이트 모드, 언어 설정
- 3-2-1 카운트다운 후 게임 시작
- 10문제 (덧셈/뺄셈), 타이머, 커스텀 숫자패드
- 기록 경신 시 축하 멘트 + 꽃가루 효과
- 다국어: 한국어, English, 中文(繁體), 中文(简体), 日本語

### 실행
1. Xcode에서 `ios/Math.xcodeproj` 열기
2. 시뮬레이터 또는 실제 기기 선택
3. ⌘R로 빌드 및 실행

### 요구사항
- Xcode 15.0 이상
- iOS 17.0 이상

## Android

### 기능
- iOS와 동일한 기능 (메인, 게임, 설정, 다국어, 다크모드)

### 실행
1. **Android Studio**에서 `android/` 폴더 열기
2. 에뮬레이터 또는 실제 기기 연결
3. Run 버튼으로 빌드 및 실행

또는 커맨드라인:
```bash
cd android
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 요구사항
- Android Studio 또는 JDK 17+
- Android SDK 34
- minSdk 26
