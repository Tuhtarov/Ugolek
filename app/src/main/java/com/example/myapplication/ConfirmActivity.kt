package com.example.myapplication

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.common.management.ConfirmActivityData
import com.example.myapplication.databinding.ActivityConfirmBinding
import com.example.myapplication.smscru.RetrofitSmsApi
import com.example.myapplication.smscru.SmsModel
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ConfirmActivity : AppCompatActivity() {
    private lateinit var b: ActivityConfirmBinding
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var compositeDisposableRx2: io.reactivex.disposables.CompositeDisposable
    lateinit var retrofitSmsApi: RetrofitSmsApi

    var orderIsValid: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityConfirmBinding.inflate(layoutInflater)
        compositeDisposable = CompositeDisposable()
        compositeDisposableRx2 = io.reactivex.disposables.CompositeDisposable()
        retrofitSmsApi = RetrofitSmsApi()
        setContentView(b.root)

        b.fieldProviderOrder.setText(intent.getStringExtra("provider"))
        b.fieldMarkCoalOrder.setText(intent.getStringExtra("coal"))
        b.fieldPriceCoalOrder.setText(intent.getStringExtra("priceCoal"))
        b.fieldPriceCoalOrder.append(" руб.")
        b.fieldAddressDeliveryOrder.setText(intent.getStringExtra("addressDelivery"))
        b.fieldRequiredMassOrder.setText(intent.getStringExtra("requiredMass"))
        b.fieldRequiredMassOrder.append(" тон")
        b.fieldDistanceOrder.setText(intent.getStringExtra("distance"))
        b.fieldDeliveryOrder.setText(intent.getStringExtra("priceDelivery"))
        b.fieldAllPriceOrder.setText(intent.getStringExtra("allPrice"))
        b.fieldAllPriceOrder.append(" руб.")

        b.fieldPhoneOrder.textChanges()
            .map { text -> text.isNotEmpty() }
            .debounce(150, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it) {
                    //TODO флуд
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
                            b.fieldPhoneOrder.text.takeLast(b.fieldPhoneOrder.text.length - 1)
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
                        b.btnConfirmOrder.isEnabled = b.fieldPhoneOrder.text.length == 12
                    }

                    b.btnConfirmOrder.isEnabled = b.fieldPhoneOrder.text.length == 12
                }
            }, {
                Log.e(TAG, it.localizedMessage)
            }).disposeAtTheEnd()

        b.btnConfirmOrder.setOnClickListener {
            val codeConfirm = generateCodeSms()
            sendCodeConfirm(b.fieldPhoneOrder.text.toString(), codeConfirm)
            showDialogConfirmSms(codeConfirm)
        }

    }

    /*========================================DIALOGS && RETROFIT && RXJAVA==============================================*/

    private fun showDialogConfirmSms(codeConfirm: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_phone)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val fieldConfirmCode = dialog.findViewById<EditText>(R.id.field_inputCodeSms)
        dialog.show()

        var validCode: Boolean? = null
        fieldConfirmCode.textChanges()
            .map { text -> (text.length == 4) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .debounce(350, TimeUnit.MILLISECONDS)
            .subscribe({
                if (it) {
                    if (codeSms == fieldConfirmCode.text.toString()) {
                        Log.d("tagAccept", "код подтверждён")
                        validCode = true
                        dialog.dismiss()
                        //TODO!!!!!!!!!!!!!!!!!!!!!!!
                    } else {
                        Log.e("tagCanceled", "код неверный")
                        validCode = false
                        dialog.dismiss()
                        //TODO!!!!!!!!!!!!!!!!!!!!!!!
                    }
                }
            }, {
                Log.e("tagConfirm", "ошибка ${it.localizedMessage}")
            }).disposeAtTheEnd()

        dialog.setOnDismissListener {
            validCode?.let {
                if (it) showDialogCodeConfirmed() else showDialogCodeCanceled()
            }
        }
    }


    private fun showDialogCodeConfirmed() {
        sendSmsOrder() /* при подтверждённом номере, отправляются введённые данные оператору угля */

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_code_confirmed)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val buttonOk = dialog.findViewById<Button>(R.id.btn_code_ok)
        dialog.show()

        buttonOk.setOnClickListener {
            startTheEnd(dialog)
        }
        dialog.setOnDismissListener {
            startTheEnd(dialog)
        }

    }

    private fun startTheEnd(dialog: Dialog){
        if(orderIsValid != false){
            Toast.makeText(this, "Заказ отправлен на обработку!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Заказ не был отправлен на обработку.", Toast.LENGTH_LONG).show()
        }
        val intent = Intent(this, MainActivity::class.java)
        dialog.dismiss()
        finish()
        startActivity(intent)
    }


    private fun showDialogCodeCanceled() {
        val dialogCodeCanceled = Dialog(this)
        dialogCodeCanceled.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogCodeCanceled.setContentView(R.layout.dialog_code_canceled)

        val buttonRepeat = dialogCodeCanceled.findViewById<Button>(R.id.btn_code_repeat)
        val buttonCancel = dialogCodeCanceled.findViewById<Button>(R.id.btn_code_cancel)

        dialogCodeCanceled.show()

        createBlockForSendCode(buttonRepeat)

        buttonRepeat.setOnClickListener {
            dialogCodeCanceled.dismiss()
            showDialogConfirmSms(generateCodeSms())
        }

        buttonCancel.setOnClickListener {
            dialogCodeCanceled.dismiss()
        }

    }

    private fun sendCodeConfirm(phoneNumber: String, codeConfirm: String){
        retrofitSmsApi.sendMessageApi(phoneNumber, "Код подтверждения: $codeConfirm")
            .subscribeOn(io.reactivex.schedulers.Schedulers.computation())
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<SmsModel> {
                override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
                    Log.d("SMS", "подписка выполнена")
                }

                override fun onSuccess(t: SmsModel) {
                    if(t.cnt != null){
                        Log.d(TAG, "sendCodeConfirm -> сообщение отправлено успешно, кол-во частей смс = ${t.cnt.toString()}")
                    }
                    if(t.error != null){
                        orderIsValid = false
                        Toast.makeText(this@ConfirmActivity,
                            "Возникла ошибка в работе с сервером: ${t.error.toString() + " | id sms -> " + t.id.toString()} ",
                            Toast.LENGTH_LONG).show()
                        Log.e(TAG, "sendCodeConfirm -> ${t.error.toString() + " | " + t.error_code.toString() + " id sms -> " + t.id.toString()}")
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e("SMS", "ошибка -> ${e.localizedMessage}")
                    Toast.makeText(
                        this@ConfirmActivity,
                        "ошибка ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            })
    }

    private fun sendSmsOrder() {
        retrofitSmsApi.sendMessageApi(
            Companion.PHONE_OPERATOR_COAL,
            "Поставщик: ${b.fieldProviderOrder.text} \n" +
                    "Марка угля: ${b.fieldMarkCoalOrder.text} \n" +
                    "Масса: ${b.fieldRequiredMassOrder.text} \n" +
                    "Адрес: ${b.fieldAddressDeliveryOrder.text} \n" +
                    "Телефон заказчика: ${b.fieldPhoneOrder.text}"
        )
            .subscribeOn(io.reactivex.schedulers.Schedulers.computation())
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<SmsModel> {
                override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
                    Log.d(SMS, "подписка выполнена")
                }

                override fun onSuccess(t: SmsModel) {
                    if(t.cnt != null){
                        Log.d(TAG, "sendCodeConfirm -> сообщение отправлено успешно, кол-во частей смс = ${t.cnt.toString()}")
                    }
                    if(t.error != null){
                        orderIsValid = false
                        Toast.makeText(this@ConfirmActivity,
                            "Возникла ошибка в работе с сервером: ${t.error.toString() + " | id sms -> " + t.id.toString()} ",
                            Toast.LENGTH_LONG).show()
                        Log.e(TAG, "sendSmsOrder -> ${t.error.toString() + " | " + t.error_code.toString() + " id sms -> " + t.id.toString()}")
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e(SMS, "ошибка -> ${e.localizedMessage}")
                    Toast.makeText(
                        this@ConfirmActivity,
                        "ошибка ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            })
    }

    /*======================================UTILS==========================================*/

    private fun generateCodeSms(): String {
        codeSms = ""
        codeSms = (0..9).random().toString() + (0..9).random().toString() + (0..9).random()
            .toString() + (0..9).random().toString()
        Log.d("tag", codeSms)
        return codeSms
    }


    private fun createBlockForSendCode(btn: Button) {
        createTicker()
            .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe({
                b.btnConfirmOrder.isEnabled = false
                b.fieldPhoneOrder.isEnabled = false
                btn.isEnabled = false
                btn.text = it.toString()
                b.btnConfirmOrder.text = it.toString()

                if(btn.text == "0") {
                    btn.text = "Повторить"
                    b.btnConfirmOrder.text = getString(R.string.symbol_check)
                    b.btnConfirmOrder.isEnabled = true
                    b.fieldPhoneOrder.isEnabled = true
                    btn.isEnabled = true
                }
            }, {
                Log.e(TAG, it.localizedMessage)
            }, {
            }).disposeAtTheEnd()
    }

    private fun createTicker(): Observable<Int> {
        return Observable.create { second ->
            for (i in 30 downTo 0) {
                Thread.sleep(1000)
                second.onNext(i)
            }
        }
    }


    /* Очищение потоков */
    private fun Disposable.disposeAtTheEnd() {
        compositeDisposable.add(this)
    }

    private fun io.reactivex.disposables.Disposable.disposeAtTheEnd() {
        compositeDisposableRx2.add(this)
    }

    override fun onDestroy() {
        Handler().postDelayed({
            compositeDisposable.dispose()
            compositeDisposableRx2.dispose()
            Log.d(TAG, "dispose? = ${compositeDisposableRx2.isDisposed}")
        }, 30000)
        super.onDestroy()
    }

    //Константы
    companion object {
        const val PHONE_OPERATOR_COAL = "+79628003000"
        private val TAG = ConfirmActivity::class.java.simpleName
        private val SMS = "SmsApi"
        var codeSms = ""
    }
}