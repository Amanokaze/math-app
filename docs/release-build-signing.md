# 배포 빌드 및 서명 실행 문서

이 문서는 Android AAB와 iOS Archive를 만들기 위한 코드 작업 기준이다. 스토어 콘솔 입력은 [ios-app-store-release.md](./ios-app-store-release.md), [google-play-release.md](./google-play-release.md)를 따른다.

## 현재 상태

Android:

- `android/app/build.gradle.kts`
- `applicationId = "com.mathapp.practice"`
- `compileSdk = 34`
- `targetSdk = 34`
- `versionCode = 1`
- `versionName = "1.0"`
- release minify 꺼짐
- release signing 설정 없음

iOS:

- `ios/Math.xcodeproj/project.pbxproj`
- `PRODUCT_BUNDLE_IDENTIFIER = com.mathapp.practice`
- `MARKETING_VERSION = 1.0`
- `CURRENT_PROJECT_VERSION = 1`
- `IPHONEOS_DEPLOYMENT_TARGET = 17.0`
- `CFBundleDisplayName = Math`
- `PrivacyInfo.xcprivacy` 없음

## 출시 전 식별자 결정

스토어 레코드 생성 전에 아래 값을 확정한다.

| 항목 | 현재 값 | 출시 권장 |
|---|---|---|
| Android applicationId | `com.mathapp.practice` | `<owned.reverse.domain>.semtokki` |
| Android namespace | `com.mathapp.practice` | package 변경 시 함께 정리 |
| iOS bundle identifier | `com.mathapp.practice` | `<owned.reverse.domain>.semtokki` |
| 앱 표시 이름 | `Math` | `셈토끼` |

`owned.reverse.domain`은 실제 소유한 도메인이나 개발자/조직 식별자를 사용한다. 확정된 식별자가 없다면 임시로 스토어 레코드를 만들지 않는다.

## Android 작업

### 수정 대상 파일

- `android/app/build.gradle.kts`
- `android/shared/build.gradle.kts`
- `android/app/src/main/AndroidManifest.xml`
- 필요 시 Kotlin package path 전체
- `android/app/proguard-rules.pro`
- 새 파일: `android/keystore.properties.example`
- 새 파일: `.gitignore` 항목 추가가 필요하면 기존 파일 확인 후 추가

### 작업 순서

1. 최종 `applicationId`를 확정한다.
2. Google Play 요구사항에 맞춰 `targetSdk`를 현재 기준 이상으로 올린다. 2026-04-25 기준 신규 앱/업데이트는 Android 15, API 35 이상이 필요하다.
3. `compileSdk`도 같은 API 수준으로 올린다.
4. `android:label`을 `셈토끼`로 바꾸거나 `@string/app_name`으로 분리한다.
5. release signing config를 추가하되 keystore 파일과 비밀번호는 커밋하지 않는다.
6. `versionCode` 증가 규칙을 문서화한다.
7. release AAB를 생성한다.

### Gradle signing 예시

실제 비밀 값은 커밋하지 않는다.

```properties
# android/keystore.properties.example
storeFile=/absolute/path/to/upload-keystore.jks
storePassword=CHANGE_ME
keyAlias=upload
keyPassword=CHANGE_ME
```

`android/app/build.gradle.kts` 구현 방향:

```kotlin
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "FINAL_APPLICATION_ID"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
}
```

`isMinifyEnabled`는 1차 출시에서는 `false`를 유지해 리스크를 낮춘다. QA가 충분하면 이후 크기 최적화를 위해 켤 수 있다.

### Android 검증 명령

```bash
cd android
./gradlew :app:clean :app:assembleDebug
./gradlew :app:bundleRelease
```

생성물:

```text
android/app/build/outputs/bundle/release/app-release.aab
```

추가 확인:

```bash
cd android
./gradlew :app:processReleaseMainManifest
```

완료 기준:

- release AAB가 생성된다.
- `targetSdk`가 현재 Play 요구사항 이상이다.
- `AD_ID`, `INTERNET`, 위치, 카메라, 마이크 권한이 추가되지 않았다.
- Android 런처 표시 이름이 `셈토끼`이다.
- keystore와 `keystore.properties`는 Git에 포함되지 않는다.

## iOS 작업

### 수정 대상 파일

- `ios/Math/Info.plist`
- `ios/Math.xcodeproj/project.pbxproj`
- 새 파일: `ios/Math/PrivacyInfo.xcprivacy`
- 필요 시 `ios/Math/Assets.xcassets/AppIcon.appiconset/AppIcon.png`

### 작업 순서

1. 최종 Bundle ID를 확정한다.
2. `CFBundleDisplayName`을 `셈토끼`로 변경한다.
3. `MARKETING_VERSION`을 `1.0.0`으로 맞춘다.
4. `CURRENT_PROJECT_VERSION`은 첫 출시 `1`로 유지하고 업데이트마다 1씩 증가한다.
5. `PrivacyInfo.xcprivacy`를 추가하고 앱 타깃 리소스로 포함한다.
6. AppIcon을 최종 1024x1024 PNG로 교체한다.
7. Xcode에서 Signing & Capabilities가 실제 Apple Developer Team과 맞는지 확인한다.
8. Archive를 생성한다.

### Privacy Manifest

`NSUserDefaults` 사용 때문에 `NSPrivacyAccessedAPICategoryUserDefaults`와 `CA92.1`을 선언한다. 자세한 내용은 [privacy-and-kids-compliance.md](./privacy-and-kids-compliance.md)를 따른다.

검증:

```bash
plutil -lint ios/Math/PrivacyInfo.xcprivacy
```

### iOS 빌드 명령

로컬 Xcode 환경에 따라 scheme 이름을 확인한 뒤 실행한다.

```bash
xcodebuild -project ios/Math.xcodeproj -scheme Math -configuration Release -sdk iphonesimulator build
```

Archive는 Xcode Organizer에서 만드는 것을 기본으로 한다. CLI가 필요하면 팀/서명 설정을 먼저 확인한 뒤 사용한다.

완료 기준:

- Release 빌드가 성공한다.
- 앱 표시 이름이 `셈토끼`이다.
- `PrivacyInfo.xcprivacy`가 archive에 포함된다.
- Bundle ID가 최종 식별자다.
- AppIcon이 검은 배경 `M` 아이콘이 아니다.

## 버전 규칙

첫 출시:

- Android `versionName = "1.0.0"`
- Android `versionCode = 1`
- iOS `MARKETING_VERSION = 1.0.0`
- iOS `CURRENT_PROJECT_VERSION = 1`

업데이트:

- 기능 업데이트: `1.1.0`, `versionCode + 1`, `CURRENT_PROJECT_VERSION + 1`
- 버그 수정: `1.0.1`, `versionCode + 1`, `CURRENT_PROJECT_VERSION + 1`
- 스토어에 업로드한 빌드 번호는 재사용하지 않는다.

## 금지 사항

- keystore, 인증서, provisioning profile, 비밀번호를 Git에 커밋하지 않는다.
- 스토어 첫 업로드 이후 package name 또는 bundle ID 변경을 전제로 작업하지 않는다.
- targetSdk 요구사항을 낮추지 않는다.
- 출시 빌드에 디버그 이름, 임시 아이콘, 테스트 문구를 남기지 않는다.

## 정책 출처

- Google Play target API requirement: https://developer.android.com/google/play/requirements/target-sdk
- Google Play app setup and versioning: https://support.google.com/googleplay/android-developer/answer/9859152
- Apple App Store Connect app information: https://developer.apple.com/help/app-store-connect/reference/app-information/app-information
- Apple required reason API: https://developer.apple.com/documentation/bundleresources/describing-use-of-required-reason-api
