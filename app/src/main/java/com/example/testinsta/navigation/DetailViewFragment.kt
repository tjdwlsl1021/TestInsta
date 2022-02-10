package com.example.testinsta.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testinsta.R
import com.example.testinsta.databinding.FragmentDetailBinding
import com.example.testinsta.databinding.ItemDetailBinding
import com.example.testinsta.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {
    private val TAG = "HSJ"

    var firestore: FirebaseFirestore? = null
    var uid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailBinding.inflate(layoutInflater)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "uid: $uid")

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
                    } ?: return@addSnapshotListener
                }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DetailViewRecyclerViewAdapter.CustomViewHolder {
            val binding = ItemDetailBinding.inflate(layoutInflater)

            val holder = CustomViewHolder(binding)
            holder.binding.detailviewitemFavoriteImageview.setOnClickListener {
                favoriteEvent(holder.bindingAdapterPosition)
            }
            holder.binding.detailviewitemProfileImage.setOnClickListener {
                val fragment = UserFragment()
                val bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[holder.bindingAdapterPosition].uid)
                bundle.putString("userId", contentDTOs[holder.bindingAdapterPosition].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, fragment)?.commit()
            }
            return holder
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

            if (contentDTOs[position].favorites.containsKey(uid)) {
                binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite)
            } else {
                binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position: Int) {
            val tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            tsDoc?.let {
                firestore?.runTransaction { transition ->
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    uid?.let {
                        val contentDTO = transition.get(tsDoc).toObject(ContentDTO::class.java)
                        contentDTO?.let {
                            if (contentDTO.favorites.containsKey(uid)) {
                                contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                                contentDTO.favorites.remove(uid)
                            } else {
                                contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                                contentDTO.favorites[uid] = true
                            }
                            transition.set(tsDoc, contentDTO)
                        }
                    }
                }
            }
        }
    }
}


