package com.hyperdrop.app.data.transport.nearby

import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyTransportRepositoryImplTest {

    private lateinit var connectionsClient: ConnectionsClient
    private lateinit var repository: NearbyTransportRepositoryImpl

    @Before
    fun setup() {
        connectionsClient = mockk(relaxed = true)
        repository = NearbyTransportRepositoryImpl(connectionsClient)
    }

    @Test
    fun `startDiscovery emits peers when endpoints are found and lost`() = runTest {
        val callbackSlot = slot<EndpointDiscoveryCallback>()
        
        // Mock startDiscovery to capture the callback
        val mockTask: Task<Void> = mockk(relaxed = true)
        every { 
            connectionsClient.startDiscovery(any(), capture(callbackSlot), any<DiscoveryOptions>()) 
        } returns mockTask

        val emittedLists = mutableListOf<List<com.hyperdrop.app.domain.model.Peer>>()
        val job = launch {
            repository.startDiscovery().toList(emittedLists)
        }

        // Wait for coroutines to settle
        advanceUntilIdle()

        assertTrue("Callback should be captured", callbackSlot.isCaptured)
        val callback = callbackSlot.captured

        // Simulate finding endpoint 1
        val endpointInfo1 = DiscoveredEndpointInfo("Device A", "service_id")
        callback.onEndpointFound("endpoint_1", endpointInfo1)

        // Simulate finding endpoint 2
        val endpointInfo2 = DiscoveredEndpointInfo("Device B", "service_id")
        callback.onEndpointFound("endpoint_2", endpointInfo2)

        // Simulate losing endpoint 1
        callback.onEndpointLost("endpoint_1")

        advanceUntilIdle()

        // Verify emissions (Initial empty list may or may not be emitted depending on task success, 
        // but our mock task is relaxed and doesn't invoke the success listener in this basic test setup.
        // We focus on the found/lost callback emissions).
        
        // Let's check the size of the emissions list dynamically to avoid strict initial state mismatch
        // Usually, 3 emissions from the callbacks (find 1, find 2, lose 1)
        val nonInitialEmissions = emittedLists.filter { it.isNotEmpty() }
        
        assertEquals(3, nonInitialEmissions.size)
        
        // First non-empty emission: endpoint 1
        assertEquals(1, nonInitialEmissions[0].size)
        assertEquals("endpoint_1", nonInitialEmissions[0][0].peerId)
        assertEquals("Device A", nonInitialEmissions[0][0].displayName)
        
        // Second emission: endpoint 1 and 2
        assertEquals(2, nonInitialEmissions[1].size)
        
        // Third emission: only endpoint 2
        assertEquals(1, nonInitialEmissions[2].size)
        assertEquals("endpoint_2", nonInitialEmissions[2][0].peerId)
        assertEquals("Device B", nonInitialEmissions[2][0].displayName)

        job.cancel()
    }
}
