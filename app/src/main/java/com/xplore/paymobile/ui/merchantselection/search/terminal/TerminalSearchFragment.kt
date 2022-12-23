package com.xplore.paymobile.ui.merchantselection.search.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearent.idtech.android.wrapper.ui.util.MarginItemDecoration
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentMerchantSearchBinding
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.ui.merchantselection.MerchantSelectSharedViewModel
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TerminalSearchFragment : BaseFragment() {

    private var _binding: FragmentMerchantSearchBinding? = null
    private val binding get() = _binding!!

    override val hasBottomNavigation: Boolean = false

    private val viewModel by viewModels<TerminalSearchViewModel>()
    private val sharedViewModel by activityViewModels<MerchantSelectSharedViewModel>()

    private var adapter = MerchantsListAdapter(onItemClicked = { item, position ->
        binding.okButton.isEnabled = item.isSelected
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTerminalsFlow()
        setupTitle()
        setupSearchBar()
        setupList()
    }

    private fun setupTerminalsFlow() {
        viewModel.setTerminals(sharedViewModel.terminals)
        lifecycleScope.launch {
            viewModel.terminalsFlow.collect { list ->
                Timber.d("TESTEST got terminals collect ${list.size}")
                adapter.submitList(list.map {
                    MerchantsListAdapter.MerchantItem(it.terminalName, it.terminalPKId)
                })
            }
        }
    }

    private fun setupTitle() {
        with(binding) {
            titleTextView.text = getString(R.string.terminals)
            toolbarLayout.toolbarTitle.text = getString(R.string.terminals)
            toolbarLayout.backButton.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun setupSearchBar() {
        binding.apply {
            searchInputLayout.setOnFocusChangeListener { _, hasFocus ->

            }
            searchEditText.doOnTextChanged { text, _, _, count ->
                Timber.d("TESTEST text $text count $count")
                viewModel.searchForQuery(text.toString())
            }
        }
    }

    private fun setupList() {
        binding.apply {
            itemsList.adapter = adapter
            itemsList.layoutManager = LinearLayoutManager(requireContext())
            itemsList.addItemDecoration(MarginItemDecoration(8, 0))
            okButton.setOnClickListener {
                val selectedMerchant = adapter.currentList.find { it.isSelected }
                selectedMerchant?.let {
                    viewModel.saveTerminal(it)
                }
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}