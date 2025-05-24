package com.vlog.my.data.scripts.music

import java.util.Arrays

data class MusicItem(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val filePath: String?,
    val url: String?,
    val musicData: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MusicItem

        if (id != other.id) return false
        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (filePath != other.filePath) return false
        if (url != other.url) return false
        if (musicData != null) {
            if (other.musicData == null) return false
            if (!musicData.contentEquals(other.musicData)) return false
        } else if (other.musicData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (musicData?.contentHashCode() ?: 0)
        return result
    }
}
