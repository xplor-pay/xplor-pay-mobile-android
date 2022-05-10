package com.xplore.paymobile.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearent.idtech.android.wrapper.logger.FileLoggingTree
import com.xplore.paymobile.databinding.FragmentLogsBinding

class LogsFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        populateUI()

        return root
    }

    private fun populateUI() {
        val logsAdapter = LogsAdapter()

        logsAdapter.submitList(FileLoggingTree.readLogsFromInternalStorage(requireContext()))

        binding.apply {
            logsList.apply {
                adapter = logsAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}