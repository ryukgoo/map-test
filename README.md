# 📍 Current Location Map (Pre-Interview Assignment)

이 프로젝트는 **Jetpack Compose**와 **Google Maps Compose**를 활용하여  
비동기적으로 **현재 위치를 지도에 표시하는 안드로이드 앱**입니다.

---

## 🎯 주요 목표

> **지도뷰에서 비동기적으로 현 위치를 표시합니다.**

### 요구사항
1. `'현 위치'` 버튼 클릭 시 WorkManager로 현재 위치를 조회한다.
2. 조회된 위치 정보를 Room 로컬 DB에 저장한다.
3. Room DB에 저장된 위치 정보를 가져와 지도뷰에 마커로 표시한다.

---

## 🧩 기술 스택

| 영역 | 사용 기술 |
|------|----------|
| UI | **Jetpack Compose**, Material3, Google Maps Compose |
| 비동기 처리 | **WorkManager**, Kotlin Coroutines |
| 데이터 저장 | **Room** |
| 의존성 주입 | **Hilt** |
| 아키텍처 | **MVVM + Repository Pattern** |
| 언어 / 환경 | Kotlin 2.0.21, Android SDK 34 |

---

## ⚙️ 아키텍처 개요
- **UI (`MapScreen`)**
    - GoogleMap을 표시하고, ViewModel 상태를 구독하여 Marker 업데이트.
    - “현 위치” 버튼 클릭 시 `viewModel.updateLocation()` 호출.

- **ViewModel (`MapViewModel`)**
    - Hilt로 주입된 `LocationRepository`를 통해 Room 데이터 Flow 관찰.
    - LocationRepository 이용한 위치 갱신 요청을 수행.

- **Repository (`LocationRepository`)**
    - WorkManager 호출 및 DB 접근 관리.
    - 위치 데이터의 단방향 데이터 흐름 유지.

- **Worker (`LocationWorker`)**
    - 실제 위치 조회(또는 테스트용 좌표 생성)를 수행하고 DB에 저장.
    - HiltWorkerFactory를 통해 Hilt로 주입된 DAO 사용.

---

## 📦 주요 구성 파일

| 파일 | 역할 |
|------|------|
| `MainActivity.kt` | Compose 기반 Entry Activity (`@AndroidEntryPoint`) |
| `MapScreen.kt` | 지도 표시 및 현위치 버튼 UI |
| `MapViewModel.kt` | WorkManager 호출, 위치 데이터 관리 |
| `LocationRepository.kt` | Room 및 Worker 연결 |
| `LocationWorker.kt` | 백그라운드 위치 갱신 처리 |
| `AppDatabase.kt` / `LocationDao.kt` | Room DB 구성 |
| `MapTestApp.kt` | `@HiltAndroidApp`, `Configuration.Provider` 구현 |
| `AndroidManifest.xml` | `androidx.startup` 비활성화 및 Hilt 기반 초기화 설정 |

---

## 🛰️ 백그라운드 처리 (핵심 구현 포인트)

- `LocationWorker`는 `WorkManager`를 통해 비동기적으로 실행됩니다.
- `@HiltWorker` + `@AssistedInject` 구조를 사용하여 Hilt로 DAO를 주입받습니다.
- 위치는 테스트 시 랜덤 좌표(서울 지역)를 사용하며, 실제 기기에서는 `FusedLocationProviderClient`로 변경 가능.

```kotlin
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: LocationDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val randomLat = Random.nextDouble(37.4, 37.7)
        val randomLng = Random.nextDouble(126.8, 127.2)
        dao.insert(LocationEntity(latitude = randomLat, longitude = randomLng))
        return Result.success()
    }
}