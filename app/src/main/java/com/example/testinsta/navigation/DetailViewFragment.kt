package com.example.testinsta.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testinsta.databinding.FragmentDetailBinding
import com.example.testinsta.databinding.ItemDetailBinding
import com.example.testinsta.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {

    var firestore: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailBinding.inflate(layoutInflater)

        firestore = FirebaseFirestore.getInstance()

        binding.detailviewfragmentRecyclerview.adapter = DetailViewRecyclerViewAdapter()

        return binding.root
    }

    inner class DetailViewRecyclerViewAdapter :
        RecyclerView.Adapter<DetailViewRecyclerViewAdapter.CustomViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, error ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    querySnapshot?.let {
                        for (snapshot in it.documents) {
                            val item = snapshot.toObject(ContentDTO::class.java)
                            item?.let {
                                contentDTOs.add(item)
                                contentUidList.add(snapshot.id)
                            }
                            notifyDataSetChanged()
                        }
                    }
                }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DetailViewRecyclerViewAdapter.CustomViewHolder {
            val binding = ItemDetailBinding.inflate(layoutInflater)
            return CustomViewHolder(binding)
        }

        inner class CustomViewHolder(var binding: ItemDetailBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val binding = holder.binding

            binding.detailviewitemProfileTextview.text = contentDTOs[position].userId
            Glide.with(binding.detailviewitemImageviewContent.context)
                .load(contentDTOs[position].imageUrl).into(binding.detailviewitemImageviewContent)
            binding.detailviewitemExplainTextview.text = contentDTOs[position].explain

            val favoriteText = "Likes ${contentDTOs[position].favoriteCount}"
            binding.detailviewitemFavoritecounterTextview.text = favoriteText

            Glide.with(binding.detailviewitemProfileImage.context)
                .load(contentDTOs[position].imageUrl).into(binding.detailviewitemProfileImage)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}


