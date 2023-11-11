package com.jri.emisigas.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryViewModel : ViewModel() {
    private val databaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun getHistoryData(): LiveData<List<History>>{
        val liveData = MutableLiveData<List<History>>()

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<History>()
                for (dataSnapshot in snapshot.children){
                    val history = dataSnapshot.getValue(History::class.java)
                    history?.let { historyList.add(it) }
                }
                liveData.value = historyList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return liveData
    }
}