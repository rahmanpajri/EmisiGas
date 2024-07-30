package com.jri.emisigas.history

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.R
import com.jri.emisigas.databinding.FragmentHistoryBinding
import com.jri.emisigas.result.Result

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private lateinit var rvHistory: RecyclerView
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val list = ArrayList<Result>()
    private val binding get() = _binding!!
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view = binding.root
        progressDialog = ProgressDialog(requireContext(), com.google.android.material.R.style.Theme_AppCompat_Light_Dialog)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.setIndeterminateDrawable(resources.getDrawable(R.drawable.rotate_animation, null))

        rvHistory = binding.rvHistory
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.setHasFixedSize(true)

        getHistory()

        return view
    }

    private fun showLoading() {
        progressDialog.show()
    }

    private fun hideLoading() {
        progressDialog.dismiss()
    }

    private fun getHistory() {
        showLoading()
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        dbRef = database.reference.child("result")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hideLoading()
                if(snapshot.exists()){
                    for (resultSnapshot in snapshot.children){
                        val result = resultSnapshot.getValue(Result::class.java)
                        if (result != null) {
                            if (result.user_id == user?.uid) {
                                list.add(result!!)
                            }
                        }
                    }
                    rvHistory.adapter = HistoryAdapter(list)
                }else{
                    Toast.makeText(requireContext(), "Data is Null", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                hideLoading()
                Toast.makeText(requireContext(), "Database Error, Check your Connection", Toast.LENGTH_SHORT).show()
            }

        })
    }
}