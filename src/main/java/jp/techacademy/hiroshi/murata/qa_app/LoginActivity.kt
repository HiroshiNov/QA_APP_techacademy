package jp.techacademy.hiroshi.murata.qa_app

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseError
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference

    private var mIsCreateAccount = false

    override fun onCreate(saveInstanceState: Bundle?){
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_login)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        mAuth = FirebaseAuth.getInstance()

        mCreateAccountListener = OnCompleteListener { task ->
            if ( task.isSuccessful) {
                //When succeeded ..
                //Do login
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            }else {
                //When failed
                //Display errors
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view,"アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()

                //Hide progressbar
                progressBar.visibility = View.GONE
            }
        }

        //Listener after
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful){
                //When succeeded
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)

                if(mIsCreateAccount){
                    //Save into Firebase
                    val name = nameText.text.toString()

                    val data = HashMap<String, String>()
                    data["name"] = name
                    userRef.setValue(data)

                    saveName(name)
                }else{
                    userRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["name"] as String)
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {
                        }
                    })
                }

                progressBar.visibility = View.GONE

                finish()
            }else{
                //When failed

                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()

                progressBar.visibility = View.GONE
            }
        }

        title = "ログイン"

        createButton.setOnClickListener{ v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            if (email.length != 0 && password.length >= 6 && name.length != 0){
                mIsCreateAccount = true

                createAccount(email, password)
            }else{
                Snackbar.make(v,"正しく入力してください",Snackbar.LENGTH_LONG).show()
            }
        }
        loginButton.setOnClickListener{ v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6){
                mIsCreateAccount = false

                login(email, password)
            }else{
                Snackbar.make(v,"正しく入力してください",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun createAccount(email: String, password: String){
        //Display progressbar
        progressBar.visibility = View.VISIBLE
        //Create new account
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)

    }

    private fun login(email: String,password: String){
        //Display progressbar
        progressBar.visibility = View.VISIBLE
        //Do login
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    private fun saveName(name: String){
        //Save to Preference
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY,name)
        editor.commit()
    }


}