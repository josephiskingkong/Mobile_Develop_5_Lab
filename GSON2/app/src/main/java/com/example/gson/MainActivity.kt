package com.example.gson

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        loadPhotos()
    }

    private fun loadPhotos() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1")
            .build()

        thread {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            responseBody?.let {
                val wrapper = Gson().fromJson(it, Wrapper::class.java)
                val photoUrls = wrapper.photos.photo.mapIndexed { index, photo ->
                    if (index % 5 == 0) {
                        Timber.d(photo.toString())
                    }
                    generatePhotoUrl(photo)
                }

                runOnUiThread {
                    adapter = PhotoAdapter(photoUrls) { url ->
                        copyToClipboard(url)
                        Timber.i(url)
                    }
                    recyclerView.adapter = adapter
                }
            }
        }
    }

    private fun generatePhotoUrl(photo: Photo): String {
        return "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_z.jpg"
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("photoUrl", text)
        clipboard.setPrimaryClip(clip)
    }
}

class PhotoAdapter(
    private val photos: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoUrl = photos[position]
        Picasso.get().load(photoUrl).into(holder.imageView)
        holder.imageView.setOnClickListener { onClick(photoUrl) }
    }

    override fun getItemCount() = photos.size
}

data class Photo(
    val id: String,
    val secret: String,
    val server: String,
    val farm: Int
)

data class PhotoPage(
    val photo: List<Photo>
)

data class Wrapper(
    val photos: PhotoPage
)
