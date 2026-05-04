package com.eisen.trackernow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eisen.trackernow.data.local.FavoritesManager
import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.domain.repository.NetworkMonitor
import com.eisen.trackernow.domain.repository.NetworkStatus
import com.eisen.trackernow.domain.repository.ShipmentRepository
import com.eisen.trackernow.domain.usecase.FilterShipmentsUseCase
import com.eisen.trackernow.domain.usecase.GetShipmentsUseCase
import com.eisen.trackernow.domain.usecase.RefreshShipmentsUseCase
import com.eisen.trackernow.domain.usecase.SearchShipmentsUseCase
import com.eisen.trackernow.presentation.util.DataStoreManager
import com.eisen.trackernow.presentation.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

private val NOT_DELIVERED_STATUSES = setOf(
    "IN_TRANSIT",
    "OUT_FOR_DELIVERY",
    "LABEL_CREATED",
    "PENDING",
    "EXCEPTION"
)

@HiltViewModel
class ShipmentListViewModel @Inject constructor(
    private val getShipmentsUseCase: GetShipmentsUseCase,
    private val refreshShipmentsUseCase: RefreshShipmentsUseCase,
    private val repository: ShipmentRepository,
    private val searchShipmentsUseCase: SearchShipmentsUseCase,
    private val filterShipmentsUseCase: FilterShipmentsUseCase,
    private val favoritesManager: FavoritesManager,
    private val dataStoreManager: DataStoreManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _shipments = MutableStateFlow<Resource<List<Shipment>>>(Resource.Loading)
    val shipments: StateFlow<Resource<List<Shipment>>> = _shipments.asStateFlow()

    private val _originalShipments = MutableStateFlow<List<Shipment>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _hasNewUpdates = MutableStateFlow(false)
    val hasNewUpdates: StateFlow<Boolean> = _hasNewUpdates.asStateFlow()

    private val _lastUpdateMessage = MutableStateFlow<String?>(null)
    val lastUpdateMessage: StateFlow<String?> = _lastUpdateMessage.asStateFlow()

    private val _selectedFilter = MutableStateFlow<StatusFilter?>(null)
    val selectedFilter: StateFlow<StatusFilter?> = _selectedFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isDefaultFilterActive = MutableStateFlow(true)
    val isDefaultFilterActive: StateFlow<Boolean> = _isDefaultFilterActive.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites = _showOnlyFavorites.asStateFlow()


    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val _hasCachedData = MutableStateFlow(false)
    val hasCachedData: StateFlow<Boolean> = _hasCachedData.asStateFlow()

    private var searchJob: Job? = null
    private var pushUpdateJob: Job? = null

    init {
        loadUserId()
        loadUserPreferences()
        loadFavorites()
        loadShipments()
        setupSearchAndFilter()
        startListeningForPushUpdates()
        checkForPendingUpdates()
        monitorNetworkStatus()
    }

    private fun monitorNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.observeNetworkStatus().collect { status ->
                val wasAvailable = _isNetworkAvailable.value
                val isAvailable = status == NetworkStatus.Available || status == NetworkStatus.Metered || status == NetworkStatus.Unmetered
                _isNetworkAvailable.value = isAvailable

                if (!wasAvailable && isAvailable && _hasCachedData.value) {
                    refresh()
                }
            }
        }
    }


    private fun loadUserId() {
        viewModelScope.launch {
            dataStoreManager.observeUserId().collect { id ->
                _userId.value = id
            }
            val userId = dataStoreManager.getUserId()
            _userId.value = userId
        }
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            dataStoreManager.getRecentSearches().collect { searches ->
                _recentSearches.value = searches
            }
        }

        viewModelScope.launch {
            dataStoreManager.getShowOnlyFavorites().collect { showFavorites ->
                _showOnlyFavorites.value = showFavorites
            }
        }

        viewModelScope.launch {
            dataStoreManager.getLastRefreshTime().collect { lastRefresh ->
                val lastUpdate = repository.getLastUpdateTimestamp()
                _hasNewUpdates.value = lastUpdate > lastRefresh
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesManager.getFavoriteIds().collect { ids ->
                _favoriteIds.value = ids
                _originalShipments.value = _originalShipments.value.map { shipment ->
                    shipment.copy(isFavorite = ids.contains(shipment.id))
                }
                applyFilters()
            }
        }
    }

    private fun loadShipments() {
        viewModelScope.launch {
            getShipmentsUseCase().collect { resource ->
                if (resource is Resource.Success) {
                    val data = resource.data ?: emptyList()
                    _hasCachedData.value = data.isNotEmpty()
                    _originalShipments.value = data
                    applyFilters()
                } else if (resource is Resource.Error) {
                    if (!_hasCachedData.value && !_isNetworkAvailable.value) {
                        _shipments.value = Resource.Error(
                            "No internet connection and no cached data available",
                            offline = true
                        )
                        return@collect
                    }
                }

                _shipments.value = resource
            }
        }
    }

    fun shouldShowOfflineNoData(): Boolean {
        val isOffline = !_isNetworkAvailable.value
        val hasData = _hasCachedData.value
        val isLoading = _shipments.value is Resource.Loading
        val isError = _shipments.value is Resource.Error && (_shipments.value as Resource.Error).isOffline

        return (isOffline && !hasData && !isLoading) || (isError && !hasData)
    }

    fun isOfflineWithCachedData(): Boolean {
        return !_isNetworkAvailable.value && _hasCachedData.value
    }

    private fun setupSearchAndFilter() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedFilter,
                _showOnlyFavorites
            ) { query, filter, onlyFavorites ->
                Triple(query, filter, onlyFavorites)
            }.collect { (query, filter, onlyFavorites) ->
                _isDefaultFilterActive.value = (filter == null && query.isEmpty() && !onlyFavorites)
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        viewModelScope.launch {
            var filtered = _originalShipments.value.toList()

            val effectiveFilter = _selectedFilter.value ?: run {
                if (_searchQuery.value.isEmpty() && !_showOnlyFavorites.value) {
                    filtered = filtered.filter { NOT_DELIVERED_STATUSES.contains(it.lastStatus.code) }
                }
                null
            }

            if (_searchQuery.value.isNotEmpty()) {
                filtered = searchShipmentsUseCase(filtered, _searchQuery.value)
            }

            effectiveFilter?.let { filter ->
                if (filter.code != "ALL") {
                    filtered = filterShipmentsUseCase(filtered, filter.code)
                }
            }
            if (_showOnlyFavorites.value) {
                filtered = filtered.filter { _favoriteIds.value.contains(it.id) }
            }
            _shipments.value = Resource.Success(filtered, (_shipments.value as? Resource.Success)?.isOffline ?: false)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: StatusFilter?) {
        _selectedFilter.value = filter
    }

    fun toggleShowOnlyFavorites() {
        val newValue = !_showOnlyFavorites.value
        _showOnlyFavorites.value = newValue
        viewModelScope.launch {
            dataStoreManager.saveShowOnlyFavorites(newValue)
        }
    }

    fun toggleFavorite(shipmentId: String) {
        viewModelScope.launch {
            val isCurrentlyFavorite = _favoriteIds.value.contains(shipmentId)
            if (isCurrentlyFavorite) {
                favoritesManager.removeFavorite(shipmentId)
            } else {
                favoritesManager.addFavorite(shipmentId)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = refreshShipmentsUseCase()
            if (result.isSuccess) {
                dataStoreManager.saveLastRefreshTime(System.currentTimeMillis())
                loadShipments()
                checkForPendingUpdates()
            } else {
                val currentData = (_shipments.value as? Resource.Success)?.data
                _hasCachedData.value = !currentData.isNullOrEmpty()
            }
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedFilter.value = null
        _showOnlyFavorites.value = false
        _isDefaultFilterActive.value = true
        viewModelScope.launch {
            dataStoreManager.saveShowOnlyFavorites(false)
        }
    }

    fun addRecentSearchIfHasResults(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            val hasResults = when (val state = _shipments.value) {
                is Resource.Success -> {
                    val shipments = state.data ?: emptyList()
                    shipments.any { shipment ->
                        shipment.trackingNumber.contains(query, ignoreCase = true) ||
                                shipment.carrier.name.contains(query, ignoreCase = true)
                    }
                }
                else -> false
            }

            if (hasResults) {
                val current = _recentSearches.value.toMutableList()
                current.remove(query)
                current.add(0, query)
                if (current.size > 10) current.removeAt(current.size - 1)
                _recentSearches.value = current
                dataStoreManager.saveRecentSearches(current)
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            _recentSearches.value = emptyList()
            dataStoreManager.clearRecentSearches()
        }
    }

    fun getFilteredShipments(): List<Shipment> {
        return when (val state = _shipments.value) {
            is Resource.Success -> state.data ?: emptyList()
            else -> emptyList()
        }
    }

    fun checkForPendingUpdates() {
        viewModelScope.launch {
            val lastUpdate = repository.getLastUpdateTimestamp()
            dataStoreManager.getLastRefreshTime().collect { lastRefresh ->
                _hasNewUpdates.value = lastUpdate > lastRefresh
            }
        }
    }

    fun refreshWithUpdates() {
        refresh()
        _hasNewUpdates.value = false
        dismissUpdateMessage()
    }

    fun dismissUpdateMessage() {
        _lastUpdateMessage.value = null
    }

    private fun startListeningForPushUpdates() {
        pushUpdateJob = viewModelScope.launch {
            repository.listenForUpdates().collect { update ->
                repository.saveLastUpdateTimestamp(update.timestamp)
                _hasNewUpdates.value = true
                val message = "📦 ${update.changes.newStatus.label}: ${update.shipmentId} at ${update.changes.location}"
                _lastUpdateMessage.value = message
                delay(5000)
                if (_lastUpdateMessage.value == message) {
                    _lastUpdateMessage.value = null
                }
            }
        }
    }

    enum class StatusFilter(val code: String, val displayName: String) {
        ALL("ALL", "All"),
        IN_TRANSIT("IN_TRANSIT", "In Transit"),
        OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "Out for Delivery"),
        LABEL_CREATED("LABEL_CREATED", "Label Created"),
        DELIVERED("DELIVERED", "Delivered"),
        EXCEPTION("EXCEPTION", "Exception")

    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        pushUpdateJob?.cancel()
    }
}