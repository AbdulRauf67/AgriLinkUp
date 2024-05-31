package com.example.agrilinkup.View.Fragments.UserAccount

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.agrilinkup.Models.PreferenceManager
import com.example.agrilinkup.R
import com.example.agrilinkup.View.Adapters.AdapterMyProducts
import com.example.agrilinkup.View.Fragments.AddProductListingFragment
import com.example.agrilinkup.View.VmProfile
import com.example.agrilinkup.databinding.FragmentUserProductsBinding
import com.example.agrilinkup.ui.ProfileRepository
import com.example.agrilinkup.utils.DataState
import com.example.agrilinkup.utils.ProgressDialogUtil.dismissProgressDialog
import com.example.agrilinkup.utils.ProgressDialogUtil.showProgressDialog
import com.example.agrilinkup.utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserProductsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProductsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding:FragmentUserProductsBinding

    private lateinit var vmProfile: VmProfile
    private lateinit var preferenceManager:PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserProductsBinding.inflate(inflater, container, false)

        val prefs= PreferenceManager(requireContext())
        val auth= FirebaseAuth.getInstance()
        val db= FirebaseFirestore.getInstance()
        val storage= FirebaseStorage.getInstance().getReference()
        val context=requireContext()
        preferenceManager=prefs

        val profileRepo= ProfileRepository(db, auth, prefs, storage, context)
        vmProfile = VmProfile(profileRepo)
        vmProfile.fetchProducts()
        setUpObserver()
        return binding.root

          }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addProductsListings.setOnClickListener {
            findNavController().navigate(R.id.action_userProductsFragment2_to_addProductListingFragment3)
        }

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserProductsG.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserProductsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



    private fun setUpObserver() {
        vmProfile.fetchProduct.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    val productsList = it.data
                    if (!productsList.isNullOrEmpty()) {
                        val navigator=findNavController()
                        binding.recyclerViewProductItems.adapter =AdapterMyProducts(productsList,requireContext(),navigator)
                      }
//                    else {
//                        binding.lyNoCars.visible()
//                        binding.rvVehicles.gone()
//                    }
                    dismissProgressDialog()
                }

                is DataState.Error -> {
                    toast(it.errorMessage)
                    dismissProgressDialog()
                }

                is DataState.Loading -> {
                    showProgressDialog()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}
