package com.example.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.adapter.Action
import com.example.myapplication.adapter.TravelAdapter
import com.example.myapplication.database.model.Marker
import com.example.myapplication.databinding.FragmentListBinding
import com.example.myapplication.view_model.MainViewModel


class ListFragment : Fragment(), TravelAdapter.ClickListener {

    private var viewBinding: FragmentListBinding? = null
    private val binding: FragmentListBinding get() = viewBinding!!
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadList()
    }

    override fun onClick(action: Action, marker: Marker, position: Int) {
        when (action) {
            Action.DELETE -> {
                mainViewModel.deleteMarker(marker.latitude, marker.longitude)
                loadList()
            }
            Action.CLICK -> {
                mainViewModel.showMarker.postValue(marker)
                findNavController().popBackStack()
            }
        }
    }

    private fun loadList() {
        mainViewModel.getAllMarkers().observe(viewLifecycleOwner) {
            binding.rvList.adapter = TravelAdapter().apply {
                clickListener = this@ListFragment
                setData(it)
            }
        }
    }

}