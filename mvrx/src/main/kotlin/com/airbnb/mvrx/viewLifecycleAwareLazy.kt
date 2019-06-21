package com.airbnb.mvrx

import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.Serializable

/**
 * This was copied from SynchronizedLazyImpl but modified to automatically initialize in onAttachedToWindow.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("Detekt.ClassNaming")
class viewLifecycleAwareLazy<out T>(private val view: View, initializer: (AppCompatActivity, Fragment) -> T) : Lazy<T>, Serializable {
    private var initializer: ((AppCompatActivity, Fragment) -> T)? = initializer

    @Volatile
    @SuppressWarnings("Detekt.VariableNaming")
    private var _value: Any? = UninitializedValue
    // final field is required to enable safe publication of constructed instance
    private val lock = this

    init {
        if (view.isAttachedToWindow) {
            if (!isInitialized()) value
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View) {}

                override fun onViewAttachedToWindow(v: View) {
                    if (!isInitialized()) value
                    v.removeOnAttachStateChangeListener(this)
                }
            })
        }
    }

    @Suppress("LocalVariableName")
    override val value: T
        get() {
            @SuppressWarnings("Detekt.VariableNaming")
            val _v1 = _value
            if (_v1 !== UninitializedValue) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                @SuppressWarnings("Detekt.VariableNaming")
                val _v2 = _value
                if (_v2 !== UninitializedValue) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    val activity = view.context as AppCompatActivity
                    val fragment = view.fragment()
                    val typedValue = initializer!!(activity, fragment)
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UninitializedValue

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    fun View.fragment(): Fragment {
        val activity = context as AppCompatActivity
        val viewFragmentMap = mutableMapOf<View, Fragment>()
        activity.supportFragmentManager.fragments.forEach { it.collectViewMap(viewFragmentMap) }

        var view: View? = this
        while (view != null) {
            val fragment = viewFragmentMap[view]
            if (fragment != null) {
                return fragment
            }
            view = view.parent as? ViewGroup
        }
        throw java.lang.IllegalStateException("Unable to find a Fragment in any of the parents of $this.")
    }

    fun Fragment.collectViewMap(map: MutableMap<View, Fragment>) {
        view?.let { view -> map[view] = this }
        childFragmentManager.fragments.forEach { it.collectViewMap(map) }
    }
}