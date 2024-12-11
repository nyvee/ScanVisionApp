package com.scanvision.ui.history

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scanvision.data.local.ResultEntity
import com.scanvision.databinding.ResultCardBinding
import com.scanvision.utils.DateFormat

class ResultAdapter(
    private val results: List<ResultEntity>,
    private val onDeleteClick: (ResultEntity) -> Unit
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(private val binding: ResultCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ResultEntity) {
            binding.tvResultHistory.text = result.classificationResult
            binding.tvTimeHistory.text = DateFormat.formatTime(result.time)
            binding.tvDateHistory.text = DateFormat.formatDate(result.date)
            binding.ivResultHistory.setImageURI(Uri.parse(result.imageUri))
            binding.btnDelete.setOnClickListener {
                onDeleteClick(result)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ResultCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size
}