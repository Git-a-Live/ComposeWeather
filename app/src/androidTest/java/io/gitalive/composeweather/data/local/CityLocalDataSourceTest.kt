package io.gitalive.composeweather.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.gitalive.composeweather.data.local.entity.MyObjectBox
import io.gitalive.composeweather.domain.model.City
import io.objectbox.BoxStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * 用内存库跑真实的 ObjectBox 读写，验证事务内的选中与去重逻辑。
 */
@RunWith(AndroidJUnit4::class)
class CityLocalDataSourceTest {

    private lateinit var store: BoxStore
    private lateinit var dataSource: CityLocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        store = MyObjectBox.builder()
            .androidContext(context)
            .inMemory("test" + System.nanoTime())
            .build()
        dataSource = CityLocalDataSource(store)
    }

    @After
    fun tearDown() {
        store.close()
    }

    @Test
    fun insertOrGetDeduplicatesByCoordinates() = runBlocking {
        val first = dataSource.insertOrGet(hangzhou("杭州"))
        val second = dataSource.insertOrGet(hangzhou("杭州市"))

        assertEquals(first.id, second.id)
        assertEquals(1, dataSource.getAll().size)
    }

    @Test
    fun selectMarksExactlyOneCity() = runBlocking {
        val first = dataSource.insertOrGet(hangzhou("杭州"))
        val second = dataSource.insertOrGet(beijing())

        dataSource.select(first.id)
        assertEquals(first.id, dataSource.getSelected()?.id)

        dataSource.select(second.id)
        assertEquals(second.id, dataSource.getSelected()?.id)
    }

    @Test
    fun removeDeletesCity() = runBlocking {
        val city = dataSource.insertOrGet(hangzhou("杭州"))

        dataSource.remove(city.id)

        assertTrue(dataSource.getAll().isEmpty())
        assertNull(dataSource.getSelected())
    }

    @Test
    fun replaceCurrentLocationReusesNearbySavedCity() = runBlocking {
        val saved = dataSource.insertOrGet(hangzhou("杭州"))

        val located = dataSource.replaceCurrentLocation(
            City(name = "杭州市", latitude = 30.2765, longitude = 120.1530, country = "", isCurrentLocation = true)
        )

        val all = dataSource.getAll()
        assertEquals(saved.id, located.id)
        assertEquals(1, all.size)
        assertEquals("杭州市", all.single().name)
        assertTrue(all.single().isCurrentLocation)
    }

    @Test
    fun replaceCurrentLocationRemovesPreviousLocationRow() = runBlocking {
        val first = dataSource.replaceCurrentLocation(
            City(name = "当前位置", latitude = 39.9042, longitude = 116.4074, country = "")
        )
        val second = dataSource.replaceCurrentLocation(
            City(name = "当前位置", latitude = 30.2741, longitude = 120.1551, country = "")
        )

        val all = dataSource.getAll()
        assertEquals(1, all.size)
        assertTrue(first.id != second.id)
        assertTrue(abs(all.single().latitude - 30.2741) < 0.0001)
    }

    private fun hangzhou(name: String) = City(
        name = name,
        latitude = 30.2741,
        longitude = 120.1551,
        country = "中国"
    )

    private fun beijing() = City(
        name = "北京",
        latitude = 39.9042,
        longitude = 116.4074,
        country = "中国"
    )
}
