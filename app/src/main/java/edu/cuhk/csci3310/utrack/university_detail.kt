package edu.cuhk.csci3310.utrack

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_university_detail.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class university_detail : AppCompatActivity() {
    val cal = Calendar.getInstance()
    lateinit var firebaseStore: FirebaseFirestore
    lateinit var user: FirebaseUser
    lateinit var id: String
    lateinit var UniTitle: EditText
    lateinit var UniProg: EditText
    lateinit var date_pickerView: TextView
    lateinit var UniStatus: Spinner
    lateinit var UniTransType: Spinner
    lateinit var UniTransStatus: Spinner
    lateinit var GREStatus: Spinner
    lateinit var TOEFLStatus: Spinner
    lateinit var Rec1Name: EditText
    lateinit var Rec2Name: EditText
    lateinit var Rec3Name: EditText
    lateinit var Rec1Status: Spinner
    lateinit var Rec2Status: Spinner
    lateinit var Rec3Status: Spinner
    lateinit var Notes: EditText

    lateinit var DetailCard: CardView
    lateinit var LORCard: CardView
    lateinit var NotesCard: CardView

    lateinit var TransStatusArray: Array<String>
    lateinit var UniStatusArray: Array<String>
    lateinit var TransTypeArray: Array<String>
    lateinit var TestStatusArray: Array<String>
    lateinit var RecStatusArray: Array<String>

    lateinit var deleteButton: CardView
    lateinit var saveButton: CardView

    lateinit var settings: SharedPreferences

    lateinit var contextView: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_university_detail)
        date_pickerView = findViewById<TextView>(R.id.date_picker)
        UniTitle = findViewById(R.id.uni_name)
        UniProg = findViewById(R.id.prog_name)
        UniStatus = findViewById(R.id.status_spinner)
        UniTransType = findViewById(R.id.transType_spinner)
        UniTransStatus = findViewById(R.id.transStatus_spinner)
        GREStatus = findViewById(R.id.GREStatus_spinner)
        TOEFLStatus = findViewById(R.id.TOEFLStatus_spinner)
        Rec1Name = findViewById(R.id.rec_1_name)
        Rec2Name = findViewById(R.id.rec_2_name)
        Rec3Name = findViewById(R.id.rec_3_name)
        Rec1Status = findViewById(R.id.rec_1_Status_spinner)
        Rec2Status = findViewById(R.id.rec_2_Status_spinner)
        Rec3Status = findViewById(R.id.rec_3_Status_spinner)
        Notes = findViewById(R.id.special_notes)

        DetailCard = findViewById(R.id.card_detailed)
        LORCard = findViewById(R.id.LOR_detailed)
        NotesCard = findViewById(R.id.Special_notes_detailed)

        deleteButton = findViewById(R.id.Delete_button)
        saveButton = findViewById(R.id.Save_button)

        TransStatusArray = resources.getStringArray(R.array.TransStatusSpinnerItems)
        UniStatusArray = resources.getStringArray(R.array.statusSpinnerItems)
        TransTypeArray = resources.getStringArray(R.array.TransTypeSpinnerItems)
        TestStatusArray = resources.getStringArray(R.array.TestStatusSpinnerItems)
        RecStatusArray = resources.getStringArray(R.array.RecStatusSpinnerItems)

        contextView = findViewById(R.id.uniDetailDash)

        DatePickerWidget()


        var intent = getIntent()
        var data = intent.extras
        firebaseStore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser!!


        if (data != null)      // Edit Uni
        {
            EditUni(data)

            deleteButton.setOnClickListener { View ->
                firebaseStore.collection("users").document(user!!.uid).collection("universities")
                    .document(id).delete()
                finish()
            }

            saveButton.setOnClickListener { View ->
                SaveChanges()
                finish()
            }

        } else            // New Uni
        {
            DetailCard.setCardBackgroundColor(getColor(R.color.NotSubmitted))
            LORCard.setCardBackgroundColor(getColor(R.color.NotSubmitted))
            NotesCard.setCardBackgroundColor(getColor(R.color.NotSubmitted))

            deleteButton.setOnClickListener { View ->
                finish()
            }
            saveButton.setOnClickListener { View ->
                SaveNew()
                finish()
            }

        }

        settings = getSharedPreferences("mode", Context.MODE_PRIVATE)

        //Check display mode
        if (!settings.contains("mode")) {
            var editor: SharedPreferences.Editor = settings.edit();
            editor.putString("mode", "light")
            editor.apply()
        } else {
            var mode = settings.getString("mode", "")
            if (mode == "light") {
                changeMode("light")
            } else {
                changeMode("dark")
            }
        }

    }

    // Date picker popup
    fun DatePickerWidget() {
        date_pickerView.text = SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis())
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "dd.MM.yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                date_pickerView.text = sdf.format(cal.time)

            }
        date_pickerView.setOnClickListener {
            DatePickerDialog(
                this@university_detail, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // Initialize uni attributes from the database
    fun EditUni(data: Bundle) {
        var model = data.getString("uni_data")
        id = data.getString("id", "")
        var gson: Gson = Gson()
        var uni_data: Uni_model = gson.fromJson(model, Uni_model()::class.java)
        UniTitle.setText(uni_data.uniTitle)
        UniProg.setText(uni_data.uniProg)
        UniStatus.setSelection(UniStatusArray.indexOf(uni_data.status))
        date_pickerView.setText(uni_data.deadline)
        UniTransType.setSelection(TransTypeArray.indexOf(uni_data.transType))
        UniTransStatus.setSelection(TransStatusArray.indexOf(uni_data.transStatus))
        GREStatus.setSelection(TestStatusArray.indexOf(uni_data.GREStatus))
        TOEFLStatus.setSelection(TestStatusArray.indexOf(uni_data.TOEFLStatus))
        Rec1Name.setText(uni_data.Rec1Name)
        Rec2Name.setText(uni_data.Rec2Name)
        Rec3Name.setText(uni_data.Rec3Name)
        Rec1Status.setSelection(RecStatusArray.indexOf(uni_data.Rec1Status))
        Rec2Status.setSelection(RecStatusArray.indexOf(uni_data.Rec2Status))
        Rec3Status.setSelection(RecStatusArray.indexOf(uni_data.Rec3Status))
        Notes.setText(uni_data.Notes)

        changeCardColor(uni_data)
    }

    //Add new uni to database
    fun SaveNew() {

        var saveUni: Uni_model = Uni_model(
            Title = UniTitle.text.toString(),
            Description = UniProg.text.toString(),
            Status = UniStatus.selectedItem.toString(),
            timeStamp = Timestamp.now(),
            deadline = date_pickerView.text.toString(),
            transType = UniTransType.selectedItem.toString(),
            transStatus = UniTransStatus.selectedItem.toString(),
            GREStatus = GREStatus.selectedItem.toString(),
            TOEFLStatus = TOEFLStatus.selectedItem.toString(),
            Rec1Name = Rec1Name.text.toString(),
            Rec2Name = Rec2Name.text.toString(),
            Rec3Name = Rec3Name.text.toString(),
            Rec1Status = Rec1Status.selectedItem.toString(),
            Rec2Status = Rec2Status.selectedItem.toString(),
            Rec3Status = Rec3Status.selectedItem.toString(),
            Notes = Notes.text.toString()
        )

        var addData =
            firebaseStore.collection("users").document(user!!.uid).collection("universities")
        addData.add(saveUni)

    }

    //Save edits
    fun SaveChanges() {

        var saveUni: Uni_model = Uni_model(
            Title = UniTitle.text.toString(),
            Description = UniProg.text.toString(),
            Status = UniStatus.selectedItem.toString(),
            timeStamp = Timestamp.now(),
            deadline = date_pickerView.text.toString(),
            transType = UniTransType.selectedItem.toString(),
            transStatus = UniTransStatus.selectedItem.toString(),
            GREStatus = GREStatus.selectedItem.toString(),
            TOEFLStatus = TOEFLStatus.selectedItem.toString(),
            Rec1Name = Rec1Name.text.toString(),
            Rec2Name = Rec2Name.text.toString(),
            Rec3Name = Rec3Name.text.toString(),
            Rec1Status = Rec1Status.selectedItem.toString(),
            Rec2Status = Rec2Status.selectedItem.toString(),
            Rec3Status = Rec3Status.selectedItem.toString(),
            Notes = Notes.text.toString()
        )

        var addData =
            firebaseStore.collection("users").document(user!!.uid).collection("universities")
                .document(id)
        addData.set(saveUni, SetOptions.merge())
    }

    //Change color based on status
    fun changeCardColor(uni_data: Uni_model) {
        when (uni_data.status) {
            "Not Submitted" -> {
                DetailCard.setCardBackgroundColor(getColor(R.color.NotSubmitted))
                LORCard.setCardBackgroundColor(getColor(R.color.NotSubmitted))
                NotesCard.setCardBackgroundColor(getColor(R.color.NotSubmitted))

            }
            "Submitted" -> {
                DetailCard.setCardBackgroundColor(getColor(R.color.Submitted))
                LORCard.setCardBackgroundColor(getColor(R.color.Submitted))
                NotesCard.setCardBackgroundColor(getColor(R.color.Submitted))
            }
            "Incomplete Submission" -> {
                DetailCard.setCardBackgroundColor(getColor(R.color.IncompleteSubmission))
                LORCard.setCardBackgroundColor(getColor(R.color.IncompleteSubmission))
                NotesCard.setCardBackgroundColor(getColor(R.color.IncompleteSubmission))
            }
            "Waitlisted" -> {
                DetailCard.setCardBackgroundColor(getColor(R.color.Waitlist))
                LORCard.setCardBackgroundColor(getColor(R.color.Waitlist))
                NotesCard.setCardBackgroundColor(getColor(R.color.Waitlist))
            }
            "Rejected" -> {
                DetailCard.setCardBackgroundColor(getColor(R.color.Rejected))
                LORCard.setCardBackgroundColor(getColor(R.color.Rejected))
                NotesCard.setCardBackgroundColor(getColor(R.color.Rejected))
            }
            "Accepted" -> {
                DetailCard.setCardBackgroundColor(getColor(R.color.Accepted))
                LORCard.setCardBackgroundColor(getColor(R.color.Accepted))
                NotesCard.setCardBackgroundColor(getColor(R.color.Accepted))
            }
            else -> {
                DetailCard.setCardBackgroundColor(Color.GRAY)
                LORCard.setCardBackgroundColor(Color.GRAY)
                NotesCard.setCardBackgroundColor(Color.GRAY)
            }
        }
    }

    //Dark mode light mode switcher
    fun changeMode(mode: String) {
        if (mode == "light") {
            contextView.setBackgroundColor(Color.WHITE)
            contextView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(R.color.colorPrimary)
            var editor: SharedPreferences.Editor = settings.edit();
            editor.putString("mode", "light")
            editor.apply()

        } else {
            contextView.setBackgroundColor(Color.BLACK)
            contextView.systemUiVisibility = 0
            window.statusBarColor = getColor(R.color.colorDark)
            var editor: SharedPreferences.Editor = settings.edit();
            editor.putString("mode", "dark")
            editor.apply()
        }
    }
}
