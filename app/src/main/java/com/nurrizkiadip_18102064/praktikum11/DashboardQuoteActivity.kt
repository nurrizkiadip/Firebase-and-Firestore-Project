package com.nurrizkiadip_18102064.praktikum11

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nurrizkiadip_18102064.praktikum11.adapter.QuoteAdapter
import com.nurrizkiadip_18102064.praktikum11.data.Quote
import com.nurrizkiadip_18102064.praktikum11.databinding.ActivityDashboardQuoteBinding
import com.nurrizkiadip_18102064.praktikum11.helper.REQUEST_ADD
import com.nurrizkiadip_18102064.praktikum11.helper.REQUEST_UPDATE
import com.nurrizkiadip_18102064.praktikum11.helper.RESULT_ADD
import com.nurrizkiadip_18102064.praktikum11.helper.RESULT_DELETE
import com.nurrizkiadip_18102064.praktikum11.helper.RESULT_UPDATE
import kotlinx.android.synthetic.main.activity_dashboard_quote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DashboardQuoteActivity : AppCompatActivity() {
	private lateinit var adapter: QuoteAdapter
	private lateinit var binding: ActivityDashboardQuoteBinding

	private lateinit var auth: FirebaseAuth
	private lateinit var firestore: FirebaseFirestore

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityDashboardQuoteBinding.inflate(layoutInflater)
		setContentView(binding.root)
		supportActionBar?.title = "Quotes"
		binding.rvQuotes.layoutManager = LinearLayoutManager(this)
		binding.rvQuotes.setHasFixedSize(true)
		adapter = QuoteAdapter(this)

		firestore = Firebase.firestore
		auth = Firebase.auth
		
		binding.fabAdd.setOnClickListener {
			val intent = Intent(this@DashboardQuoteActivity, QuoteAddUpdateActivity::class.java)
			startActivityForResult(intent, REQUEST_ADD)
		}
		loadQuotes()
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		loadQuotes()
	}
	
	private fun loadQuotes() {
		GlobalScope.launch(Dispatchers.Main) {
			progressbar.visibility = View.VISIBLE
			val quotesList = ArrayList<Quote>()

			val currentUser = auth.currentUser
			firestore.collection("quotes")
				.whereEqualTo("uid", currentUser?.uid)
				.get()
				.addOnSuccessListener { result ->
					progressbar.visibility = View.INVISIBLE
					for (document in result) {
						val id = document.id
						val title = document.get("title").toString()
						val description = document.get("description").toString()
						val category = document.get("category").toString()
						val date = document.get("date") as com.google.firebase.Timestamp
						quotesList.add(Quote(id, title, description, category, date))
					}
					if (quotesList.size > 0) {
						binding.rvQuotes.adapter = adapter
						adapter.listQuotes = quotesList
					} else {
//						adapter.listQuotes = ArrayList()
						adapter.listQuotes.clear()
						binding.rvQuotes?.adapter?.notifyDataSetChanged()
						showSnackbarMessage("Tidak ada data saat ini")
					}
				}
				.addOnFailureListener { exception ->
					progressbar.visibility = View.INVISIBLE
					Toast.makeText(this@DashboardQuoteActivity,
						"Error adding document", Toast.LENGTH_SHORT).show()
				}
		}
	}
	
	private fun showSnackbarMessage(message: String) {
		Snackbar.make(binding.rvQuotes, message, Snackbar.LENGTH_SHORT).show()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (data != null) {
			when (requestCode) {
				REQUEST_ADD -> if (resultCode == RESULT_ADD) {
					loadQuotes()
					showSnackbarMessage("Satu item berhasil ditambahkan")
				}
				REQUEST_UPDATE ->
					when (resultCode) {
						RESULT_UPDATE -> {
							loadQuotes()
							showSnackbarMessage("Satu item berhasil diubah")
						}
						RESULT_DELETE -> {
							loadQuotes()
							showSnackbarMessage("Satu item berhasil dihapus")
						}
					}
			}
		}
	}
}