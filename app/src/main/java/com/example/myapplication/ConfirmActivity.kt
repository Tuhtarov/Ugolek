package com.example.myapplication

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.common.management.ConfirmActivityData
import com.example.myapplication.databinding.ActivityConfirmBinding
import com.example.myapplication.smscru.RetrofitSmsApi
import com.example.myapplication.smscru.SmsModel
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.SingleObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.sql.Time
import java.util.concurrent.TimeUnit
import kotlin.math.log
import kotlin.random.Random

class ConfirmActivity : AppCompatActivity() {
    private lateinit var dialog: Dialog
    private lateinit var b: ActivityConfirmBinding
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var compositeDisposableRx2: io.reactivex.disposables.CompositeDisposable
    private val TAG = ConfirmActivity::class.java.simpleName
    lateinit var retrofitSmsApi: RetrofitSmsApi
    var codeSms = ""

    private fun Disposable.disposeAtTheEnd() {
        compositeDisposable.add(this)
    }

    private fun io.reactivex.disposables.Disposable.disposeAtTheEnd() {
        compositeDisposableRx2.add(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityConfirmBinding.inflate(layoutInflater)
        compositeDisposable = CompositeDisposable()
        compositeDisposableRx2 = io.reactivex.disposables.CompositeDisposable()
        retrofitSmsApi = RetrofitSmsApi()
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
        b.fieldAllPriceOrder.append(" руб.")

        b.fieldPhoneOrder.textChanges()
            .map { text -> text.isNotEmpty() }
            .debounce(150, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it) {
                    //TODO да, тут большой флуд кодом, но главное работает без эксепшинов))0 0))))
                    if (b.fieldPhoneOrder.text.isNotEmpty()) {
                        b.fieldPhoneOrder.text.trim()
                        if (b.fieldPhoneOrder.text?.toString()?.get(0) == '+') {
                            if (b.fieldPhoneOrder.text.length > 1) {
                                if (b.fieldPhoneOrder.text?.toString()?.get(1) == '7') {
                                } else {
                                    b.fieldPhoneOrder.text.replace(1, 2, "7")
                                }
                            }
                        } else {
                            b.fieldPhoneOrder.text.clear()
                            b.fieldPhoneOrder.text.append("+7")
                        }

                        if (b.fieldPhoneOrder.text.length > 2) {
                            b.fieldPhoneOrder.text.takeLast(b.fieldPhoneOrder.text.length.toInt() - 1)
                                .forEachIndexed { index, c ->
                                    if (ConfirmActivityData().numbersPhone.contains(c)) {
                                    } else {
                                        var text = b.fieldPhoneOrder.text.toString()
                                        text = text.replace(c, ' ').trim()
                                        if (b.fieldPhoneOrder.text.isNotEmpty()) {
                                            b.fieldPhoneOrder.setText(text)
                                            b.fieldPhoneOrder.setSelection(b.fieldPhoneOrder.text.length)
                                        } else {
                                            b.fieldPhoneOrder.text.clear()
                                            b.fieldPhoneOrder.text.append("+7")
                                        }
                                    }
                                }
                        }
                    } else {
                        b.fieldPhoneOrder.text.clear()
                        b.fieldPhoneOrder.setText("+7")
                    }

                    b.btnConfirmOrder.isEnabled = b.fieldPhoneOrder.text.length == 12
                }
            }, {
                Log.e(TAG, "${it.localizedMessage}")
            }).disposeAtTheEnd()

        b.btnConfirmOrder.setOnClickListener {
            val codeConfirm = generateCodeSms()
            showDialogConfirmSms(codeConfirm)
        }

    }

    private fun generateCodeSms(): String {
        codeSms = ""
        codeSms = (0..9).random().toString() + (0..9).random().toString() + (0..9).random()
            .toString() + (0..9).random().toString()
        Log.d("tag", codeSms)
        return codeSms
    }


    /*=============================================================================================*/

    private fun showDialogConfirmSms(codeConfirm: String) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_phone)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fieldConfirmCode = dialog.findViewById<EditText>(R.id.field_inputCodeSms)

//        retrofitSmsApi.sendMessageApi(phone.text.toString(), "Код подтверждения: $codeConfirm")
//            .subscribeOn(io.reactivex.schedulers.Schedulers.computation())
//            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
//            .subscribe(object : SingleObserver<SmsModel> {
//                override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
//                    Log.d("SmsApi", "подписка выполнена")
//                }
//
//                override fun onSuccess(t: SmsModel) {
//                    Log.d("SmsApi", "${t.error.toString()}")
//                }
//
//                override fun onError(e: Throwable) {
//                    Log.e("SmsApi", "ошибка -> ${e.localizedMessage}")
//                    Toast.makeText(
//                        this@ConfirmActivity,
//                        "ошибка ${e.localizedMessage}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//
//            })
//        sendMessage(b.fieldPhoneOrder.text.toString(), codeConfirm)
        dialog.show()

        var flag: Boolean? = null

        fieldConfirmCode.textChanges()
            .map { text -> (text.length == 4) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .debounce(350, TimeUnit.MILLISECONDS)
            .subscribe({
                if (it) {
                    if (codeSms == fieldConfirmCode.text.toString()) {
                        Log.d("tagAccept", "код подтверждён")
                        flag = true
                        dialog.dismiss()
                        //TODO!!!!!!!!!!!!!!!!!!!!!!!
                    } else {
                        Log.e("tagCanceled", "код неверный")
                        flag = false
                        dialog.dismiss()
                        //TODO!!!!!!!!!!!!!!!!!!!!!!!
                    }
                }
            }, {
                Log.e("tagConfirm", "ошибка ${it.localizedMessage}")
            }).disposeAtTheEnd()

        dialog.setOnCancelListener {
            dialog.dismiss()
            Toast.makeText(this, "Подтверждение пока что в разработке", Toast.LENGTH_SHORT).show()
        }

        dialog.setOnDismissListener {
            flag?.let {
                if (it) showDialogCodeConfirmed() else showDialogCodeCanceled()
            }
        }
    }


    private fun showDialogCodeConfirmed() {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_code_confirmed)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val buttonOk = dialog.findViewById<Button>(R.id.btn_code_ok)

        buttonOk?.setOnClickListener {
            Toast.makeText(this, "Ну ок так ок", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

    }

    private fun showDialogCodeCanceled() {
        var dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_code_canceled)
        dialog.show()

        val buttonRepeat = dialog.findViewById<Button>(R.id.btn_code_repeat)
        val buttonCancel = dialog.findViewById<Button>(R.id.btn_code_cancel)

        buttonRepeat?.setOnClickListener {
            dialog.dismiss()
            showDialogConfirmSms(generateCodeSms())
        }

        buttonCancel?.setOnClickListener {
            dialog.dismiss()
            b.fieldPhoneOrder.text.clear()
        }


    }

//    fun sendMessage(phoneNumber: String, codeConfirm: String){
//        retrofitSmsApi.sendMessageApi(phoneNumber, "Код подтверждения: $codeConfirm")
//            .subscribeOn(io.reactivex.schedulers.Schedulers.computation())
//            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
//            .subscribe(object : SingleObserver<SmsModel> {
//                override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
//                    Log.d("SmsApi", "подписка выполнена")
//                }
//
//                override fun onSuccess(t: SmsModel) {
//                    Log.d("SmsApi", "${t.error.toString()}")
//                }
//
//                override fun onError(e: Throwable) {
//                    Log.e("SmsApi", "ошибка -> ${e.localizedMessage}")
//                    Toast.makeText(
//                        this@ConfirmActivity,
//                        "ошибка ${e.localizedMessage}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//
//            })
//    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

}