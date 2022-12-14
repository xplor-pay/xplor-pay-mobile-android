package com.xplore.paymobile.ui.merchantselection.search.merchant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearent.idtech.android.wrapper.ui.util.MarginItemDecoration
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentMerchantSearchBinding
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.ui.merchantselection.MerchantSelectSharedViewModel
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import com.xplore.paymobile.ui.merchantselection.search.list.SeeMoreListAdapter
import com.xplore.paymobile.ui.merchantselection.search.terminal.TerminalSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MerchantSearchFragment : BaseFragment() {

    private var _binding: FragmentMerchantSearchBinding? = null
    private val binding get() = _binding!!

    override val hasBottomNavigation: Boolean = false

    private val viewModel by viewModels<TerminalSearchViewModel>()
    private val sharedViewModel by activityViewModels<MerchantSelectSharedViewModel>()

    private val itemsAdapter = MerchantsListAdapter(onItemClicked = {

    })
    private val seeMoreAdapter = SeeMoreListAdapter(onItemClicked = {
        mockMerchantList()
    })
    private val concatAdapter = ConcatAdapter(itemsAdapter, seeMoreAdapter)

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
        setupTitle()
        setupSearchBar()
        setupList()
    }

    private fun setupTitle() {
        with(binding) {
            titleTextView.text = getString(R.string.merchants)
            toolbarLayout.toolbarTitle.text = getString(R.string.merchants)
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
                if (count >= 3) {
                    viewModel.searchForQuery(text.toString())
                }
            }
        }
    }

    private fun setupList() {
        binding.apply {
            itemsList.adapter = concatAdapter
            itemsList.layoutManager = LinearLayoutManager(requireContext())
            itemsList.addItemDecoration(MarginItemDecoration(8, 0))
            mockMerchantList()
        }
    }

    private fun mockMerchantList() {
        lifecycleScope.launch {
            val seeMoreButton = SeeMoreListAdapter.SeeMoreItem(getString(R.string.see_more), true)
            seeMoreAdapter.submitList(listOf(seeMoreButton))
            delay(2000)
            seeMoreButton.isLoading = false
            seeMoreAdapter.notifyItemChanged(0)
            val currentList = itemsAdapter.currentList.toMutableList()
            for (index in (currentList.size + 1)..(currentList.size + 10)) {
                currentList.add(MerchantsListAdapter.MerchantItem("Merchant #$index"))
            }
            itemsAdapter.submitList(currentList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}