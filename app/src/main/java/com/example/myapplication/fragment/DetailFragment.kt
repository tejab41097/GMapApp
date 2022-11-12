package com.example.myapplication.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.example.myapplication.database.model.Marker
import com.example.myapplication.databinding.FragmentDetailBinding
import com.example.myapplication.view_model.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class DetailFragment : BottomSheetDialogFragment() {

    companion object {
        var listener: Listener? = null
    }

    interface Listener {
        fun onDelete(isRemove: Boolean)
    }

    private val mainViewModel: MainViewModel by activityViewModels()
    private var isRemove = false
    private var viewBinding: FragmentDetailBinding? = null
    private val binding: FragmentDetailBinding get() = viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
        }
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.selectedMarker.observe(viewLifecycleOwner) { marker ->
            marker?.let {
                if (it.title != null) {
                    binding.btnSave.isVisible = false
                    binding.tfName.isEnabled = false
                    binding.etName.setText(it.title)
                }

                isRemove = it.title == null
                binding.etCoordinates.setText(
                    getString(
                        R.string.two_long,
                        it.latitude,
                        it.longitude
                    )
                )
                binding.etName.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                        save(it.latitude, it.longitude)
                    return@setOnEditorActionListener false
                }
                binding.btnSave.setOnClickListener { _ ->
                    if (binding.etName.text.toString().isNotEmpty())
                        save(it.latitude, it.longitude)
                    else
                        binding.tfName.error = getString(R.string.please_enter_name)
                }
                binding.fbDeleteMarker.setOnClickListener {
                    isRemove = true
                    dismiss()
                }
                binding.fbCloseMarker.setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    private fun save(latitude: Double, longitude: Double) {
        mainViewModel.save(
            Marker(
                latitude,
                longitude,
                binding.etName.text.toString()
            )
        )
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        listener?.onDelete(isRemove)
        super.onDismiss(dialog)
    }
}