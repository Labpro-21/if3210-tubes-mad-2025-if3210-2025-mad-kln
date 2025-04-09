package com.android.purrytify.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.purrytify.R
import com.android.purrytify.data.local.entities.Song

class SongAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songImage: ImageView = itemView.findViewById(R.id.songImage)
        private val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        private val songArtist: TextView = itemView.findViewById(R.id.songArtist)

        fun bind(song: Song) {
            songImage.load(song.imageUri)
            songTitle.text = song.title
            songArtist.text = song.artist

            itemView.setOnClickListener {
                onItemClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_card, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

}
