package mapper

import com.jitinsharma.worker.models.*
import kotlin.js.Date

object ConferenceTransformer {
    
    fun transformConferences(conferenceResponses: List<ConferenceResponse>): List<ConferenceModel> {
        return conferenceResponses.map { conference ->
            val (city, country) = splitLocation(conference.location)
            val flagUrl = getCountryFlagUrl(country)
            
            ConferenceModel(
                name = conference.name,
                website = conference.website,
                city = city,
                country = country,
                countryFlagUrl = flagUrl,
                dateStartEpoch = dateStringToEpoch(conference.dateStart),
                dateEndEpoch = dateStringToEpoch(conference.dateEnd),
                cfp = conference.cfp?.let { cfp ->
                    CallForPapersModel(
                        startEpoch = cfp.start?.let { dateStringToEpoch(it) },
                        endEpoch = cfp.end?.let { dateStringToEpoch(it) },
                        site = cfp.site
                    )
                }
            )
        }
    }
    
    private fun splitLocation(location: String): Pair<String, String> {
        val parts = location.split(",").map { it.trim() }
        
        return when (parts.size) {
            1 -> {
                // Only one part, treat as city with empty country
                parts[0] to ""
            }
            2 -> {
                // Two parts: city, country
                parts[0] to parts[1]
            }
            else -> {
                // More than 2 parts: last is country, rest join as city
                val country = parts.last()
                val city = parts.dropLast(1).joinToString(", ")
                city to country
            }
        }
    }
    
    private fun getCountryFlagUrl(country: String): String {
        // Skip empty countries
        if (country.isBlank()) {
            return CountryCodeMapper.getDefaultFlagUrl()
        }
        
        // Try static mapping first (fast lookup)
        CountryCodeMapper.getCountryCode(country)?.let { code ->
            return CountryCodeMapper.getFlagUrl(code)
        }
        
        // If no match found, return default flag
        console.log("No country code found for: '$country', using default UN flag")
        return CountryCodeMapper.getDefaultFlagUrl()
    }

    private fun dateStringToEpoch(dateString: String): Long {
        // Convert YYYY-MM-DD to epoch time (seconds) using JavaScript Date
        val epochMillis: Double = Date.parse(dateString)
        return (epochMillis / 1000).toLong()
    }
}