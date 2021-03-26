package com.example.myapplication

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.databinding.ActivityConfirmBinding
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.log

class ConfirmActivity : AppCompatActivity() {
    private lateinit var dialog: Dialog
    private lateinit var b: ActivityConfirmBinding
    private lateinit var compositeDisposable: CompositeDisposable

    private fun Disposable.disposeAtTheEnd() {
        compositeDisposable.add(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityConfirmBinding.inflate(layoutInflater)
        compositeDisposable = CompositeDisposable()
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
        b.fieldAllPriceOrder.append(" тон")


        b.fieldPhoneOrder.textChanges()
            .map { text -> (text.isNotEmpty())
            }
            .subscribe({
                if(it){
                    if(b.fieldPhoneOrder.text.first() != '+'){
                        b.fieldPhoneOrder.text.clear()
                        b.fieldPhoneOrder.append("+7")
                    }
                }
            }, {
                Log.e("error", "${it.localizedMessage}")
            }).disposeAtTheEnd()


        //                if (it[0].toString() == "8") {
//                    b.fieldPhoneOrder.setText("+7")8
//                } else if (it[0].toString() == "+") {
//                    b.fieldPhoneOrder.setText("+7")
//                } else {
//                    b.fieldPhoneOrder.setText("")
//                }

//        b.fieldPhoneOrder.addTextChangedListener(object : TextWatcher {
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                b.btnConfirmOrder.isEnabled = b.fieldPhoneOrder.text.toString() != ""
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                b.btnConfirmOrder.isEnabled = b.fieldPhoneOrder.text.toString() != ""
//            }
//
//        })

        b.btnConfirmOrder.setOnClickListener {
            showDialogConfirmSms()
        }

    }


    /*=============================================================================================*/

    private fun plusSeven() {
        b.fieldPhoneOrder.append("+7")
    }

    private fun showDialogConfirmSms() {
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

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

}