package com.studieresan.studs.happenings

import HappeningsQuery
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.studieresan.studs.R
import com.studieresan.studs.happenings.adapters.HappeningRecyclerViewAdapter
import com.studieresan.studs.happenings.viewmodels.HappeningsViewModel

class HappeningsListFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_happening_list, container, false)

        val viewModel = ViewModelProviders.of(requireActivity()).get(HappeningsViewModel::class.java)

        viewModel.happenings.observe(viewLifecycleOwner, Observer { t ->
            if (view is RecyclerView) {

                with(view) {
                    layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }
                    if (t != null) adapter = HappeningRecyclerViewAdapter(t, this@HappeningsListFragment)

                }
            }
        })

        return view
    }


    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

    }
}
