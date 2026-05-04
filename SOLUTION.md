# Design Rationale and Key Decisions

## Architecture Overview

The app follows Clean Architecture principles with clear separation of concerns:

- **Data Layer**: Room Database + Firebase REST API + DataStore  
- **Domain Layer**: Use cases and models  
- **Presentation Layer**: MVVM with Jetpack Compose  

---

## Key Design Decisions

### 1. Offline-First Approach

**Decision:**  
Cache data in Room database and display cached data immediately while fetching fresh data in background.

**Why:**  
Users expect to see their shipments instantly, even without connectivity. The app shows cached data first (if available) then updates when network is available.

**Implementation:**  
`ShipmentRepositoryImpl.getShipmentsStream()` emits cached data first, then fetches from network and updates cache.

---

### 2. Resource Wrapper for State Management

**Decision:**  
Custom `Resource` sealed class to represent Loading, Success, and Error states with offline awareness.

**Why:**  
Provides type-safe state handling across ViewModels and Composables, making UI state management predictable.

---

### 3. DataStore for Preferences

**Decision:**  
Use DataStore instead of SharedPreferences for theme, favorites filter, and recent searches.

**Why:**  
Type-safe, coroutine-friendly, and provides Flow APIs for reactive preferences.

---

### 4. Push Update Simulation

**Decision:**  
Firebase Realtime Database listener for real-time updates simulation.

**Why:**  
Demonstrates scalability — the architecture can easily adapt to real push notifications by swapping the update source.

---

## Trade-offs

| Trade-off                | Decision             | Rationale                                                      |
|-------------------------|----------------------|----------------------------------------------------------------|
| Single source of truth   | Room + Remote        | Simpler than full offline sync, sufficient for MVP             |
| Timeline deduplication  | Done in ViewModel    | Keeps Repository pure, easy to test                            |
| Carrier colors          | Hardcoded mapping    | Extensible via config file later                               |
| Network calls           | One per screen       | Could batch requests, but fine for scale                       |

---

## Scalability Considerations

### More Carriers

- Carrier colors in `Helper.kt` can be externalized to a JSON config  
- Carrier-specific parsing logic can be delegated to strategy pattern  

---

### Push Notifications (Real)

Current push update system uses Firebase Realtime Database listener.  
 