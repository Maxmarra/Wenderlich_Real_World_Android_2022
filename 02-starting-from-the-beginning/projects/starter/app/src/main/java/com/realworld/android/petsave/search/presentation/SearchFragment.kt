package com.realworld.android.petsave.search.presentation

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.realworld.android.petsave.R
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.presentation.AnimalsAdapter
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import okio.IOException
import retrofit2.HttpException

@AndroidEntryPoint
class SearchFragment: Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentSearchBinding? = null

    companion object {
        private const val ITEMS_PER_ROW = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        // TODO: call prepareForSearch method here
    }

    private fun setupUI() {
        val adapter = createAdapter()
        setupRecyclerView(adapter)
        observeViewStateUpdates(adapter)
    }

    private fun createAdapter(): AnimalsAdapter {
        return AnimalsAdapter()
    }

    private fun setupRecyclerView(searchAdapter: AnimalsAdapter) {
        binding.searchRecyclerView.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(requireContext(), ITEMS_PER_ROW)
            setHasFixedSize(true)
        }
    }

    private fun observeViewStateUpdates(searchAdapter: AnimalsAdapter) {
        // TODO: observe state updates here
    }

    private fun handleFailures(failure: Event<Throwable>?) {
        val unhandledFailure = failure?.getContentIfNotHandled() ?: return

        handleThrowable(unhandledFailure)
    }

    private fun handleThrowable(exception: Throwable) {
        val fallbackMessage = getString(R.string.an_error_occurred)
        val snackbarMessage = when (exception) {
            is NoMoreAnimalsException -> exception.message ?: fallbackMessage
            is IOException, is HttpException -> fallbackMessage
            else -> ""
        }

        if (snackbarMessage.isNotEmpty()) {
            Snackbar.make(requireView(), snackbarMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
