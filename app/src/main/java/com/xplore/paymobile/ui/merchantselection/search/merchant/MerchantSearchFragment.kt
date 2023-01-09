package com.xplore.paymobile.ui.merchantselection.search.merchant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearent.idtech.android.wrapper.ui.util.MarginItemDecoration
import com.xplore.paymobile.R
import com.xplore.paymobile.data.web.Merchant
import com.xplore.paymobile.databinding.FragmentMerchantSearchBinding
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.ui.merchantselection.MerchantSelectSharedViewModel
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import com.xplore.paymobile.ui.merchantselection.search.list.SeeMoreListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MerchantSearchFragment : BaseFragment() {

    private var _binding: FragmentMerchantSearchBinding? = null
    private val binding get() = _binding!!

    override val hasBottomNavigation: Boolean = false

    private val viewModel by viewModels<MerchantSearchViewModel>()
    private val sharedViewModel by activityViewModels<MerchantSelectSharedViewModel>()

    private val itemsAdapter = MerchantsListAdapter(onItemClicked = { item, _ ->
        binding.okButton.isEnabled = item.isSelected
    })

    private val seeMoreAdapter = SeeMoreListAdapter(onItemClicked = {
        showLoading()
        viewModel.nextPage()
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
        setupMerchantsFlow()
        setupTitle()
        setupSearchBar()
        setupList()
    }

    private fun setupMerchantsFlow() {
        lifecycleScope.launch {
            viewModel.resultsFlow.collect { merchants ->
                Timber.d("Received merchants ${merchants.size}")
                if (seeMoreAdapter.currentList.isNotEmpty()) {
                    val seeMoreItem = seeMoreAdapter.currentList[0]
                    seeMoreItem.isLoading = false
                    seeMoreAdapter.notifyItemChanged(0)
                    if (merchants.size.mod(10) != 0 || merchants.isEmpty()) {
                        hideSeeMore()
                    }
                }
                submitList(merchants)
            }
        }
    }

    private fun submitList(merchants: List<Merchant>) {
        itemsAdapter.submitList(merchants.map {
            MerchantsListAdapter.MerchantItem(it.merchantName, it.merchantNumber)
        }) {
            if (viewModel.currentPage() == 1 && merchants.isNotEmpty()) {
                binding.itemsList.scrollToPosition(0)
            }
        }
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
            searchEditText.doOnTextChanged { text, _, _, _ ->
                if (text?.length!! >= 3) {
                    submitList(emptyList())
                    viewModel.searchForQuery(text.toString())
                    showLoading()
                } else if (text.isEmpty()) {
                    submitList(emptyList())
                    hideSeeMore()
                }
            }
        }
    }

    private fun setupList() {
        binding.apply {
            itemsList.adapter = concatAdapter
            itemsList.layoutManager = LinearLayoutManager(requireContext())
            itemsList.addItemDecoration(MarginItemDecoration(8, 0))
            showLoading()
            viewModel.searchForQuery("")
            okButton.setOnClickListener {
                val selectedMerchant = itemsAdapter.currentList.find { it.isSelected }
                selectedMerchant?.let {
                    viewModel.saveMerchant(it)
                }
                viewModel.removeTerminal()
                findNavController().popBackStack()
            }
        }
    }

    private fun showLoading() {
        val seeMoreButton = SeeMoreListAdapter.SeeMoreItem(getString(R.string.see_more), true)
        seeMoreAdapter.submitList(listOf(seeMoreButton))
    }

    private fun hideSeeMore() {
        seeMoreAdapter.submitList(emptyList())
    }

    private fun showSeeMore() {
        val seeMoreButton = SeeMoreListAdapter.SeeMoreItem(getString(R.string.see_more), false)
        seeMoreAdapter.submitList(listOf(seeMoreButton))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}