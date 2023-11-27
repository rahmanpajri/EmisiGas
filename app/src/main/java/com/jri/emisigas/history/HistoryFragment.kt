package com.jri.emisigas.history

import HistoryAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jri.emisigas.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private lateinit var historyAdapter: HistoryAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
    ): View {
        val historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view = binding.root

        historyAdapter = HistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        historyViewModel.getHistoryData().observe(viewLifecycleOwner) { historyData ->
            historyAdapter.submitList(historyData)
        }

        return view
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}