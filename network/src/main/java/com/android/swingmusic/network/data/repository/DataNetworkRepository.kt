package com.android.swingmusic.network.data.repository

import com.android.swingmusic.core.data.mapper.Map.toFolderAndTracks
import com.android.swingmusic.core.data.mapper.Map.toFoldersAndTracksRequestDto
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.util.Resource
import com.android.swingmusic.network.data.api.ApiService
import com.android.swingmusic.network.domain.repository.NetworkRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DataNetworkRepository @Inject constructor(
    private val apiService: ApiService,
) : NetworkRepository {
    override suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Resource<FoldersAndTracks> {
        return try {
            val foldersAndTracksDto =
                apiService.getFoldersAndTracks(requestData.toFoldersAndTracksRequestDto())
            Resource.Success(data = foldersAndTracksDto.toFolderAndTracks())

        } catch (e: IOException) {
            Resource.Error<FoldersAndTracks>(message = "Unable to fetch folders. Check your connection and try again!")

        } catch (e: HttpException) {
            Resource.Error<FoldersAndTracks>(
                message = e.localizedMessage ?: "An unexpected error occurred!"
            )
        }
    }
}