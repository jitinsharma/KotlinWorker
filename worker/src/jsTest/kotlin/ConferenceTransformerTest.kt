import com.jitinsharma.worker.models.CallForPapersResponse
import com.jitinsharma.worker.models.ConferenceResponse
import mapper.ConferenceTransformer
import kotlin.js.Date
import kotlin.test.*

class ConferenceTransformerTest {

    @Test
    fun testSplitLocationWithSinglePart() {
        // Test private function via reflection or create a test helper
        val transformer = ConferenceTransformer
        
        // Test single location (city only)
        val conference = createSampleConference(location = "Berlin")
        val result = transformer.transformConferences(listOf(conference))
        
        assertEquals("Berlin", result[0].city)
        assertEquals("", result[0].country)
    }

    @Test
    fun testSplitLocationWithTwoParts() {
        val conference = createSampleConference(location = "New York, USA")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertEquals("New York", result[0].city)
        assertEquals("USA", result[0].country)
    }

    @Test
    fun testSplitLocationWithMultipleParts() {
        val conference = createSampleConference(location = "San Francisco, California, USA")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertEquals("San Francisco, California", result[0].city)
        assertEquals("USA", result[0].country)
    }

    @Test
    fun testSplitLocationWithExtraWhitespace() {
        val conference = createSampleConference(location = " London , United Kingdom ")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertEquals("London", result[0].city)
        assertEquals("United Kingdom", result[0].country)
    }

    @Test
    fun testSplitLocationWithFourParts() {
        val conference = createSampleConference(location = "Mountain View, Santa Clara County, California, USA")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertEquals("Mountain View, Santa Clara County, California", result[0].city)
        assertEquals("USA", result[0].country)
    }

    @Test
    fun testSplitLocationEmptyString() {
        val conference = createSampleConference(location = "")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertEquals("", result[0].city)
        assertEquals("", result[0].country)
    }

    @Test
    fun testDateStringToEpochConversion() {
        val conference = createSampleConference(
            dateStart = "2024-12-13",
            dateEnd = "2024-12-14"
        )
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        // Expected epoch for 2024-12-13 (in seconds)
        val expectedStart = Date.parse("2024-12-13") / 1000
        val expectedEnd = Date.parse("2024-12-14") / 1000
        
        assertEquals(expectedStart.toLong(), result[0].dateStartEpoch)
        assertEquals(expectedEnd.toLong(), result[0].dateEndEpoch)
    }

    @Test
    fun testDateConversionWithDifferentFormats() {
        val conference = createSampleConference(
            dateStart = "2024-01-01", // Start of year
            dateEnd = "2024-12-31"    // End of year  
        )
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertTrue(result[0].dateStartEpoch > 0, "Start date should be positive epoch")
        assertTrue(result[0].dateEndEpoch > result[0].dateStartEpoch, "End date should be after start date")
    }

    @Test
    fun testCountryFlagUrlGeneration() {
        val conference = createSampleConference(location = "Berlin, Germany")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertEquals("https://flagcdn.com/w80/de.png", result[0].countryFlagUrl)
    }

    @Test
    fun testCountryFlagUrlWithUnknownCountry() {
        val conference = createSampleConference(location = "Atlantis, Unknown Country")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        // Should fallback to UN flag
        assertEquals("https://flagcdn.com/w80/un.png", result[0].countryFlagUrl)
    }

    @Test
    fun testCountryFlagUrlWithEmptyCountry() {
        val conference = createSampleConference(location = "Atlantis")
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        // Should fallback to UN flag for empty country
        assertEquals("https://flagcdn.com/w80/un.png", result[0].countryFlagUrl)
    }

    @Test
    fun testCfpTransformation() {
        val cfp = CallForPapersResponse(
            start = "2024-08-01",
            end = "2024-10-15",
            site = "https://example.com/cfp"
        )
        val conference = createSampleConference(cfp = cfp)
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertNotNull(result[0].cfp)
        assertEquals("https://example.com/cfp", result[0].cfp?.site)
        
        val expectedCfpStart = (Date.parse("2024-08-01") / 1000).toLong()
        val expectedCfpEnd = (Date.parse("2024-10-15") / 1000).toLong()
        
        assertEquals(expectedCfpStart, result[0].cfp?.startEpoch)
        assertEquals(expectedCfpEnd, result[0].cfp?.endEpoch)
    }

    @Test
    fun testCfpTransformationWithNullDates() {
        val cfp = CallForPapersResponse(
            start = null,
            end = null,
            site = "https://example.com/cfp"
        )
        val conference = createSampleConference(cfp = cfp)
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertNotNull(result[0].cfp)
        assertEquals("https://example.com/cfp", result[0].cfp?.site)
        assertNull(result[0].cfp?.startEpoch)
        assertNull(result[0].cfp?.endEpoch)
    }

    @Test
    fun testCfpTransformationWithNullCfp() {
        val conference = createSampleConference(cfp = null)
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        assertNull(result[0].cfp)
    }

    @Test
    fun testCompleteConferenceTransformation() {
        val cfp = CallForPapersResponse(
            start = "2024-08-01",
            end = "2024-10-15", 
            site = "https://droidcon.in/cfp"
        )
        
        val conference = ConferenceResponse(
            name = "Droidcon India",
            website = "https://droidcon.in",
            location = "Bengaluru, India",
            dateStart = "2024-12-13",
            dateEnd = "2024-12-14",
            cfp = cfp
        )
        
        val result = ConferenceTransformer.transformConferences(listOf(conference))
        
        result[0].apply {
            assertEquals("Droidcon India", name)
            assertEquals("https://droidcon.in", website)
            assertEquals("Bengaluru", city)
            assertEquals("India", country)
            assertEquals("https://flagcdn.com/w80/in.png", countryFlagUrl)
            
            assertTrue(dateStartEpoch > 0)
            assertTrue(dateEndEpoch > dateStartEpoch)
            
            assertNotNull(this.cfp)
            assertEquals("https://droidcon.in/cfp", this.cfp?.site)
            assertNotNull(this.cfp?.startEpoch)
            assertNotNull(this.cfp?.endEpoch)
        }
    }

    @Test
    fun testMultipleConferencesTransformation() {
        val conferences = listOf(
            createSampleConference(
                name = "Conference 1",
                location = "Berlin, Germany"
            ),
            createSampleConference(
                name = "Conference 2", 
                location = "Tokyo, Japan"
            ),
            createSampleConference(
                name = "Conference 3",
                location = "New York, USA"
            )
        )
        
        val result = ConferenceTransformer.transformConferences(conferences)
        
        assertEquals(3, result.size)
        assertEquals("Conference 1", result[0].name)
        assertEquals("Germany", result[0].country)
        assertEquals("https://flagcdn.com/w80/de.png", result[0].countryFlagUrl)
        
        assertEquals("Conference 2", result[1].name)
        assertEquals("Japan", result[1].country)
        assertEquals("https://flagcdn.com/w80/jp.png", result[1].countryFlagUrl)
        
        assertEquals("Conference 3", result[2].name)
        assertEquals("USA", result[2].country)
        assertEquals("https://flagcdn.com/w80/us.png", result[2].countryFlagUrl)
    }

    // Helper function to create sample conference data
    private fun createSampleConference(
        name: String = "Test Conference",
        website: String = "https://test.com",
        location: String = "Test City, Test Country",
        dateStart: String = "2024-06-01",
        dateEnd: String = "2024-06-02",
        cfp: CallForPapersResponse? = null
    ) = ConferenceResponse(
        name = name,
        website = website,
        location = location,
        dateStart = dateStart,
        dateEnd = dateEnd,
        cfp = cfp
    )
}