package com.example.testinsta.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.testinsta.LoginActivity
import com.example.testinsta.MainActivity
import com.example.testinsta.R
import com.example.testinsta.navigation.model.AlarmDTO
import com.example.testinsta.navigation.model.ContentDTO
import com.example.testinsta.navigation.model.FollowDTO
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
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
            fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)
                ?.setOnClickListener {
                    requestFollow()
                }
        }

        fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerview)?.adapter =
            UserFragmentRecyclerViewAdapter()

        fragmentView?.findViewById<ImageView>(R.id.account_iv_profile)?.setOnClickListener {
            actionPick.launch(Intent(Intent.ACTION_PICK).setType("image/*"))
        }
        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }

    fun getFollowerAndFollowing() {
        firestore?.collection("users")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
                if (followDTO?.followingCount != null) {
                    fragmentView?.findViewById<TextView>(R.id.account_tv_following_count)?.text =
                        followDTO.followingCount.toString()
                }
                if (followDTO?.followerCount != null) {
                    fragmentView?.findViewById<TextView>(R.id.account_tv_follower_count)?.text =
                        followDTO.followerCount.toString()
                    if (followDTO.followers.containsKey(currentUserUid!!)) {
                        fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)?.text =
                            getString(R.string.follow_cancel)
                        fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)?.background?.setColorFilter(
                            ContextCompat.getColor(activity!!, R.color.colorLightGray),
                            PorterDuff.Mode.MULTIPLY
                        )
                    } else {
                        if (uid != currentUserUid) {
                            fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)?.text =
                                getString(R.string.follow)
                            fragmentView?.findViewById<AppCompatButton>(R.id.account_btn_follow_signout)?.background?.colorFilter =
                                null

                        }

                    }
                }
            }
    }

    fun requestFollow() {
        val tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transition ->
            var followDTO = transition.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO.followerCount = 1
                followDTO.followers[uid!!] = true
                transition.set(tsDocFollowing, followDTO)
                return@runTransaction
            }
            if (followDTO.followings.containsKey(uid)) {
                followDTO.followingCount = followDTO.followingCount - 1
                followDTO.followers.remove(uid)
            } else {
                followDTO.followingCount = followDTO.followingCount + 1
                followDTO.followers[uid!!] = true
            }
            transition.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        val tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transition ->
            var followDTO = transition.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
                transition.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
            if (followDTO!!.followers.containsKey(currentUserUid)) {
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)
            } else {
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transition.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }

    fun followerAlarm(destinationUid: String) {
        val alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }

    fun getProfileImage() {
        uid?.let {
            firestore?.collection("profileImages")?.document(it)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    documentSnapshot?.let {
                        documentSnapshot.data?.let { data ->
                            val url = data["image"]
                            activity?.let {
                                val accountIvProfile =
                                    fragmentView?.findViewById<ImageView>(R.id.account_iv_profile)
                                accountIvProfile?.let {
                                    Glide.with(it).load(url).apply(RequestOptions().circleCrop())
                                        .into(accountIvProfile)
                                }
                            }

                        }
                    } ?: return@addSnapshotListener
                }
        }

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

    private val actionPick =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val imageUri = it.data?.data
                    imageUri?.let {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        uid?.let {
                            val storageRef =
                                FirebaseStorage.getInstance().reference.child("userProfileImages")
                                    .child(uid)
                            storageRef.putFile(imageUri)
                                .continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                                    return@continueWithTask storageRef.downloadUrl
                                }.addOnSuccessListener { uri ->
                                    val map = HashMap<String, Any>()
                                    map["image"] = uri.toString()
                                    FirebaseFirestore.getInstance().collection("profileImages")
                                        .document(uid).set(map)
                                }
                        }
                    }


                }
            }
        }
}