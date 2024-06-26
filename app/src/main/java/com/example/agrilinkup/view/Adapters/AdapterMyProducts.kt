package com.example.agrilinkup.view.Adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrilinkup.Models.Entities.ProductModel
import com.example.agrilinkup.Models.Entities.ProductSharedPreferance
import com.example.agrilinkup.Models.PreferenceManager
import com.example.agrilinkup.R
import com.example.agrilinkup.databinding.ItemMyProductListingsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class AdapterMyProducts(private var items: List<ProductModel>, val context: Context, val navigator: NavController) :
    RecyclerView.Adapter<AdapterMyProducts.ViewHolder>() {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val storage= FirebaseStorage.getInstance().getReference()
    val preferenceManager=PreferenceManager(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyProductListingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    fun updateList(newList: List<ProductModel>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        holder.bind(data)
    }

    inner class ViewHolder(val binding: ItemMyProductListingsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductModel) {
            val user = preferenceManager.getUserData()
            if (user != null) {
                binding.txtItemProductSeller.text = user.fullName
            }
            binding.txtItemProductName.text = product.productTitle
            //binding.txtItemProductSeller.text = product.user_ID
            binding.txtItemProductLocation.text = product.UserLocation
            binding.txtItemProductPrice.text = product.pricePerUnit
            Glide.with(context)
                .load(product.productImgeUri)
                .into(binding.imgItemProductThumb)
            binding.AvailableStatus.text=product.productStatus

            binding.imgItemProductThumb.setOnClickListener(View.OnClickListener {
                val builder= AlertDialog.Builder(context,R.style.CustomAlertDialog).create()
                val inflaterRaw=LayoutInflater.from(context)
                val view= inflaterRaw.inflate(R.layout.custom_product_image_view_layout,null)
                val text=view.findViewById<TextView>(R.id.productNameTxtView)
                text.text=product.productTitle
                val  image=view.findViewById<ImageView>(R.id.productImageview)
                Glide.with(context)
                    .load(product.productImgeUri)
                    .into(image)
                val layoutRemove=view.findViewById<LinearLayout>(R.id.layour12)
                layoutRemove.visibility= View.INVISIBLE
//                val btnCart=view.findViewById<Button>(R.id.button2)
//                btnCart.text="Delete From Cart"
//                btnCart.setOnClickListener(View.OnClickListener {
//                    deleteProduct(product)
//                })
                builder.setView(view)
                builder.setCanceledOnTouchOutside(true)
                builder.show()
            })

            binding.onItmeClickLayout.setOnClickListener(View.OnClickListener {
                val preferenceManager=ProductSharedPreferance(context)
                product.tempValue="MyProductsFragment"
                preferenceManager.saveTempProduct(product)
                navigator.navigate(R.id.action_userProductsFragment2_to_productDetailsFragment)
            })


            deleteProduct(product)
            updateProduct(product)
        }


        private fun deleteProduct(product: ProductModel) {

            val imageUri=product.productImgeUri.toUri()
            val imageName=getImageNameFromUri(imageUri)
            deleteProductImage(imageName)

            binding.deleteProductListing.setOnClickListener {
                db.collection("users_products")
                    .document(auth.uid!!)
                    .collection("products")
                    .document(product.docId)
                    .delete()



            }
        }
        private fun getImageNameFromUri(uri: Uri): String {
            val path = uri.path
            return path?.substring(path.lastIndexOf('/') + 1) ?: ""
        }

        private fun deleteProductImage(imageName: String) {
            // Get reference to the old profile image
            val oldProfileImageRef = storage.child("Product_images").child(imageName)

            // Delete the old profile image
            oldProfileImageRef.delete()
                .addOnSuccessListener {
                    // Handle successful deletion
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                    Log.e("Exception", "Error deleting old profile image: $exception")
                }
        }

        private fun updateProduct(product: ProductModel){
            binding.updateProductListing.setOnClickListener{
                val preferenceManager=ProductSharedPreferance(context)
                preferenceManager.saveTempProduct(product)
                navigator.navigate(R.id.action_userProductsFragment2_to_updateMyProductFragment)
            }
        }
    }

}