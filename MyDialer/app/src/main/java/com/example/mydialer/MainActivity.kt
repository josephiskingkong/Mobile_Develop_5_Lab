package com.example.mydialer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import kotlin.concurrent.thread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private var contactsList: List<Contact> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.et_search)
        searchButton = findViewById(R.id.btn_search)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ContactsAdapter(emptyList())
        recyclerView.adapter = adapter

        searchButton.setOnClickListener {
            filterContacts(searchEditText.text.toString())
        }

        loadContacts()
    }

    private fun filterContacts(query: String) {
        val filteredList = contactsList.filter {
            it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)
    }

    private fun loadContacts() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download")
            .build()

        thread {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            responseBody?.let {
                val contactsType = object : TypeToken<List<Contact>>() {}.type
                contactsList = Gson().fromJson(it, contactsType)

                runOnUiThread {
                    adapter.updateList(contactsList)
                }
            }
        }
    }
}

data class Contact(
    val name: String,
    val phone: String,
    val type: String?
)

class ContactsAdapter(private var contacts: List<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val phoneTextView: TextView = view.findViewById(R.id.phoneTextView)
    }

    fun updateList(newList: List<Contact>) {
        contacts = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.phoneTextView.text = contact.phone
    }

    override fun getItemCount() = contacts.size
}