package com.fgr.dicodingstory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.fgr.dicodingstory.data.StoryRepository
import com.fgr.dicodingstory.retrofit.ListStoryItem
import com.fgr.dicodingstory.ui.home.list_story.ListStoryAdapter
import com.fgr.dicodingstory.ui.home.list_story.ListStoryViewModel
import com.fgr.dicodingstory.utils.DataDummy
import com.fgr.dicodingstory.utils.PagingSource
import com.fgr.dicodingstory.utils.getOrAwaitValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ListStoryViewModelTest {

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    @get: Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var listStoryViewModel: ListStoryViewModel

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        listStoryViewModel = ListStoryViewModel(storyRepository)
    }

    @Test
    fun `when get story should not null and return data`() = runTest {
        val dummyStory = DataDummy.generateDummyStories()
        val data: PagingData<ListStoryItem> = PagingSource.snapshot(dummyStory)
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data
        `when`(storyRepository.getStory()).thenReturn(expectedStory)
        val actualStory: PagingData<ListStoryItem> = listStoryViewModel.listStory.getOrAwaitValues()
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)

        Assert.assertNotNull(differ.snapshot())
        assertEquals(dummyStory.size, differ.snapshot().size)
        assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when get empty story should not return data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data
        `when`(storyRepository.getStory()).thenReturn(expectedStory)
        val actualStory: PagingData<ListStoryItem> = listStoryViewModel.listStory.getOrAwaitValues()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)
        assertEquals(0, differ.snapshot().size)
    }
}