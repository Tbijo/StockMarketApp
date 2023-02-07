package com.plcoding.stockmarketapp.presentation.company_listings

import com.plcoding.stockmarketapp.domain.model.CompanyListing

data class CompanyListingsState(
    val companies: List<CompanyListing> = emptyList(),
    val isLoading: Boolean = false, // show progress bar or not
    val isRefreshing: Boolean = false, // if true then swipe refresh layout is refreshing
    val searchQuery: String = ""
)
