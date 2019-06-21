package com.airbnb.mvrx.sample.features.statefulview

import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxEpoxyController
import com.airbnb.mvrx.sample.core.simpleController

class StatefulViewFragment : BaseFragment() {
    override fun epoxyController() = simpleController {
        for (i in 1..100) {
            statefulBasicRow {
                id("row_$i")
                title("Row $i")
            }
        }
    }
}