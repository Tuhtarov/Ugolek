package com.example.myapplication

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_confirm.*
import kotlinx.android.synthetic.main.dialog_confirm_phone.*

class ConfirmActivity : AppCompatActivity() {
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

        field_providerOrder.text = intent.getStringExtra("provider")
        field_markCoalOrder.text = intent.getStringExtra("coal")
        field_priceCoalOrder.text = intent.getStringExtra("priceCoal")
        field_addressDeliveryOrder.text = intent.getStringExtra("addressDelivery")
        field_requiredMassOrder.text = intent.getStringExtra("requiredMass")

        field_phoneOrder.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                btn_confirmOrder.isEnabled = field_phoneOrder.text.toString() != ""

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                btn_confirmOrder.isEnabled = field_phoneOrder.text.toString() != ""
            }

        })

        btn_confirmOrder.setOnClickListener {
            showDialogConfirmSms()
        }

    }


    private fun showDialogConfirmSms(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_phone)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fieldCodeFromSms = dialog.field_inputCodeSms

        dialog.setOnCancelListener {
            dialog.dismiss()
            Toast.makeText(this, "Подтверждение пока что в разработке", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }


}