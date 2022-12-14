package com.xplore.paymobile.ui.merchantselection.search.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearent.idtech.android.wrapper.ui.util.MarginItemDecoration
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentMerchantSearchBinding
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.ui.merchantselection.MerchantSelectSharedViewModel
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import com.xplore.paymobile.util.bundle
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class TerminalSearchFragment : BaseFragment() {

    private var _binding: FragmentMerchantSearchBinding? = null
    private val binding get() = _binding!!

    override val hasBottomNavigation: Boolean = false

    private val viewModel by viewModels<TerminalSearchViewModel>()
    private val sharedViewModel by activityViewModels<MerchantSelectSharedViewModel>()

    private val merchantId by bundle<String>()

    private var adapter = MerchantsListAdapter(onItemClicked = {

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
        Timber.d("TESTEST merchant id $merchantId")
        setupTitle()
        setupSearchBar()
        setupList()
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
                if (count >= 3) {
                    viewModel.searchForQuery(text.toString())
                }
            }
        }
    }

    private fun setupList() {
        binding.apply {
            itemsList.adapter = adapter
            itemsList.layoutManager = LinearLayoutManager(requireContext())
            itemsList.addItemDecoration(MarginItemDecoration(8, 0))
            adapter.submitList(mockTerminalList())
        }
    }

    private fun mockTerminalList(): MutableList<MerchantsListAdapter.MerchantItem> {
        val list = mutableListOf<MerchantsListAdapter.MerchantItem>()
        for (index in 1..45) {
            list.add(MerchantsListAdapter.MerchantItem("Terminal #$index"))
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}