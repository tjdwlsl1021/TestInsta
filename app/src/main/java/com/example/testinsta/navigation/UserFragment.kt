package com.example.testinsta.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.testinsta.LoginActivity
import com.example.testinsta.MainActivity
import com.example.testinsta.R
import com.example.testinsta.navigation.model.ContentDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import splitties.fragments.start

class UserFragment : Fragment() {

    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)

        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if (uid == currentUserUid) {
            fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)?.text =
                getString(R.string.signout)
            fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)
                ?.setOnClickListener {
                    activity?.finish()
                    start<LoginActivity>()
                    auth?.signOut()
                }
        } else {
            fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)?.text =
                getString(R.string.follow)
            val mainactivity = (activity as MainActivity)
            mainactivity.findViewById<TextView>(R.id.toolbar_username)?.text =
                arguments?.getString("userId")
            mainactivity.findViewById<ImageView>(R.id.toolbar_btn_back)?.setOnClickListener {
                mainactivity.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId =
                    R.id.action_home
            }
            mainactivity.findViewById<ImageView>(R.id.toolbar_title_image)?.visibility = View.GONE
            mainactivity.findViewById<TextView>(R.id.toolbar_username)?.visibility = View.VISIBLE
            mainactivity.findViewById<ImageView>(R.id.toolbar_btn_back)?.visibility = View.VISIBLE
        }

        fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerview)?.adapter =
            UserFragmentRecyclerViewAdapter()
        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    fragmentView?.findViewById<TextView>(R.id.account_tv_post_count)?.text =
                        contentDTOs.size.toString()
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) :
            RecyclerView.ViewHolder(imageview)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop()).into(imageview)
        }


    }
}