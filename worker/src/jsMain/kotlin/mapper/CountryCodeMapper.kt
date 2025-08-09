package mapper

object CountryCodeMapper {
    private val countryMappings = mapOf(
        // Major countries
        "united states" to "us",
        "india" to "in",
        "germany" to "de",
        "japan" to "jp",
        "united kingdom" to "gb",
        "canada" to "ca",
        "australia" to "au",
        "france" to "fr",
        "italy" to "it",
        "spain" to "es",
        "brazil" to "br",
        "china" to "cn",
        "south korea" to "kr",
        "netherlands" to "nl",
        "singapore" to "sg",
        "denmark" to "dk",
        "sweden" to "se",
        "norway" to "no",
        "poland" to "pl",
        "finland" to "fi",
        "austria" to "at",
        "belgium" to "be",
        "switzerland" to "ch",
        "portugal" to "pt",
        "ireland" to "ie",
        "israel" to "il",
        "turkey" to "tr",
        "russia" to "ru",
        "ukraine" to "ua",
        "romania" to "ro",
        "greece" to "gr",
        "hungary" to "hu",
        "czech republic" to "cz",
        "slovakia" to "sk",
        "croatia" to "hr",
        "serbia" to "rs",
        "bulgaria" to "bg",
        "lithuania" to "lt",
        "latvia" to "lv",
        "estonia" to "ee",
        "slovenia" to "si",
        "mexico" to "mx",
        "argentina" to "ar",
        "chile" to "cl",
        "colombia" to "co",
        "peru" to "pe",
        "venezuela" to "ve",
        "south africa" to "za",
        "egypt" to "eg",
        "kenya" to "ke",
        "nigeria" to "ng",
        "morocco" to "ma",
        "thailand" to "th",
        "vietnam" to "vn",
        "indonesia" to "id",
        "malaysia" to "my",
        "philippines" to "ph",
        "taiwan" to "tw",
        "hong kong" to "hk",
        "new zealand" to "nz",

        // Common variations and abbreviations
        "usa" to "us",
        "america" to "us",
        "uk" to "gb",
        "britain" to "gb",
        "england" to "gb",
        "scotland" to "gb",
        "wales" to "gb",
        "northern ireland" to "gb",
        "uae" to "ae",
        "united arab emirates" to "ae",
        "korea" to "kr",
        "south korea" to "kr",
        "republic of korea" to "kr",
        "czech" to "cz",
        "czechia" to "cz",
        "holland" to "nl",
        "bosnia" to "ba",
        "bosnia and herzegovina" to "ba",
        "macedonia" to "mk",
        "north macedonia" to "mk",
        "jordan" to "jo",

        // Tech hub cities that might appear as countries
        "silicon valley" to "us",
        "bay area" to "us"
    )

    fun getCountryCode(countryName: String): String? {
        val normalized = countryName.lowercase().trim()
        
        // Return null for empty or very short inputs
        if (normalized.isEmpty() || normalized.length < 2) {
            return null
        }

        // Direct match
        countryMappings[normalized]?.let { return it }

        // Fuzzy matching for partial matches
        return countryMappings.entries.find { (key, _) ->
            // Check if the country name contains our key or vice versa
            key.contains(normalized) || normalized.contains(key) ||
                    // Check for word boundaries (e.g., "korea" matches "south korea")
                    normalized.split(" ").any { word -> key.contains(word) && word.length > 2 } ||
                    key.split(" ").any { word -> normalized.contains(word) && word.length > 2 }
        }?.value
    }

    fun getFlagUrl(countryCode: String, size: Int = 80): String {
        return "https://flagcdn.com/w$size/${countryCode.lowercase()}.png"
    }

    fun getDefaultFlagUrl(size: Int = 80): String {
        return "https://flagcdn.com/w$size/un.png" // UN flag as fallback
    }
}