package com.example.myapplication

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.databinding.ActivityConfirmBinding

class ConfirmActivity : AppCompatActivity() {
    private lateinit var dialog: Dialog
    private lateinit var b: ActivityConfirmBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityConfirmBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.fieldProviderOrder.text = intent.getStringExtra("provider")
        b.fieldMarkCoalOrder.text = intent.getStringExtra("coal")
        b.fieldPriceCoalOrder.text = intent.getStringExtra("priceCoal")
        b.fieldPriceCoalOrder.append(" руб.")
        b.fieldAddressDeliveryOrder.text = intent.getStringExtra("addressDelivery")
        b.fieldRequiredMassOrder.text = intent.getStringExtra("requiredMass")
        b.fieldRequiredMassOrder.append(" тон")
        b.fieldDistanceOrder.text = intent.getStringExtra("distance")
        b.fieldDeliveryOrder.text = intent.getStringExtra("priceDelivery")
        b.fieldAllPriceOrder.text = intent.getStringExtra("allPrice")

        b.fieldPhoneOrder.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                b.btnConfirmOrder.isEnabled = b.fieldPhoneOrder.text.toString() != ""

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                b.btnConfirmOrder.isEnabled = b.fieldPhoneOrder.text.toString() != ""
            }

        })

        b.btnConfirmOrder.setOnClickListener {
            showDialogConfirmSms()
        }

    }


    private fun showDialogConfirmSms(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_phone)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fieldCodeFromSms = dialog.findViewById<EditText>(R.id.field_inputCodeSms)

        dialog.setOnCancelListener {
            dialog.dismiss()
            Toast.makeText(this, "Подтверждение пока что в разработке", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }


}