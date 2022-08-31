package com.example.snapshots

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment(), HomeAux {

    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolderView>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private val PATH_SNAPSHOT = "snapshots"
    private val PATH_LIKELIST = "likeList"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)

        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, SnapshotParser {
                val snapshot = it.getValue(Snapshot::class.java)
                snapshot!!.id = it.key!!
                snapshot
            }).build()
            //.setQuery(query, Snapshot::class.java).build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolderView>(options){
            private lateinit var mContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolderView {
                mContext = parent.context
                val view = LayoutInflater.from(mContext).inflate(R.layout.item_snapshot, parent, false)
                return SnapshotHolderView(view)
            }

            override fun onBindViewHolder(
                holder: SnapshotHolderView,
                position: Int,
                model: Snapshot
            ) {
                val snapShot = getItem(position)
                with(holder){
                    setListener(snapShot)
                    binding.tvTitle.text = snapShot.title
                    binding.cbLike.text = snapShot.likeList.keys.size.toString()
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked = snapShot.likeList
                            .containsKey(it.uid)
                    }
                    Glide.with(mContext)
                        .load(snapShot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPhoto)
                }
            }

            @SuppressLint("NotifyDataSetChanged")//error interno firebase 8.0.1
            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        mLayoutManager = LinearLayoutManager(context)

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    private fun deleteSnapshot(snapshot: Snapshot){
        val databaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
        databaseReference.child(snapshot.id).removeValue()
    }

    private fun setLike(snapshot: Snapshot, checked: Boolean){
        val databaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
        if(checked){
            databaseReference.child(snapshot.id).child(PATH_LIKELIST)
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(checked)
        }
        else{
            databaseReference.child(snapshot.id).child(PATH_LIKELIST)
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(null)
        }
    }

    inner class SnapshotHolderView(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){
            binding.btnDelete.setOnClickListener { deleteSnapshot(snapshot) }
            binding.cbLike.setOnCheckedChangeListener { _, checked ->
                setLike(snapshot, checked)
            }
        }
    }

    override fun goToTop() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }
}