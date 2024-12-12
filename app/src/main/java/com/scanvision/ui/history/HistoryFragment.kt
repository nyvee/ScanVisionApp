package com.scanvision.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.scanvision.adapter.ResultAdapter
import com.scanvision.data.local.AppDatabase
import com.scanvision.data.local.ResultEntity
import com.scanvision.databinding.FragmentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val resultDao = database.resultDao()

        lifecycleScope.launch {
            resultDao.getAllResults().collect { results ->
                if (results.isEmpty()) {
                    binding.tvNoHistory.visibility = View.VISIBLE
                    binding.rvHistoryResult.visibility = View.GONE
                } else {
                    binding.tvNoHistory.visibility = View.GONE
                    binding.rvHistoryResult.visibility = View.VISIBLE
                    val adapter = ResultAdapter(results) { result ->
                        deleteResult(result)
                    }
                    binding.rvHistoryResult.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvHistoryResult.adapter = adapter
                }
            }
        }
    }

    private fun deleteResult(result: ResultEntity) {
        val database = AppDatabase.getDatabase(requireContext())
        val resultDao = database.resultDao()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                resultDao.deleteResult(result)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Data successfully deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}