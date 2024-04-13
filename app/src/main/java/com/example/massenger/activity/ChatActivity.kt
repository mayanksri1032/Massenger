package com.example.massenger.activity

import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.widget.Toast
import com.example.massenger.R
import com.example.massenger.adapter.MessageAdapter
import com.example.massenger.databinding.ActivityChatBinding
import com.example.massenger.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var list: ArrayList<MessageModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        senderUid = FirebaseAuth.getInstance().uid.toString()
        receiverUid = intent.getStringExtra("uid")!!

        senderRoom = senderUid+receiverUid
        receiverRoom = receiverUid+senderUid

        binding= ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        list = ArrayList()

        database= FirebaseDatabase.getInstance()

        binding.imageView3.setOnClickListener {
            if(binding.msgbox.text.isEmpty()){
                Toast.makeText(this, "Please Enter Message", Toast.LENGTH_SHORT).show()
            }else{

                val message=MessageModel(binding.msgbox.text.toString(), senderUid, Date().time)

                val randomKey = database.reference.push().key

                database.reference.child("chats")
                    .child(senderRoom).child("message").child(randomKey!!).setValue(message).addOnSuccessListener {

                        database.reference.child("chats").child(receiverRoom).child("message").child(randomKey!!).setValue(message).addOnSuccessListener {
                            binding.msgbox.text =null
                            Toast.makeText(this,"Sent", Toast.LENGTH_SHORT).show()

                        }

                        }
            }
        }

        database.reference.child("chats").child(senderRoom).child("message")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()

                    for (snapshot1 in snapshot.children){
                        val data = snapshot1.getValue(MessageModel::class.java)
                        list.add(data!!)
                    }

                    binding.recyclerView.adapter = MessageAdapter(this@ChatActivity, list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity,"Error : $error",Toast.LENGTH_SHORT).show()
                }

            })
    }
}