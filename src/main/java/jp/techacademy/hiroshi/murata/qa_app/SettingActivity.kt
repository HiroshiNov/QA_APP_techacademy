package jp.techacademy.hiroshi.murata.qa_app

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity: AppCompatActivity(){

    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(saveInstanceState: Bundle?){
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_setting)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY,"")
        nameText.setText(name)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        //Initialize UI
        title = "設定"

        changeButton.setOnClickListener{v ->
            //Close when Keyboard is displayed
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)

            val user = FirebaseAuth.getInstance().currentUser

            if(user == null){
                //DO nothing when logout
                Snackbar.make(v,"ログインしていません",Snackbar.LENGTH_LONG).show()
            }else{
                //Store the displayed name into Firebase
                val name = nameText.text.toString()
                val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String,String>()
                data["name"] = name
                userRef.setValue(data)

                //Store the dispkayed name into Preference
                val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp.edit()
                editor.putString(NameKEY,name)
                editor.commit()

                Snackbar.make(v,"ログアウトしました",Snackbar.LENGTH_LONG).show()

            }
        }

        logoutButton.setOnClickListener{ v ->
            FirebaseAuth.getInstance().signOut()
            nameText.setText("")
            Snackbar.make(v,"ログアウトしました",Snackbar.LENGTH_LONG).show()
        }
    }
}