package com.example.mylauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkspaceAdapter(
    private val pages: List<List<AppInfo>>,
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<WorkspaceAdapter.PageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val recycler = RecyclerView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(parent.context, 4)
            overScrollMode = View.OVER_SCROLL_NEVER
            setPadding(0, 12, 0, 12)
            clipToPadding = false
        }
        return PageViewHolder(recycler)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.recycler.adapter = AppGridAdapter(onAppClick).apply {
            submitList(pages[position])
        }
    }

    override fun getItemCount(): Int = pages.size.coerceAtLeast(1)

    class PageViewHolder(val recycler: RecyclerView) :
        RecyclerView.ViewHolder(recycler)
}
