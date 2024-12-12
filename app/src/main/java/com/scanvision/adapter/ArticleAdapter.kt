package com.scanvision.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scanvision.data.remote.response.Article
import com.scanvision.databinding.ArticleCardBinding
import com.scanvision.databinding.ArticleCardLongBinding
import com.squareup.picasso.Picasso

class ArticleAdapter(private val articles: List<Article>, private val viewType: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SHORT = 1
        const val VIEW_TYPE_LONG = 2
    }

    inner class ShortArticleViewHolder(private val binding: ArticleCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.articleTitle.text = article.title
            Picasso.get().load(article.urlToImage).into(binding.articleImage)
            binding.root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                binding.root.context.startActivity(intent)
            }
        }
    }

    inner class LongArticleViewHolder(private val binding: ArticleCardLongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.articleTitle.text = article.title
            Picasso.get().load(article.urlToImage).into(binding.articleImage)
            binding.root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SHORT -> {
                val binding = ArticleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ShortArticleViewHolder(binding)
            }
            VIEW_TYPE_LONG -> {
                val binding = ArticleCardLongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LongArticleViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val article = articles[position]
        when (holder) {
            is ShortArticleViewHolder -> holder.bind(article)
            is LongArticleViewHolder -> holder.bind(article)
        }
    }

    override fun getItemCount(): Int = articles.size
}