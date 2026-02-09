package dev.answer.material.view

/**
 *
 * @author AnswerDev
 * @date 2026/2/9 20:25
 * @description Fragment
 */
abstract class Fragment {

    lateinit var root: View

    protected abstract fun onCreateView(): View

    open fun onAttach() {}
    open fun onDetach() {}

    internal fun performCreateView(): View {
        root = onCreateView()
        onAttach()
        return root
    }

    internal fun performDestroyView() {
        onDetach()
    }
}