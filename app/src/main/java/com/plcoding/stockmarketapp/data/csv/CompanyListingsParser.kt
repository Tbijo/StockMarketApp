package com.plcoding.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

// This is a implementation for CompanyListings every other parser should have own impl.

@Singleton // for Hilt to know how to construct this and provide for other dependencies
class CompanyListingsParser @Inject constructor(): CSVParser<CompanyListing> {

    // Take the stream and parse it to List<CompanyListing>
    override suspend fun parse(stream: InputStream): List<CompanyListing> {
        // get access OpenCSV Reader
        // we will get a table from which we get data
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1) // drop first row cause it is names of columns
                .mapNotNull { line ->
                    // get every part of data from the line
                    val symbol = line.getOrNull(0)
                    val name = line.getOrNull(1)
                    val exchange = line.getOrNull(2)
                    // create CompanyListing Object
                    CompanyListing(
                        name = name ?: return@mapNotNull null, // ignore null
                        symbol = symbol ?: return@mapNotNull null,
                        exchange = exchange ?: return@mapNotNull null
                    )
                }
                .also {
                    csvReader.close()
                }
        }
    }
}