package edu.cuhk.csci3310.utrack


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.gson.Gson

// Dashboard
class uniViewer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var auth: FirebaseAuth
    lateinit var uniList: RecyclerView
    lateinit var firebaseStore: FirebaseFirestore
    lateinit var adapter: FirestoreRecyclerAdapter<Uni_model, Uni_view_holder>
    var user: FirebaseUser? = null
    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var navigationView: NavigationView
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var username: String
    lateinit var contextView: LinearLayout
    lateinit var settings: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_uni_viewer)

        //Initialize Firebase attributes
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        firebaseStore = FirebaseFirestore.getInstance()
        uniList = findViewById(R.id.uni_list)
        username = auth.currentUser!!.displayName.toString()
        contextView = findViewById<LinearLayout>(R.id.Dashboard)
        settings = getSharedPreferences("mode", Context.MODE_PRIVATE)

        //Set Navigation Bar
        addNavBar()

        if (!settings.contains("mode")) {
            var editor: SharedPreferences.Editor = settings.edit();
            editor.putString("mode", "light")
            editor.apply()
            changeMode("light")
        } else {
            var mode = settings.getString("mode", "")
            if (mode == "light") {
                changeMode("light")
            } else {
                changeMode("dark")
            }
        }


        Snackbar.make(contextView, "Welcome " + username, Snackbar.LENGTH_SHORT).show()
        //Set FAB Button
        val fab: View = findViewById(R.id.fab_uni)
        fab.setOnClickListener { view ->
            var intent = Intent(this@uniViewer, university_detail::class.java)
            startActivity(intent)
        }

        //Setting Recycler View
        addAdapter()


    }

    //Adapter Viewholder
    class Uni_view_holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var TitleView: TextView = itemView.findViewById(R.id.uniTitle)
        var DescView: TextView = itemView.findViewById(R.id.uniProg)
        var StatusView: TextView = itemView.findViewById(R.id.uniStatusDash)
        var Card: CardView = itemView.findViewById(R.id.uni_card_single)

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
        Log.d("VIEW CREATE", "Started Listening")

    }

    // Nav bar selection
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.signout -> {
                Toast.makeText(this, "You Have Signed Out", Toast.LENGTH_SHORT).show()
                auth.signOut()
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }
            R.id.help -> {
                var intent = Intent(this, About::class.java)
                startActivity(intent)
            }
            R.id.dark -> {

                if (settings.getString("mode", "") == "light") {
                    changeMode("black")
                } else {
                    changeMode("light")
                }
            }


            else -> Toast.makeText(
                this,
                "Problem in Signing Out. Try Again",
                Toast.LENGTH_SHORT
            ).show()

        }
        return true
    }

    // Add Firebase Adapter
    fun addAdapter() {
        //Query University
        var query =
            firebaseStore.collection("users").document(user!!.uid).collection("universities")
                .orderBy("timestamp")

        //Recycler Options Builder
        var options =
            FirestoreRecyclerOptions.Builder<Uni_model>().setQuery(query, Uni_model::class.java)
                .build()

        //Set Adapter
        adapter = object : FirestoreRecyclerAdapter<Uni_model, Uni_view_holder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Uni_view_holder {
                var view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.uni_single, parent, false)

                return Uni_view_holder(view)
            }

            override fun onBindViewHolder(
                holder: Uni_view_holder,
                position: Int,
                model: Uni_model
            ) {
                holder.TitleView.text = model.uniTitle
                holder.DescView.text = model.uniProg
                holder.StatusView.text = model.status

                when (model.status) {
                    "Not Submitted" -> holder.Card.setCardBackgroundColor(getColor(R.color.NotSubmitted))
                    "Submitted" -> holder.Card.setCardBackgroundColor(getColor(R.color.Submitted))
                    "Incomplete Submission" -> holder.Card.setCardBackgroundColor(getColor(R.color.IncompleteSubmission))
                    "Waitlisted" -> holder.Card.setCardBackgroundColor(getColor(R.color.Waitlist))
                    "Rejected" -> holder.Card.setCardBackgroundColor(getColor(R.color.Rejected))
                    "Accepted" -> holder.Card.setCardBackgroundColor(getColor(R.color.Accepted))
                    else -> holder.Card.setCardBackgroundColor(Color.GRAY)
                }
                holder.Card.setOnClickListener { View ->
                    var id = adapter.snapshots.getSnapshot(position).id

                    var gson: Gson = Gson()
                    var json: String = gson.toJson(model)

                    var intent = Intent(this@uniViewer, university_detail::class.java)
                    var bundle = Bundle()

                    bundle.putString("uni_data", json)
                    bundle.putString("id", id)

                    intent.putExtras(bundle)

                    startActivity(intent)
                }
            }

        }

        uniList.setHasFixedSize(false)
        uniList.layoutManager = LinearLayoutManager(this)
        uniList.adapter = adapter
    }

    //Add Nav Bar
    fun addNavBar() {
        drawerLayout = findViewById(R.id.navigationDrawer)
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.navbar)
        var header = navigationView.getHeaderView(0)
        var usernameNav = header.findViewById<TextView>(R.id.username)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawerOpen,
            R.string.drawerClose
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
        usernameNav.setText(username)
    }

    //Light mode dark mode switcher
    fun changeMode(mode: String) {
        var window = this.window
        if (mode == "light") {
            contextView.setBackgroundColor(Color.WHITE)
            toggle.drawerArrowDrawable.color = getColor(R.color.colorDark)
            contextView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(R.color.colorPrimary)
            var editor: SharedPreferences.Editor = settings.edit();
            editor.putString("mode", "light")
            editor.apply()

        } else {
            contextView.setBackgroundColor(Color.BLACK)
            contextView.systemUiVisibility = 0
            window.statusBarColor = getColor(R.color.colorDark)
            toggle.drawerArrowDrawable.color = getColor(R.color.colorAccent)
            var editor: SharedPreferences.Editor = settings.edit();

            editor.putString("mode", "dark")
            editor.apply()
        }
    }

}



