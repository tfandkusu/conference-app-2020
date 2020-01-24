package io.github.droidkaigi.confsched2020.session.ui.item

import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.view.doOnPreDraw
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.xwray.groupie.Item
import com.xwray.groupie.databinding.BindableItem
import io.github.droidkaigi.confsched2020.ext.getThemeColor
import io.github.droidkaigi.confsched2020.model.Session
import io.github.droidkaigi.confsched2020.session.R
import io.github.droidkaigi.confsched2020.session.databinding.ItemSessionDetailDescriptionBinding

class SessionDetailDescriptionItem @AssistedInject constructor(
    @Assisted private val session: Session,
    @Assisted private val isShowFullText: Boolean,
    @Assisted onShowFullText: () -> Unit
) :
    BindableItem<ItemSessionDetailDescriptionBinding>() {

    companion object {
        private const val ELLIPSIS_LINE_COUNT = 6
    }

    private var showEllipsis = true

    private val transition = AutoTransition()

    init {
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                onShowFullText()
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
            }

            override fun onTransitionStart(transition: Transition) {
            }
        })
    }

    override fun getLayout() = R.layout.item_session_detail_description

    override fun isSameAs(other: Item<*>?): Boolean = other is SessionDetailDescriptionItem

    override fun bind(binding: ItemSessionDetailDescriptionBinding, position: Int) {
        val fullDescription = session.desc
        val textView = binding.sessionDescription
        textView.doOnPreDraw {
            textView.text = fullDescription
            // Return here if not more than the specified number of rows
            if (textView.lineCount <= ELLIPSIS_LINE_COUNT || !showEllipsis || isShowFullText)
                return@doOnPreDraw
            val lastLineStartPosition = textView.layout.getLineStart(ELLIPSIS_LINE_COUNT - 1)
            val context = textView.context
            val ellipsis = context.getString(R.string.ellipsis_label)
            val lastLineText = TextUtils.ellipsize(
                fullDescription.substring(lastLineStartPosition),
                textView.paint,
                textView.width - textView.paint.measureText(ellipsis),
                TextUtils.TruncateAt.END
            )
            val ellipsisColor = context.getThemeColor(R.attr.colorSecondary)
            val onClickListener = {
                TransitionManager.beginDelayedTransition(binding.itemRoot, transition)
                textView.text = fullDescription
                showEllipsis = !showEllipsis
            }
            val detailText = fullDescription.substring(0, lastLineStartPosition) + lastLineText
            val text = buildSpannedString {
                clickableSpan(
                    onClickListener,
                    {
                        append(detailText)
                        color(ellipsisColor) {
                            append(ellipsis)
                        }
                    }
                )
            }
            textView.setText(text, TextView.BufferType.SPANNABLE)
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun SpannableStringBuilder.clickableSpan(
        clickListener: () -> Unit,
        builderAction: SpannableStringBuilder.() -> Unit
    ) {
        inSpans(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    clickListener()
                }

                override fun updateDrawState(ds: TextPaint) {
                    // nothing
                }
            },
            builderAction
        )
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(
            session: Session,
            isShowFullText: Boolean,
            onShowFullText: () -> Unit
        ): SessionDetailDescriptionItem
    }
}
