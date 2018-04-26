package com.ruanmeng.village_arrival

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.utils.NameLengthFilter
import kotlinx.android.synthetic.main.activity_withdraw.*
import java.text.DecimalFormat

class WithdrawActivity : BaseActivity() {

    private var withdrawSum = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)
        init_title("申请提现", "说明")
    }

    override fun init_title() {
        super.init_title()
        withdrawSum = intent.getStringExtra("balance").toDouble()
        withdraw_money.text = DecimalFormat(",##0.00").format(withdrawSum)

        bt_submit.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
        bt_submit.isClickable = false

        et_name.filters = arrayOf<InputFilter>(NameLengthFilter(10))
        et_count.addTextChangedListener(this@WithdrawActivity)
        et_card.addTextChangedListener(this@WithdrawActivity)
        et_name.addTextChangedListener(this@WithdrawActivity)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (et_count.text.isNotBlank()
                && et_card.text.isNotBlank()
                && et_name.text.isNotBlank()) {
            bt_submit.setBackgroundResource(R.drawable.rec_bg_blue_shade)
            bt_submit.isClickable = true
        } else {
            bt_submit.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
            bt_submit.isClickable = false
        }

        if (et_count.isFocused) {
            if ("." == s.toString()) {
                et_count.setText("0.")
                et_count.setSelection(et_count.text.length) //设置光标的位置
                return
            }

            val inputCount = s.toString().toDouble()
            if (inputCount > withdrawSum) {
                et_count.setText(String.format("%.2f", withdrawSum))
                et_count.setSelection(et_count.text.length)
            } else {
                if (s.length > String.format("%.2f", withdrawSum).length) {
                    et_count.setText(String.format("%.2f", withdrawSum))
                    et_count.setSelection(et_count.text.length)
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (et_count.isFocused) {
            val temp = s.toString()
            val posDot = temp.indexOf(".")
            if (posDot < 0) {
                if (temp.length > 7) s.delete(7, 8)
            } else {
                if (temp.length - posDot - 1 > 2) s.delete(posDot + 3, posDot + 4)
            }
        }
    }
}
