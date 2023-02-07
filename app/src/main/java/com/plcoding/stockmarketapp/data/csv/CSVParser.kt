package com.plcoding.stockmarketapp.data.csv

import java.io.InputStream

// For abstraction
// Every type of parser should have own implementation
interface CSVParser<T> {
    suspend fun parse(stream: InputStream): List<T>
}