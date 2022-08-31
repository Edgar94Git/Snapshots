package com.example.snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {
    private var mPhotoSelectedUri: Uri? = null
    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStoregeReference: StorageReference
    private lateinit var mDatabaseReferencce: DatabaseReference

    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            mPhotoSelectedUri = it.data?.data
            mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
            mBinding.tilTitle.visibility = View.VISIBLE
            mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentAddBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnPost.setOnClickListener { postSnapshot() }
        mBinding.btnSelect.setOnClickListener { openGallery() }
        mStoregeReference = FirebaseStorage.getInstance().reference
        mDatabaseReferencce = FirebaseDatabase.getInstance().reference.child(SnapshotsApplication.PATH_SNAPSHOTS)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }

    private fun postSnapshot() {
        mBinding.progressBar.visibility = View.VISIBLE
        val key = mDatabaseReferencce.push().key!!
        val storegeReference = mStoregeReference.child(SnapshotsApplication.PATH_SNAPSHOTS)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(key)
        if(mPhotoSelectedUri != null){
            storegeReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = String.format("%s%%", progress)
                }
                .addOnCompleteListener{
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener{ it ->
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapShot(key, it.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.INVISIBLE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }
                    Snackbar.make(mBinding.root, getString(R.string.image_add_message), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Snackbar.make(mBinding.root, getString(R.string.image_error_message), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSnapShot(key: String, url: String, title: String){
        val snapshot = Snapshot(title = title, photoUrl =  url)
        mDatabaseReferencce.child(key).setValue(snapshot)
    }
}