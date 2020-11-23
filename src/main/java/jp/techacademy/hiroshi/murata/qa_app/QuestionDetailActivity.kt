package jp.techacademy.hiroshi.murata.qa_app

import android.R.attr.key
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.view.*
import org.w3c.dom.Text
import kotlin.math.log
import android.support.v4.app.SupportActivity
import android.support.v4.app.SupportActivity.ExtraData
import android.support.v4.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name
import android.provider.ContactsContract


class QuestionDetailActivity : AppCompatActivity(){
    
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private var keyOfCurrentquestion: String? = null
    private val listOfFavorites = arrayListOf<String>()
    private var listForCheck = arrayListOf<String>()

    private val mEventListener = object : ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot,s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers){
                if(answerUid == answer.answerUid){
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val extras = intent.extras
        mQuestion = extras.get("question") as Question
        val mGenre: Int = extras.getInt("genre")
        val mPosition : Int = extras.getInt("position")
        Log.d("id","$mGenre")
        Log.d("id","$mPosition")

        title = mQuestion.title
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener{
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null){
                Log.d("user is ", "null")
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }else{
                Log.d("user is ", "logedin")
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question",mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
            AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


        //check the authentification
        val user = FirebaseAuth.getInstance().currentUser

        val listOfQuestions = arrayListOf<String>()
        val listOfQData = arrayListOf<Any?>()
        var dataForSave : Any? = null
        //get the key of currentquestion
        val currentQuesiton = dataBaseReference.child(ContentsPATH).child(mGenre.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    for(ds in dataSnapshot.children){
                        var key = ds.key
                        var values = ds.value
                        Log.e("ds","$values")
                        listOfQuestions.add(key!!)
                        listOfQData.add(values!!)
                    }
                    keyOfCurrentquestion = listOfQuestions[mPosition]
                    dataForSave = listOfQData[mPosition]
                    Log.e("ds","$dataForSave")
                    Log.d("Question key is:", "$keyOfCurrentquestion")
                }
            })


        val currentFavoriteQuestions = dataBaseReference.child(FavoritesPATH).child(user!!.uid).addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for(ds in dataSnapshot.children){
                        var key = ds.key
                        listOfFavorites.add((key as String?)!!)
                        Log.d("Favorites is :", "$key")
                    }
                    initializeDisplayOfFavoriteText(listOfFavorites,keyOfCurrentquestion)
                }
            }
        )

        if(listOfFavorites != null && keyOfCurrentquestion != null){
        }

        favTextView.setOnClickListener {
            Log.d("Event", "Tapped")
            Log.d("listoffav","$listOfFavorites")
            Log.d("key","$keyOfCurrentquestion")
            Log.d("check","${listOfFavorites.contains(keyOfCurrentquestion)}")

            if (listOfFavorites.contains(keyOfCurrentquestion)){
                listOfFavorites.clear()
                dataBaseReference.child(FavoritesPATH).child(user!!.uid).child(keyOfCurrentquestion!!).removeValue()
                favTextView.setBackgroundResource(R.drawable.default_favorite_icon)
            }else{
                dataBaseReference.child(FavoritesPATH).child(user!!.uid).child(keyOfCurrentquestion!!).setValue(dataForSave)
                favTextView.setBackgroundResource(R.drawable.selected_favorite_icon)
            }
        }
    }

    fun initializeDisplayOfFavoriteText(listOfFavorites: ArrayList<String>, keyOfCurrentquestion: String?){
        if (listOfFavorites.contains(keyOfCurrentquestion)){
            favTextView.setBackgroundResource(R.drawable.selected_favorite_icon)
        }else{
            favTextView.setBackgroundResource(R.drawable.default_favorite_icon)
        }
    }
}

